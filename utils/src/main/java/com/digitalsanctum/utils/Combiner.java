package com.digitalsanctum.utils;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Shane Witbeck
 * @since 4/20/17
 */
public class Combiner {

  public static void main(String[] args) throws Exception {
    Path dir = Paths.get(args[0]);
    String output = args[1];

    PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + dir + "**/*.txt");

    List<String> lines = Files.walk(dir)
        .filter(pathMatcher::matches)
        .flatMap(input -> {
          try {
            if (input != null) {
              return Files.readAllLines(input).stream();
            }
          } catch (IOException e) {
            e.printStackTrace();
          }
          return Stream.empty();
        })
        .collect(Collectors.toList());

    Set<String> distinct = new HashSet<>(lines);

    StringBuilder sb = new StringBuilder();
    distinct.forEach(s -> sb.append(s).append("\n"));

    Files.write(Paths.get(output), sb.toString().getBytes("UTF-8"));

    System.out.println(lines.size() + " -> " + distinct.size());
  }
}
