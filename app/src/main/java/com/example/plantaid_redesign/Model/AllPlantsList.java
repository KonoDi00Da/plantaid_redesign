package com.example.plantaid_redesign.Model;

public class AllPlantsList {
    private String commonName, sciName, description, care, harvest, pestsDiseases, varieties,
            water, ytLink,key;
    private String progress;

    private String image;

    AllPlantsList() {

    }

    public AllPlantsList(String commonName, String sciName, String description, String care, String harvest, String pestsDiseases, String varieties, String water, String ytLink, String image,String key, String progress) {
        this.commonName = commonName;
        this.sciName = sciName;
        this.description = description;
        this.care = care;
        this.harvest = harvest;
        this.pestsDiseases = pestsDiseases;
        this.varieties = varieties;
        this.water = water;
        this.ytLink = ytLink;
        this.image = image;
        this.key = key;
        this.progress = progress;
    }

    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String getSciName() {
        return sciName;
    }

    public void setSciName(String sciName) {
        this.sciName = sciName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCare() {
        return care;
    }

    public void setCare(String care) {
        this.care = care;
    }

    public String getHarvest() {
        return harvest;
    }

    public void setHarvest(String harvest) {
        this.harvest = harvest;
    }

    public String getPestsDiseases() {
        return pestsDiseases;
    }

    public void setPestsDiseases(String pestsDiseases) {
        this.pestsDiseases = pestsDiseases;
    }

    public String getVarieties() {
        return varieties;
    }

    public void setVarieties(String varieties) {
        this.varieties = varieties;
    }

    public String getWater() {
        return water;
    }

    public void setWater(String water) {
        this.water = water;
    }

    public String getYtLink() {
        return ytLink;
    }

    public void setYtLink(String ytLink) {
        this.ytLink = ytLink;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}