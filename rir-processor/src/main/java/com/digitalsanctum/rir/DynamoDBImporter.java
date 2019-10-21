package com.digitalsanctum.rir;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.IDynamoDBMapper;
import com.digitalsanctum.rir.model.Record;
import com.digitalsanctum.rir.model.Rir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class DynamoDBImporter {

  private static final Logger log = LoggerFactory.getLogger(DynamoDBImporter.class);

  private final IDynamoDBMapper mapper;

  public DynamoDBImporter(IDynamoDBMapper mapper) {
    this.mapper = mapper;
  }

  public void importData() {
    String downloadDir = "/Users/switbe/projects/rir-processor/downloads";
    RecordParser parser = new RecordParser();
    List<Record> records = parser.parse(Paths.get(downloadDir))/*.subList(0, 10)*/;
    List<Rir> rirs = records.stream()
//        .filter(record -> record.getExtensions().equals("e2ad2c1e471df9858b8f9e275589a89a"))
        .map(Rir::new)
        .collect(toList());
    batchSave(rirs);
  }

  public void batchSave(List<Rir> rirs) {
    List<DynamoDBMapper.FailedBatch> failedBatches = this.mapper.batchWrite(rirs, emptyList());
    if (failedBatches != null && !failedBatches.isEmpty()) {
      failedBatches.forEach(fb -> log.error("batchSave failed: {}", fb.toString()));
    }
  }


  public static void main(String[] args) {

    long start = System.currentTimeMillis();

//    AmazonDynamoDB lowLevelClient = AmazonDynamoDBClientBuilder.defaultClient();
    AmazonDynamoDB lowLevelClient = AmazonDynamoDBClientBuilder.standard()
        .withRegion(Regions.US_WEST_2)
        .withCredentials(new ProfileCredentialsProvider("shane")).build();
    
    IDynamoDBMapper mapper = new DynamoDBMapper(lowLevelClient);
    DynamoDBImporter p = new DynamoDBImporter(mapper);

    p.importData();

    log.info("Completed in {} ms", (System.currentTimeMillis() - start));
  }
}
