package com.agrialert.data_manager;

import java.util.ArrayList;
import java.util.List;

public class FieldsGroup {

    private int id;
    private String name;
    private List<Field> fields;

    public FieldsGroup(int id, String name) {
        fields = new ArrayList<>();
        this.id = id;
        this.name = name;
    }
    public FieldsGroup(int id, String name, List<Field> fields) {
        this.fields = fields;
        this.id = id;
        this.name = name;
    }

    public void addField(Field field) {
        fields.add(field);
    }

    public void addFields(List<Field> fields) {
        this.fields.addAll(fields);
    }

    public List<Field> getFields() {
        return fields;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
