package com.example.youdo.Models;


public class Type {
    private int typeId;
    private String name;
    private String colour;
    private int sumTargetMinutes;
    private int sumAchievedMinutes;
    private boolean rewardOverAchievement;
    private int userId;

    // functions

    public Type() {}
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
    public int getSumTargetMinutes() { return sumTargetMinutes;}
    public void setSumTargetMinutes(int sumTargetMinutes) { this.sumTargetMinutes = sumTargetMinutes; }
    public int getSumAchievedMinutes() { return sumAchievedMinutes; }
    public void setSumAchievedMinutes(int sumAchievedMinutes) { this.sumAchievedMinutes = sumAchievedMinutes; }
    public boolean isRewardOverAchievement() { return rewardOverAchievement; }
    public void setRewardOverAchievement(boolean rewardOverAchievement) { this.rewardOverAchievement = rewardOverAchievement; }

    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    @Override
    public String toString() {
        return name; // used in Spinner
    }

}