package com.example.youdo;


public class Type {
    private int typeId;
    private String name;
    private String colour;

    // functions

    public Type(String name, String colour) {
        this.name = name;
        this.colour = colour;
    }
    public int getTypeId() {
        return typeId;
    }
    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }
    public String getName() {
        return name;
    }
    public void setName( String name) {
        this.name = name;
    }
    public String getColour() {
        return colour;
    }
    public void setColour(String colour) {
        this.colour = colour;
    }
}
