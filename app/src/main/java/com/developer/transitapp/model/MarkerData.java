package com.developer.transitapp.model;

import com.google.android.gms.maps.model.LatLng;

public class MarkerData {

    LatLng latLng;
    String title=null;
    int directionId;
    String routeId;

    public LatLng getLatLng() {
        return latLng;
    }

    public MarkerData(double lat ,double lon, int directionId, String routeId) {
        this.latLng = new LatLng(lat,lon);
        this.directionId = directionId;
        this.routeId = routeId;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getDirectionId() {
        return directionId;
    }

    public void setDirectionId(int directionId) {
        this.directionId = directionId;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }
}
