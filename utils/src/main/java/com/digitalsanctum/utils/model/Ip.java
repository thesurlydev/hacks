package com.digitalsanctum.utils.model;

import lombok.Data;

import static java.lang.Integer.parseInt;
import static java.util.Objects.requireNonNull;

@Data
public class Ip {
  private final String ip;
  final int first;
  final int second;
  final int third;
  final int fourth;

  public Ip(String ip) {
    this.ip = requireNonNull(ip, "ip is required");    
    String[] octets = ip.split("\\.");
    this.first = parseInt(octets[0]);
    this.second = parseInt(octets[1]);
    this.third = parseInt(octets[2]);
    this.fourth = parseInt(octets[3]);
  }
  
  public Integer[] pair() {
    int left = (first << 24) + (second << 16) + (third << 8) + fourth;
    int right = 32;
    Integer[] out = new Integer[2];
    out[0] = left;
    out[1] = right;
    return out;
  }
}
