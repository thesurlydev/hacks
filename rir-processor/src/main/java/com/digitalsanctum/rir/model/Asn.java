package com.digitalsanctum.rir.model;

import java.util.List;
import java.util.Objects;

public class Asn {
    private String number;
    private String name;
    private List<Poc> pocs;

    public Asn(String number, String name, List<Poc> pocs) {
        this.number = number;
        this.name = name;
        this.pocs = pocs;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Poc> getPocs() {
        return pocs;
    }

    public void setPocs(List<Poc> pocs) {
        this.pocs = pocs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Asn asn = (Asn) o;
        return number.equals(asn.number) &&
                name.equals(asn.name) &&
                Objects.equals(pocs, asn.pocs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, name, pocs);
    }

    @Override
    public String toString() {
        return "Asn{" +
                "number='" + number + '\'' +
                ", name='" + name + '\'' +
                ", pocs=" + pocs +
                '}';
    }
}
