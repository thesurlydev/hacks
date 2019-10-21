package com.digitalsanctum.rir;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.digitalsanctum.rir.RirProcessor.Rir.values;
import static java.lang.String.format;
import static java.lang.System.out;
import static java.nio.file.Paths.get;
import static java.util.stream.Stream.of;

/**
 * Downloads and parses IPv4 records from all five Regional Internet Registries (RIR)
 *
 * @author Shane Witbeck
 * @since 5/29/17
 */
public class RirProcessor {

  public enum Rir {
    ARIN("ftp://ftp.arin.net/pub/stats/arin", "delegated-arin-extended-latest"),
    RIPE("ftp://ftp.ripe.net/ripe/stats", "delegated-ripencc-extended-latest"),
    AFRINIC("ftp://ftp.afrinic.net/pub/stats/afrinic", "delegated-afrinic-extended-latest"),
    APNIC("ftp://ftp.apnic.net/pub/stats/apnic", "delegated-apnic-extended-latest"),
    LACNIC("ftp://ftp.lacnic.net/pub/stats/lacnic", "delegated-lacnic-extended-latest");

    private String baseUrl;
    private String extendedLatest;

    Rir(String baseUrl, String extendedLatest) {
      this.baseUrl = baseUrl;
      this.extendedLatest = extendedLatest;
    }

    public String getExtendedLatest() {
      return extendedLatest;
    }

    public URL getExtendedLatestUrl() {
      try {
        return new URL(baseUrl + "/" + extendedLatest);
      } catch (MalformedURLException e) {
        e.printStackTrace();
      }
      return null;
    }

    public Path getExtendedLatestDownloadPath(Path downloadDir) {
      return downloadDir.resolve(extendedLatest);
    }

    public void logDownload(Path downloadDir) {
      out.println(format("downloading %s to %s", getExtendedLatestUrl(), getExtendedLatestDownloadPath(downloadDir)));
    }
  }

  public static void main(String[] args) throws Exception {

    if (args == null || args.length != 1) {
      System.err.println("usage RirProcessor [DOWNLOAD_PATH]");
      return;
    }

    String downloadPath = args[0];
    RirProcessor processor = new RirProcessor();

    Path downloadDir = get(downloadPath);
    
    if (!downloadDir.toFile().exists()) {
      System.out.println(String.format("creating %s", downloadDir));
      Files.createDirectory(downloadDir);
    }    
    processor.download(downloadDir);
  }

  private void download(Path downloadDir) {
    of(values())
        .parallel()
        .peek(rir -> rir.logDownload(downloadDir))
        .forEach(rir -> {
          if (rir.getExtendedLatestUrl() != null) {
            try (FileOutputStream fos = new FileOutputStream(rir.getExtendedLatestDownloadPath(downloadDir).toFile())) {
              ReadableByteChannel rbc = Channels.newChannel(rir.getExtendedLatestUrl().openStream());
              fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        });
  }
}
