package com.sotirelischristos.receipts.helper;

public class Place {
    public int id;
    public String title;
    public String distance;

    public Place() {
    }

    public Place(int id, String title, String distance) {
        this.distance = distance;
        this.title = title;
        this.id = id;
    }

}
