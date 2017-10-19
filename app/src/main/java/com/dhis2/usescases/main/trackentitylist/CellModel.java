package com.dhis2.usescases.main.trackentitylist;

/**
 * Created by frodriguez on 10/19/2017.
 */

public class CellModel {

    private int id;
    private String name;

    public CellModel(int id) {
        this.id = id;
        this.name = "";

    }

    public CellModel(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
