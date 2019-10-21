package com.digitalsanctum.utils.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AggregateResponse {
  Set<String> cidrs;
  Long distinctAddressCount;
}
