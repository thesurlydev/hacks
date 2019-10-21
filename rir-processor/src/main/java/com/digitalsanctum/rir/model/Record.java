package com.digitalsanctum.rir.model;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * Derived from <a href="https://www.apnic.net/about-apnic/corporate-documents/documents/resource-guidelines/rir-statistics-exchange-format/#RecordFormat/">https://www.apnic.net/about-apnic/corporate-documents/documents/resource-guidelines/rir-statistics-exchange-format/#RecordFormat/</a>
 *
 * @author Shane Witbeck
 * @since 5/29/17
 */
public class Record {

  public Record() {
  }
  
  public Record(String[] arr) {
    setRegistry(arr[0]);
    setCc(arr[1]);
    setType(arr[2]);
    setStart(arr[3]);
    setValue(arr[4]);
    setDate(arr[5]);
    setStatus(arr[6]);
    setExtensions(arr[7]);
  }

  /**
   * One value from the set of defined strings:
   * <p>
   * {afrinic,apnic,arin,iana,lacnic,ripencc}
   */
  private String registry;

  /**
   * ISO 3166 2-letter code of the organization to which the allocation or assignment was made.
   */
  private String cc;

  /**
   * Type of Internet number resource represented in this record. One value from the set of defined strings:
   * <p>
   * {asn,ipv4,ipv6}
   */
  private String type;

  /**
   * In the case of records of type ‘ipv4’ or ‘ipv6’ this is the IPv4 or IPv6 ‘first address’ of the range.
   * <p>
   * In the case of an 16 bit AS number the format is the integer
   * value in the range 0 to 65535, in the case of a 32 bit ASN the value is
   * in the range 0 to 4294967296. No distinction is drawn between 16 and 32
   * bit ASN values in the range 0 to 65535
   */
  private String start;

  /**
   * In the case of IPv4 address the count of hosts for this range. This count does not have to represent a CIDR range.
   * In the case of an IPv6 address the value will be the CIDR prefix length from the ‘first address’ value of '<start>'.
   * In the case of records of type ‘asn’ the number is the count of AS from this start value.
   */
  private String value;

  /**
   * Date on this allocation/assignment was made by the RIR in the format YYYYMMDD
   */
  private String date;

  /**
   * Type of allocation from the set: This is the allocation or assignment made by the registry producing the file and
   * not any sub-assignment by other agencies.
   */
  private String status;

  /**
   * Any extra data on a line is undefined, but should conform to use of the field separator used above.
   */
  private String extensions;

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


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Record that = (Record) o;

    return Objects.equals(this.cc, that.cc) &&
        Objects.equals(this.date, that.date) &&
        Objects.equals(this.extensions, that.extensions) &&
        Objects.equals(this.registry, that.registry) &&
        Objects.equals(this.start, that.start) &&
        Objects.equals(this.status, that.status) &&
        Objects.equals(this.type, that.type) &&
        Objects.equals(this.value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cc, date, extensions, registry, start, status,
        type, value);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", this.getClass().getSimpleName() + "[", "]")
        .add("cc = " + cc)
        .add("date = " + date)
        .add("extensions = " + extensions)
        .add("registry = " + registry)
        .add("start = " + start)
        .add("status = " + status)
        .add("type = " + type)
        .add("value = " + value)
        .toString();
  }
}
