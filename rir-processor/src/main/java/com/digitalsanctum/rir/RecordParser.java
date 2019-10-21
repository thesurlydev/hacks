package com.digitalsanctum.rir;

import com.digitalsanctum.rir.model.Record;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.nio.file.Files.lines;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.of;

public class RecordParser {

  private static final String IPV4 = "ipv4";
  private static final String IPV6 = "ipv6";
  private static final String ASN = "asn";
  private static final String PIPE_REGEX = "\\|";

  public static void main(String[] args) {
    if (args == null || args.length != 1) {
      System.err.println("usage RirProcessor [DOWNLOAD_PATH]");
      return;
    }

    String downloadPath = args[0];
    RecordParser parser = new RecordParser();
    List<Record> records = parser.parse(Paths.get(downloadPath));

    System.out.println(records.size());
  }

  public List<Record> parse(Path downloadDir) {
    return of(RirProcessor.Rir.values()).parallel()
        .map(rir -> downloadDir.resolve(rir.getExtendedLatest()))
        .peek(p -> System.out.println("processing " + p.toString()))
        .flatMap((Function<Path, Stream<Record>>) path -> {
          try {
            return lines(path)
                .map(s -> s.split(PIPE_REGEX))
                .filter(arr -> arr.length == 8)
                .filter(arr -> IPV4.equals(arr[2]) || ASN.equals(arr[2]) || IPV6.equals(arr[2]))
                .map(Record::new);            
          } catch (IOException e) {
            e.printStackTrace();
          }
          return null;
        })
        .collect(toList());
  }
}
