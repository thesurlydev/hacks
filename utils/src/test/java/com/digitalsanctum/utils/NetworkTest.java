package com.digitalsanctum.utils;

import com.digitalsanctum.utils.model.Cidr;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

public class NetworkTest {
  
  @Test
  public void ipRangeToCidrList() {
    List<String> cidrs = Network.ipRangeToCidrList("199.115.124.0", "199.115.127.255");
    System.out.println(cidrs);
  }
  
  @Test
  public void calculate() throws Exception {
    Optional<Cidr> cidr = Network.toCidr("192.168.100.0/24");
    System.out.println(cidr); 
  }
  
  @Test
  public void containsPositive() {
    String ref = "192.168.100.0/24";
    String eval = "192.168.100.1/32";
    Assert.assertTrue(Network.contains(ref, eval));
    
    ref = "47.95.228.0/22";
    eval = "47.95.231.0/24";
    Assert.assertTrue(Network.contains(ref, eval));
  }
  
  @Test
  public void containsNegative() {
    String ref = "192.168.100.0/24";
    String eval = "192.168.101.1/32";
    Assert.assertFalse(Network.contains(ref, eval));
  }
}
