package com.digitalsanctum.utils;

import com.digitalsanctum.utils.model.AggregateResponse;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toSet;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class NetworkAggregatorTest {

  @Test
  public void testMixed() {
    Set<String> values = new HashSet<>();
    values.add("47.93.148.0/24");
    values.add("47.93.149.0/24");
    values.add("47.95.228.0/24");
    values.add("47.95.229.0/24");
    values.add("47.95.230.0/24");
    values.add("47.95.231.0/24");
    values.add("47.95.232.0/24");
    values.add("104.236.131.162");
    values.add("104.236.131.96");
    values.add("211.159.166.0/24");

    AggregateResponse response = new NetworkAggregator().aggregate(values);
    Set<String> cidrs = response.getCidrs();
    
    cidrs.forEach(System.out::println);
    
    assertThat(response.getCidrs().size(), is(6));

    /*
    47.95.228.0/22 : 1024
    47.93.148.0/23 : 512
     */
    
    assertTrue(cidrs.contains("47.93.148.0/23"));
    assertTrue(cidrs.contains("47.95.228.0/22"));
    assertTrue(cidrs.contains("47.95.232.0/24"));
    assertTrue(cidrs.contains("104.236.131.96/32"));
    assertTrue(cidrs.contains("104.236.131.162/32"));
    assertTrue(cidrs.contains("211.159.166.0/24"));
  }

  @Test
  public void testCombineCidrAndSingleIp() {
    Set<String> values = new HashSet<>();
    values.add("1.1.1.1");
    values.add("192.168.0.0/24");
    values.add("192.168.1.0/24");

    AggregateResponse response = new NetworkAggregator().aggregate(values);
    Set<String> cidrs = response.getCidrs();
    assertThat(cidrs.size(), is(2));
    assertTrue(cidrs.contains("1.1.1.1/32"));
    assertTrue(cidrs.contains("192.168.0.0/23"));
  }

  @Test
  public void testCombineCidrs() {
    Set<String> values = new HashSet<>();
    values.add("192.168.0.0/24");
    values.add("192.168.1.0/24");

    AggregateResponse response = new NetworkAggregator().aggregate(values);
    Set<String> cidrs = response.getCidrs();
    assertThat(cidrs.size(), is(1));
    assertThat(cidrs.iterator().next(), is("192.168.0.0/23"));
  }

  @Test
  public void testOrderedSingleClassCCidr() {
    Set<String> testIps = IntStream.range(0, 256)
        .mapToObj(value -> "1.1.1." + value)
        .collect(toSet());

    AggregateResponse response = new NetworkAggregator().aggregate(testIps);
    Set<String> cidrs = response.getCidrs();
    assertThat(cidrs.size(), is(1));
    assertThat(cidrs.iterator().next(), is("1.1.1.0/24"));
  }

  @Test
  public void testReverseOrderedSingleClassCCidr() {
    Set<String> testIps = revRange(0, 256)
        .mapToObj(value -> "1.1.1." + value)
        .collect(toSet());

    AggregateResponse response = new NetworkAggregator().aggregate(testIps);
    Set<String> cidrs = response.getCidrs();
    assertThat(cidrs.size(), is(1));
    assertThat(cidrs.iterator().next(), is("1.1.1.0/24"));
  }

  @Test
  public void testSingleIp() {
    Set<String> testIps = Collections.singleton("1.1.1.1");

    AggregateResponse response = new NetworkAggregator().aggregate(testIps);
    Set<String> cidrs = response.getCidrs();
    assertThat(cidrs.size(), is(1));
    assertThat(cidrs.iterator().next(), is("1.1.1.1/32"));
  }

  @Test
  public void testSingleCidr() {
    Set<String> testIps = Collections.singleton("1.1.1.0/24");

    AggregateResponse response = new NetworkAggregator().aggregate(testIps);
    Set<String> cidrs = response.getCidrs();
    assertThat(cidrs.size(), is(1));
    assertThat(cidrs.iterator().next(), is("1.1.1.0/24"));
  }

  private static IntStream revRange(int from, int to) {
    return IntStream.range(from, to).map(i -> to - i + from - 1);
  }
}
