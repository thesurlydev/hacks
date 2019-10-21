package com.digitalsanctum.utils;

import org.junit.Test;

/**
 * @author Shane Witbeck
 * @since 4/21/17
 */
public class DownloaderTest {
  
  @Test
  public void fetchFileAsString() throws Exception {
    String st = Downloader.fetchFileAsString("https://www.futz.io");
    System.out.println(st);
  }
}
