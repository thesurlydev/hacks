package com.digitalsanctum.rir.model;

import java.util.Objects;

public class Poc {
    private String name;
    private String type;

    public Poc(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Poc poc = (Poc) o;
        return name.equals(poc.name) &&
                type.equals(poc.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    @Override
    public String toString() {
        return "Poc{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
