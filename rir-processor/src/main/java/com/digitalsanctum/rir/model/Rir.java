package com.digitalsanctum.rir.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGeneratedTimestamp;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import java.util.Date;
import java.util.Objects;
import java.util.StringJoiner;

import static com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGenerateStrategy.ALWAYS;
import static com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGenerateStrategy.CREATE;

@DynamoDBTable(tableName = "networking.rir")
public class Rir {

  private static final String UNDERSCORE = "_";

  @DynamoDBHashKey
  private String id; // composite of type and start. eg. asn_1234 or ipv4_204.68.168.0

  @DynamoDBAttribute
  @DynamoDBAutoGeneratedTimestamp(strategy = CREATE)
  private Date creationDate;

  @DynamoDBAttribute
  @DynamoDBAutoGeneratedTimestamp(strategy = ALWAYS)
  private Date modificationDate;

  @DynamoDBAttribute
  private String registry;

  @DynamoDBAttribute
  private String cc;

  @DynamoDBAttribute
  private String type;

  @DynamoDBAttribute
  private String start;

  @DynamoDBAttribute
  private String value;

  @DynamoDBAttribute
  private String date;

  @DynamoDBAttribute
  private String status;

  @DynamoDBAttribute
  private String extensions;

  @DynamoDBIndexHashKey(globalSecondaryIndexName = "typeExtensionIndex")
  private String typeExtension; // composite of type and extensions

  public Rir() {
  }

  public Rir(Record r) {
    this.id = r.getType() + UNDERSCORE + r.getStart();
    this.registry = r.getRegistry();
    this.cc = r.getCc();
    this.type = r.getType();
    this.start = r.getStart();
    this.value = r.getValue();
    this.date = r.getDate();
    this.status = r.getStatus();
    this.extensions = r.getExtensions();
    this.typeExtension = r.getType() + UNDERSCORE + r.getExtensions();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public Date getModificationDate() {
    return modificationDate;
  }

  public void setModificationDate(Date modificationDate) {
    this.modificationDate = modificationDate;
  }

  public String getRegistry() {
    return registry;
  }

  public void setRegistry(String registry) {
    this.registry = registry;
  }

  public String getCc() {
    return cc;
  }

  public void setCc(String cc) {
    this.cc = cc;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getStart() {
    return start;
  }

  public void setStart(String start) {
    this.start = start;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getExtensions() {
    return extensions;
  }

  public void setExtensions(String extensions) {
    this.extensions = extensions;
  }

  public String getTypeExtension() {
    return typeExtension;
  }

  public void setTypeExtension(String typeExtension) {
    this.typeExtension = typeExtension;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Rir that = (Rir) o;

    return Objects.equals(this.cc, that.cc) &&
        Objects.equals(this.creationDate, that.creationDate) &&
        Objects.equals(this.date, that.date) &&
        Objects.equals(this.extensions, that.extensions) &&
        Objects.equals(this.id, that.id) &&
        Objects.equals(this.modificationDate, that.modificationDate) &&
        Objects.equals(this.registry, that.registry) &&
        Objects.equals(this.start, that.start) &&
        Objects.equals(this.status, that.status) &&
        Objects.equals(this.type, that.type) &&
        Objects.equals(this.typeExtension, that.typeExtension) &&
        Objects.equals(this.value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cc, creationDate, date, extensions, id, modificationDate,
        registry, start, status, type, typeExtension, value);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", this.getClass().getSimpleName() + "[", "]")
        .add("cc = " + cc)
        .add("creationDate = " + creationDate)
        .add("date = " + date)
        .add("extensions = " + extensions)
        .add("id = " + id)
        .add("modificationDate = " + modificationDate)
        .add("registry = " + registry)
        .add("start = " + start)
        .add("status = " + status)
        .add("type = " + type)
        .add("typeExtension = " + typeExtension)
        .add("value = " + value)
        .toString();
  }
}
