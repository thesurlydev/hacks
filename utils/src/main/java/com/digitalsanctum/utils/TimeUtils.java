package com.digitalsanctum.utils;

/**
 * @author Shane Witbeck
 * @since 4/21/17
 */
public class TimeUtils {

  private static final double ONE_MILLION = 1000000.0;
  
  private TimeUtils() {}

  public static double nanoToMillis(long nano) {
    return nano / ONE_MILLION;
  }

  public static double durationInMillis(long startTimeNano) {
    return nanoToMillis(System.nanoTime() - startTimeNano);
  }
}
