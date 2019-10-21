package com.digitalsanctum.utils.model;

import lombok.Data;

@Data
public class Cidr {
  private final String subnet; // 192.168.100.0/24
  private Integer cidr; // /24
  private String subnetMask; // 255.255.55.0
  private Long numAddresses; // 256
  private String networkAddress; // 192.168.100.0
  private byte[] networkAddressAsBytes; // 
  private Long networkAddressAsLong; // 3232261120
  private String broadcastAddress; // 192.168.100.255
  private byte[] broadcastAddressAsBytes; // 
  private Long broadcastAddressAsLong; // 3232261375
  private Integer hostBits; // 8
  
  private Integer octet1;
  private Integer octet2;
  private Integer octet3;
  private Integer octet4;
}
