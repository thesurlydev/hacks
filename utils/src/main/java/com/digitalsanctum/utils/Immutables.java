package com.digitalsanctum.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Shane Witbeck
 * @since 6/26/17
 */
public class Immutables {

  private Immutables() {
  }

  /** Returns an immutable copy of {@code list}. */
  public static <T> List<T> immutableList(List<T> list) {
    return Collections.unmodifiableList(new ArrayList<>(list));
  }

  /** Returns an immutable list containing {@code elements}. */
  public static <T> List<T> immutableList(T... elements) {
    return Collections.unmodifiableList(Arrays.asList(elements.clone()));
  }
}
