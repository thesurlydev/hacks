package com.digitalsanctum.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.util.Scanner;

public class Downloader {

  private static final String NEW_LINE = "\\A";

  public static Path download(String spec, Path path) {
    try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
      URL url = new URL(spec);
      ReadableByteChannel rbc = Channels.newChannel(url.openStream());
      fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return path;
  }
  
  public static Path download(URL url, Path path) {
    try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
      ReadableByteChannel rbc = Channels.newChannel(url.openStream());
      fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return path;
  }
  
  public static File download(URL url, File file) {
    try (FileOutputStream fos = new FileOutputStream(file)) {
      ReadableByteChannel rbc = Channels.newChannel(url.openStream());
      fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return file;
  }
  
  public static File download(InputStream is, File file) {
    try (FileOutputStream fos = new FileOutputStream(file)) {
      ReadableByteChannel rbc = Channels.newChannel(is);
      fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return file;
  }

  public static String fetchFileAsString(final String url) throws IOException {
    String out;
    try(InputStream is = new URL(url).openStream()) {
      out = convertStreamToString(is);
    }
    return out;
  }

  public static String convertStreamToString(InputStream is) {
    String out = null;
    try {
      Scanner scanner = new Scanner(is).useDelimiter(NEW_LINE);
      out = scanner.hasNext() ? scanner.next() : "";
      is.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return out;
  }
}
