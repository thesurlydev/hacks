package com.digitalsanctum.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author Shane Witbeck
 * @since 6/26/17
 */
public class Intersector {
  
  private Intersector() {
  }

  /**
   * Returns an array containing only elements found in {@code first} and also in {@code
   * second}. The returned elements are in the same order as in {@code first}.
   */
  @SuppressWarnings("unchecked")
  public static String[] intersect(
      Comparator<? super String> comparator, String[] first, String[] second) {
    List<String> result = new ArrayList<>();
    for (String a : first) {
      for (String b : second) {
        if (comparator.compare(a, b) == 0) {
          result.add(a);
          break;
        }
      }
    }
    return result.toArray(new String[result.size()]);
  }

  /**
   * Returns true if there is an element in {@code first} that is also in {@code second}. This
   * method terminates if any intersection is found. The sizes of both arguments are assumed to be
   * so small, and the likelihood of an intersection so great, that it is not worth the CPU cost of
   * sorting or the memory cost of hashing.
   */
  public static boolean nonEmptyIntersection(
      Comparator<String> comparator, String[] first, String[] second) {
    if (first == null || second == null || first.length == 0 || second.length == 0) {
      return false;
    }
    for (String a : first) {
      for (String b : second) {
        if (comparator.compare(a, b) == 0) {
          return true;
        }
      }
    }
    return false;
  }
}
