package com.digitalsanctum.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProcessUtils {

  private static void runProcess(String... commandArgs) {
    runProcess(null, commandArgs);
  }

  private static void runProcess(Map<String, String> environmentVariables, String... commandArgs) {
    ProcessBuilder builder = new ProcessBuilder(commandArgs);
    if (environmentVariables != null && !environmentVariables.isEmpty()) {
      environmentVariables.forEach(builder.environment()::put);
    }

    final Process process;
    try {
      process = builder.start();

      try (InputStream is = process.getInputStream()) {
        String out = inputStreamToString(is);
        System.out.println(out);
      }
      try (InputStream err = process.getErrorStream()) {
        String error = inputStreamToString(err);
        System.err.println(error);
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static Set<String> inputStreamToStringSet(InputStream is) throws IOException {
    InputStreamReader isr = new InputStreamReader(is);
    BufferedReader br = new BufferedReader(isr);
    String line;
    Set<String> output = new HashSet<>();
    while ((line = br.readLine()) != null) {
      output.add(line);
    }
    return output;
  }

  public static String inputStreamToString(InputStream is) throws IOException {
    InputStreamReader isr = new InputStreamReader(is);
    BufferedReader br = new BufferedReader(isr);
    String line;
    StringBuilder output = new StringBuilder();
    while ((line = br.readLine()) != null) {
      output.append(line);
    }
    return output.toString();
  }


}
