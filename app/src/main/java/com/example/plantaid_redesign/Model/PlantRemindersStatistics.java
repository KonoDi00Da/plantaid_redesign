package com.example.plantaid_redesign.Model;

public class PlantRemindersStatistics {
    public int water, repot, fertilize, custom;

    public PlantRemindersStatistics(int water, int repot, int fertilize, int custom) {
        this.water = water;
        this.repot = repot;
        this.fertilize = fertilize;
        this.custom = custom;
    }

    public int getWater() {
        return water;
    }

    public void setWater(int water) {
        this.water = water;
    }

    public int getRepot() {
        return repot;
    }

    public void setRepot(int repot) {
        this.repot = repot;
    }

    public int getFertilize() {
        return fertilize;
    }

    public void setFertilize(int fertilize) {
        this.fertilize = fertilize;
    }

    public int getCustom() {
        return custom;
    }

    public void setCustom(int custom) {
        this.custom = custom;
    }
}
