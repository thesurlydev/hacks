package com.digitalsanctum.utils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Dedupes entries in a line-delimited file given it's path and a path to the resulting output.
 *
 * @author Shane Witbeck
 * @since 4/20/17
 */
public class Deduper {

  public static void main(String[] args) throws Exception {

    if (args == null || args.length != 2) {
      System.err.println("Usage: Deduper [input path] [output path]");
      return;
    }

    String input = args[0];
    String output = args[1];

    List<String> lines = Files.readAllLines(Paths.get(input));

    Set<String> distinct = new HashSet<>(lines);

    StringBuilder sb = new StringBuilder();
    distinct.forEach(s -> sb.append(s).append("\n"));

    Files.write(Paths.get(output), sb.toString().getBytes("UTF-8"));

    System.out.println(lines.size() + " -> " + distinct.size());
  }
}
