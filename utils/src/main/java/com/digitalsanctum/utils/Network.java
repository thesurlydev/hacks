package com.digitalsanctum.utils;

import com.digitalsanctum.utils.model.Cidr;
import com.digitalsanctum.utils.model.Ip;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.lang.Long.valueOf;
import static java.net.InetAddress.getByAddress;
import static java.net.InetAddress.getByName;
import static java.nio.ByteBuffer.wrap;
import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.of;

/**
 * @author Shane Witbeck
 * @since 4/21/17
 */
public class Network {

  private static final char DOT = '.';
  private static final String SLASH = "/";
  private static final String DOT_REGEX = "\\.";
  private static final Pattern IPV4_ADDRESS_PATTERN = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");
  private static final Pattern IPV4_CIDR_PATTERN = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])(\\/(3[0-2]|[1-2][0-9]|[0-9]))$");

  public static boolean isValidCidr(String cidr) {
    return IPV4_CIDR_PATTERN.matcher(cidr).matches();
  }

  public static boolean isValidIpAddress(String ipAddress) {
    return IPV4_ADDRESS_PATTERN.matcher(ipAddress).matches();
  }

  public static String longToIpAddress(long ip) {
    StringBuilder sb = new StringBuilder(15);
    for (int i = 0; i < 4; i++) {
      sb.insert(0, Long.toString(ip & 0xff));
      if (i < 3) {
        sb.insert(0, DOT);
      }
      ip >>= 8;
    }
    return sb.toString();
  }

  public static long ipAddressToLong(String ipAddress) {
    long result = 0;
    String[] ipAddressInArray = ipAddress.split(DOT_REGEX);
    for (int i = 3; i >= 0; i--) {
      long ip = parseLong(ipAddressInArray[3 - i]);
      result |= ip << (i * 8);
    }
    return result;
  }

  public static List<String> ipRangeToCidrList(String startIp, String endIp) {
    long start = ipToLong(startIp);
    long end = ipToLong(endIp);

    List<String> pairs = new ArrayList<>();
    while (end >= start) {
      byte maxSize = 32;
      while (maxSize > 0) {
        long mask = CIDR2MASK[maxSize - 1];
        long maskedBase = start & mask;

        if (maskedBase != start) {
          break;
        }

        maxSize--;
      }
      double x = Math.log(end - start + 1) / Math.log(2);
      byte maxDiff = (byte) (32 - Math.floor(x));
      if (maxSize < maxDiff) {
        maxSize = maxDiff;
      }
      String ip = longToIP(start);
      pairs.add(ip + "/" + maxSize);
      start += Math.pow(2, (32 - maxSize));
    }
    return pairs;
  }

  public static final int[] CIDR2MASK = new int[]{0x00000000, 0x80000000,
      0xC0000000, 0xE0000000, 0xF0000000, 0xF8000000, 0xFC000000,
      0xFE000000, 0xFF000000, 0xFF800000, 0xFFC00000, 0xFFE00000,
      0xFFF00000, 0xFFF80000, 0xFFFC0000, 0xFFFE0000, 0xFFFF0000,
      0xFFFF8000, 0xFFFFC000, 0xFFFFE000, 0xFFFFF000, 0xFFFFF800,
      0xFFFFFC00, 0xFFFFFE00, 0xFFFFFF00, 0xFFFFFF80, 0xFFFFFFC0,
      0xFFFFFFE0, 0xFFFFFFF0, 0xFFFFFFF8, 0xFFFFFFFC, 0xFFFFFFFE,
      0xFFFFFFFF};

  public static long ipToLong(String ipString) {
    long[] ip = new long[4];
    String[] ipSec = ipString.split(DOT_REGEX);
    for (int k = 0; k < 4; k++) {
      ip[k] = valueOf(ipSec[k]);
    }
    return (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3];
  }

  public static String longToIP(long longIP) {
    StringBuilder sb = new StringBuilder("");
    sb.append(String.valueOf(longIP >>> 24));
    sb.append(".");
    sb.append(String.valueOf((longIP & 0x00FFFFFF) >>> 16));
    sb.append(".");
    sb.append(String.valueOf((longIP & 0x0000FFFF) >>> 8));
    sb.append(".");
    sb.append(String.valueOf(longIP & 0x000000FF));
    return sb.toString();
  }

  public static boolean isInRange(String cidrFormat, String ipAddress) {
    InetAddress inetAddress = null;
    try {
      inetAddress = getByName(ipAddress);
    } catch (UnknownHostException e) {
      throw new IllegalArgumentException("Invalid ipAddress: " + ipAddress, e);
    }
    
    Optional<Cidr> maybeCidr = toCidr(cidrFormat);
    if (!maybeCidr.isPresent()) {
      throw new IllegalArgumentException("Could not derive Cidr from: " + cidrFormat);
    }
    Cidr cidr = maybeCidr.get();
    BigInteger start = new BigInteger(1, cidr.getNetworkAddressAsBytes());
    BigInteger end = new BigInteger(1, cidr.getBroadcastAddressAsBytes());
    BigInteger target = new BigInteger(1, inetAddress.getAddress());

    int st = start.compareTo(target);
    int te = target.compareTo(end);

    return (st == -1 || st == 0) && (te == -1 || te == 0);
  }
  
  public static boolean contains(String referenceCidr, String evalCidr) {
    Cidr refCidr = toCidr(referenceCidr).get();
    Long refCidrNetworkAddressAsLong = refCidr.getNetworkAddressAsLong();
    Long refCidrBroadcastAddressAsLong = refCidr.getBroadcastAddressAsLong();
    
    Cidr eCidr = toCidr(evalCidr).get();
    Long eCidrNetworkAddressAsLong = refCidr.getNetworkAddressAsLong();
    Long eCidrBroadcastAddressAsLong = eCidr.getBroadcastAddressAsLong();
    
    return refCidrNetworkAddressAsLong <= eCidrNetworkAddressAsLong && refCidrBroadcastAddressAsLong >= eCidrBroadcastAddressAsLong;    
  }
  
  public static Optional<InetAddress> toInetAddress(byte[] bytes) {
    try {
      return of(getByAddress(bytes));
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }
    return Optional.empty();
  }
 
  /**
   * @param cidrString an address in CIDR notation. For example, 192.168.1.0/24
   * @return an array of size 2 where first element is a byte array representing the network address and
   * the second element is a byte array representing the broadcast address.
   */
  public static Optional<Cidr> toCidr(String cidrString) {
    requireNonNull(cidrString, "cidr is required");
    if (!isValidCidr(cidrString)) {
      if (isValidIpAddress(cidrString)) {
        cidrString = cidrString + "/32";
      } else {
        throw new IllegalArgumentException("invalid cidr: " + cidrString);
      }
    }
    
    Cidr cidr = new Cidr(cidrString);
    
    /* split CIDR to address and prefix part */
    int index = cidrString.indexOf(SLASH);
    String addressPart = cidrString.substring(0, index);
    String networkPart = cidrString.substring(index + 1);
    cidr.setCidr(parseInt(networkPart));
    
    int prefixLength = parseInt(networkPart);
    InetAddress inetAddress;
    try {
      inetAddress = getByName(addressPart);

      ByteBuffer maskBuffer;
      int targetSize;
      if (inetAddress.getAddress().length == 4) {
        maskBuffer =
            ByteBuffer
                .allocate(4)
                .putInt(-1);
        targetSize = 4;
      } else {
        maskBuffer = ByteBuffer.allocate(16)
            .putLong(-1L)
            .putLong(-1L);
        targetSize = 16;
      }

      BigInteger mask = (new BigInteger(1, maskBuffer.array())).not().shiftRight(prefixLength);
      ByteBuffer buffer = wrap(inetAddress.getAddress());
      BigInteger ipVal = new BigInteger(1, buffer.array());

      BigInteger networkBigInteger = ipVal.and(mask);
      byte[] networkIpArr = toBytes(networkBigInteger.toByteArray(), targetSize);
      cidr.setNetworkAddressAsBytes(networkIpArr);
      
      InetAddress networkInetAddress = getByAddress(networkIpArr);      
      
      String networkIp = networkInetAddress.getHostAddress();
      cidr.setNetworkAddress(networkIp);      
      
      Long networkAsLong = ipToLong(networkIp);
      cidr.setNetworkAddressAsLong(networkAsLong);

      BigInteger endIp = networkBigInteger.add(mask.not());
      byte[] broadcastIpArr = toBytes(endIp.toByteArray(), targetSize);
      cidr.setBroadcastAddressAsBytes(broadcastIpArr);
      
      InetAddress broadcastInetAddress = getByAddress(broadcastIpArr);      
      
      String broadcastIp = broadcastInetAddress.getHostAddress();
      cidr.setBroadcastAddress(broadcastIp);      
      
      Long broadcastAsLong = ipToLong(broadcastIp);
      cidr.setBroadcastAddressAsLong(broadcastAsLong);
      
      Ip ip = new Ip(networkIp);
      cidr.setOctet1(ip.getFirst());
      cidr.setOctet2(ip.getSecond());
      cidr.setOctet3(ip.getThird());
      cidr.setOctet4(ip.getFourth());
      
      Optional<CIDR> maybeCidr = CIDR.fromCidr(Integer.parseInt(networkPart));
      if (maybeCidr.isPresent()) {
        CIDR cidrEnum = maybeCidr.get();
        cidr.setNumAddresses((long) cidrEnum.getNumberOfAddresses());
        cidr.setSubnetMask(cidrEnum.getSubnetMask());
        cidr.setHostBits(cidrEnum.getHostBits());
      }

      return Optional.of(cidr);

    } catch (Exception e) {
      e.printStackTrace();
      return Optional.empty();
    }    
  }
  
  @SuppressWarnings("SpellCheckingInspection")
  static enum CIDR {
    CIDR_32(32, 0,"255.255.255.255"),
    CIDR_31(31, 1,"255.255.255.254"),
    CIDR_30(30, 2,"255.255.255.252"),
    CIDR_29(29, 3,"255.255.255.248"),
    CIDR_28(28, 4,"255.255.255.240"),
    CIDR_27(27, 5,"255.255.255.224"),
    CIDR_26(26, 6,"255.255.255.192"),
    CIDR_25(25, 7,"255.255.255.128"),
    
    CIDR_24(24, 8,"255.255.255.0"),
    CIDR_23(23, 9,"255.255.254.0"),
    CIDR_22(22, 10,"255.255.252.0"),
    CIDR_21(21, 11,"255.255.248.0"),
    CIDR_20(20, 12,"255.255.240.0"),
    CIDR_19(19, 13,"255.255.224.0"),
    CIDR_18(18, 14,"255.255.192.0"),
    CIDR_17(17, 15,"255.255.128.0"),
    
    CIDR_16(16, 16,"255.255.0.0"),
    CIDR_15(15, 17,"255.254.0.0"),
    CIDR_14(14, 18,"255.252.0.0"),
    CIDR_13(13, 19,"255.248.0.0"),
    CIDR_12(12, 20,"255.240.0.0"),
    CIDR_11(11, 21,"255.224.0.0"),
    CIDR_10(10, 22,"255.192.0.0"),
    CIDR_9(9, 23,"255.128.0.0"),
    
    CIDR_8(8, 24,"255.0.0.0"),
    CIDR_7(7, 25,"254.0.0.0"),
    CIDR_6(6, 26,"252.0.0.0"),
    CIDR_5(5, 27,"248.0.0.0"),
    CIDR_4(4, 28,"240.0.0.0"),
    CIDR_3(3, 29,"224.0.0.0"),
    CIDR_2(2, 30,"192.0.0.0"),
    CIDR_1(1, 31,"128.0.0.0"),
    CIDR_0(0, 32,"0.0.0.0");

    private int cidr;
    private int hostBits;
    private String subnetMask;
    
    CIDR(int cidr, int hostBits, String subnetMask) {
      this.cidr = cidr;
      this.hostBits = hostBits;
      this.subnetMask = subnetMask;
    }

    public int getCidr() {
      return cidr;
    }

    public int getHostBits() {
      return hostBits;
    }

    public double getNumberOfAddresses() {
      return Math.pow(2, hostBits);
    }

    public String getSubnetMask() {
      return subnetMask;
    }
    
    public static Optional<CIDR> fromCidr(int testCidr) {
      return stream(values())
          .filter(cidr -> cidr.getCidr() == testCidr)
          .findFirst();
    }
  }

  private static byte[] toBytes(byte[] array, int targetSize) {
    int counter = 0;
    List<Byte> newArr = new ArrayList<>();
    while (counter < targetSize && (array.length - 1 - counter >= 0)) {
      newArr.add(0, array[array.length - 1 - counter]);
      counter++;
    }

    int size = newArr.size();
    for (int i = 0; i < (targetSize - size); i++) {
      newArr.add(0, (byte) 0);
    }

    byte[] ret = new byte[newArr.size()];
    for (int i = 0; i < newArr.size(); i++) {
      ret[i] = newArr.get(i);
    }
    return ret;
  }
}
