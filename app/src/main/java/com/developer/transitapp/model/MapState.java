package com.developer.transitapp.model;

import com.google.android.gms.maps.model.LatLng;

public class MapState{

    LatLng latLng;
    float zoom, bearing, tilt;
    int mapType;

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public float getZoom() {
        return zoom;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    public float getBearing() {
        return bearing;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }

    public float getTilt() {
        return tilt;
    }

    public void setTilt(float tilt) {
        this.tilt = tilt;
    }

    public int getMapType() {
        return mapType;
    }

    public void setMapType(int mapType) {
        this.mapType = mapType;
    }

    public MapState(double lat, double lon, float zoom, float bearing, float tilt, int mapType) {
        this.latLng = new LatLng(lat, lon);
        this.zoom = zoom;
        this.bearing = bearing;
        this.tilt = tilt;
        this.mapType = mapType;
    }

}
