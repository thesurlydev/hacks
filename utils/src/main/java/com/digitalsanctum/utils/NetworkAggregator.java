package com.digitalsanctum.utils;

import com.digitalsanctum.utils.model.AggregateResponse;
import com.digitalsanctum.utils.model.Cidr;
import com.digitalsanctum.utils.model.Ip;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static com.digitalsanctum.utils.Network.isInRange;
import static com.digitalsanctum.utils.Network.isValidCidr;
import static com.digitalsanctum.utils.Network.longToIpAddress;
import static com.digitalsanctum.utils.Network.toCidr;
import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.of;

public class NetworkAggregator {

  public AggregateResponse aggregate(Set<String> values) {

    // convert inputs to cidrs
    Set<Cidr> inputCidrs = values.stream()
        .map(Network::toCidr)
        .filter(Objects::nonNull)
        .map(Optional::get)
        .collect(toSet());

    Long inputSize = inputCidrs.stream()
        .mapToLong(Cidr::getNumAddresses)
        .sum();

    // if any ips are CIDRs, then convert them to ip ranges first.
    // convert ips to int pairs
    // sort pairs
    List<Integer[]> pairs = values.stream()
        .flatMap(this::toIps)
        .map(Ip::new)
        .map(Ip::pair)
        .sorted(comparing(integers -> integers[0], comparingInt(o -> o)))
        .collect(toList());

    // keep combining until we can't anymore
    List<Integer[]> combined = combine(pairs);
    int prevSize;
    do {
      prevSize = combined.size();
      combined = combine(combined);
    } while (combined.size() < prevSize);

    // convert combined to CIDRs
    Set<String> combinedCidrs = combined.stream()
        .map(this::convertToCidr)
        .collect(toSet());

    combineOutcasts(pairs, combinedCidrs);

    // now add back any original inputs that haven't already been combined
    addMissingOriginalInputs(inputCidrs, combinedCidrs);

    // compare input/output number of IPs and make sure they match
    Long distinctAddressCount = doSanityCheck(inputSize, combinedCidrs);

    // sort
    /*combinedCidrs = combinedCidrs.stream()
        .map(Network::toCidr)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .sorted()
        .map(Cidr::getSubnet)
        .collect(toSet());*/
    
    return new AggregateResponse(combinedCidrs, distinctAddressCount);
  }

  private void combineOutcasts(List<Integer[]> pairs, Set<String> combinedCidrs) {
    // determine if there's any of the original pairs left out after combining. if there are, then add them back
    List<Integer[]> outcasts = getUncombined(pairs, combinedCidrs);

    // combine the outcasts
    List<Integer[]> combinedOutcasts = combine(outcasts);
    int prevOutcastSize;
    do {
      prevOutcastSize = combinedOutcasts.size();
      combinedOutcasts = combine(combinedOutcasts);
    } while (combinedOutcasts.size() < prevOutcastSize);

    combinedCidrs.addAll(combinedOutcasts.stream()
        .map(this::convertToCidr)
        .collect(toList())
    );
  }

  private void addMissingOriginalInputs(Set<Cidr> inputCidrs, Set<String> combinedCidrs) {
    List<String> adds = new ArrayList<>();
    Set<String> inputCidrSubnets = inputCidrs.stream()
        .map(Cidr::getSubnet)
        .collect(toSet());

    for (String inputCidrSubnet : inputCidrSubnets) {
      boolean add = true;
      for (String combinedCidrSubnet : combinedCidrs) {
        if (Network.contains(combinedCidrSubnet, inputCidrSubnet)) {
          add = false;
          break;
        }
      }
      if (add) {
        adds.add(inputCidrSubnet);
      }
    }
    combinedCidrs.addAll(adds);
  }

  private Long doSanityCheck(Long inputSize, Set<String> combinedCidrs) {

    Long outputSize = combinedCidrs.stream()
        .map(Network::toCidr)
        .filter(Objects::nonNull)
        .map(Optional::get)
        .mapToLong(Cidr::getNumAddresses)
        .sum();

    // sanity check to verify we get the same number of overall IPs
    if (!inputSize.equals(outputSize)) {
      throw new IllegalStateException("Output size did not match input size. Input size=" + inputSize + ", output size=" + outputSize);
    }

    return outputSize;
  }

  private List<Integer[]> getUncombined(List<Integer[]> pairs, Set<String> combinedCidrs) {
    List<Integer[]> outcasts = new ArrayList<>();
    for (Integer[] pair : pairs) {
      boolean add = true;
      for (String cidr : combinedCidrs) {
        if (isInRange(cidr, longToIpAddress(pair[0]))) {
          add = false;
          break;
        }
      }
      if (add) {
        outcasts.add(pair);
      }
    }
    return outcasts;
  }

  private List<Integer[]> combine(List<Integer[]> pairs) {
    List<Integer[]> combined = new ArrayList<>();
    for (int i = 1; i < pairs.size(); i++) {
      Integer[] prevPair = pairs.get(i - 1);
      Integer[] pair = pairs.get(i);
      boolean combine = canCombine(pair, prevPair);
      if (combine) {
        Integer[] currCombined = new Integer[]{prevPair[0], prevPair[1] - 1};
        combined.add(currCombined);
      }
    }
    return combined.isEmpty() ? pairs : combined;
  }

  private Stream<String> toIps(String s) {
    if (isValidCidr(s)) {
      Optional<Cidr> maybeCidr = toCidr(s);
      if (maybeCidr.isPresent()) {
        return getIpList(maybeCidr.get()).stream();
      } else {
        return Stream.of();
      }
    } else {
      return of(s);
    }
  }

  private List<String> getIpList(Cidr cidr) {
    return LongStream.rangeClosed(cidr.getNetworkAddressAsLong(), cidr.getBroadcastAddressAsLong())
        .mapToObj(Network::longToIpAddress)
        .collect(toList());
  }

  private String convertToCidr(Integer[] pair) {
    return longToIpAddress(pair[0]) + "/" + pair[1];
  }

  private boolean canCombine(Integer[] o1, Integer[] o2) {
    int base1 = o1[0], bits1 = o1[1];
    int base2 = o2[0], bits2 = o2[1];
    return bits1 == bits2 && (base2 ^ base1) == (1 << (32 - bits1));
  }
}
