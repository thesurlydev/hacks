package com.digitalsanctum.rir;

import com.digitalsanctum.rir.model.Record;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class PostgreSQLImporter {


  private static final Logger log = LoggerFactory.getLogger(PostgreSQLImporter.class);

  private final String url = "jdbc:postgresql://localhost:5432/postgres";
  private final String user = "switbe";

  /**
   * Connect to the PostgreSQL database
   *
   * @return a Connection object
   */
  public Connection connect() {
    Connection conn = null;
    try {
      conn = DriverManager.getConnection(url, user, null);
      System.out.println("Connected to the PostgreSQL server successfully.");
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
    return conn;
  }
  
  public void importData() {
    String downloadDir = "/Users/switbe/projects/rir-processor/downloads";
    RecordParser parser = new RecordParser();    
    List<Record> records = parser.parse(Paths.get(downloadDir));    
    insert(records);    
  }

  private static final String INSERT_SQL = "INSERT INTO networking.rir (registry, cc, type, start, value, date, status, extensions) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

  private void insert(List<Record> records) {
    try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {

      records.forEach(record -> {
        try {
          ps.setString(1, record.getRegistry());
          ps.setString(2, record.getCc());
          ps.setString(3, record.getType());
          ps.setString(4, record.getStart());
          ps.setString(5, record.getValue());
          ps.setString(6, record.getDate());
          ps.setString(7, record.getStatus());
          ps.setString(8, record.getExtensions());
          
          ps.addBatch();

        } catch (SQLException e) {
          e.printStackTrace();
        }
      });

      ps.clearParameters();
      ps.executeBatch();

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }


  public void setupTables() {
    Flyway flyway = new Flyway();
    flyway.setDataSource(url, user, null);
    flyway.setSchemas("networking");
    flyway.clean(); // just rerun all migrations every time
    flyway.migrate();
  }

  public static void main(String[] args) {

    long start = System.currentTimeMillis();

    PostgreSQLImporter p = new PostgreSQLImporter();

    p.setupTables();
    p.importData();

    log.info("Completed in {} ms", (System.currentTimeMillis() - start));
  }
}
