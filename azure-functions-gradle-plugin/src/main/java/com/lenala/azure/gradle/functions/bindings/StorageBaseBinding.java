package com.lenala.azure.gradle.functions.bindings;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public abstract class StorageBaseBinding extends BaseBinding {
    private String connection = "";

    protected StorageBaseBinding(String name, String type, String direction, String dataType) {
        super(name, type, direction, dataType);
    }

    @JsonGetter
    public String getConnection() {
        return connection;
    }

    public void setConnection(String connection) {
        this.connection = connection;
    }
}
