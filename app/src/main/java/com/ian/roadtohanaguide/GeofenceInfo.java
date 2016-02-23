package com.ian.roadtohanaguide;

import android.location.Location;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.model.LatLng;

/**
 * The location services geofence class can only store a few parameters
 * This custom class lets me store extra parameters like description, LatLng for the markers, etc
 * When I add a Geofence object to the mGeofences list for GeofencingRequest.Builder I also add a similar GeofenceInfo object
 * to my mGeofenceInfos list, then when we get a broadcast from GeofenceTransitionIntentService we can get additional custom info
 * about the geofence
 */


public class GeofenceInfo {
    private String requestId;
    private String descripition;
    private String mileMarker;
    private String tourSection;
    private int orderForManualTour;
    private String roadName;
    private boolean userCreated;
    private int imageId;
    private LatLng location;
    private double distanceBetween;
    private boolean isHeader;

    //<editor-fold desc="Constructors">
    public GeofenceInfo(boolean isHeader, String requestId) {
        this.isHeader = isHeader;
        this.requestId = requestId;
    }

    public GeofenceInfo(String requestId, String descripition, String mileMarker) {
        this.isHeader = false;
        this.requestId = requestId;
        this.mileMarker = mileMarker;
        this.descripition = descripition;

    }

    public GeofenceInfo(String requestId, String descripition, String mileMarker, LatLng location) {
        this.requestId = requestId;
        this.descripition = descripition;
        this.location = location;
        this.mileMarker = mileMarker;
        this.isHeader = false;

    }
    //</editor-fold>

    //<editor-fold desc="Getters and Setters">
    public String getTourSection() {
        return tourSection;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public void setIsHeader(boolean isHeader) {
        this.isHeader = isHeader;
    }

    public void setTourSection(String tourSection) {
        this.tourSection = tourSection;
    }

    public int getOrderForManualTour() {
        return orderForManualTour;
    }

    public void setOrderForManualTour(int orderForManualTour) {
        this.orderForManualTour = orderForManualTour;
    }

    public String getRoadName() {
        return roadName;
    }

    public void setRoadName(String roadName) {
        this.roadName = roadName;
    }

    public double getDistanceBetween() {
        return distanceBetween;
    }

    public boolean isUserCreated() {
        return userCreated;
    }

    public String getMileMarker() {
        return mileMarker;
    }

    public void setMileMarker(String mileMarker) {
        this.mileMarker = mileMarker;
    }

    public void setUserCreated(boolean userCreated) {
        this.userCreated = userCreated;
    }

    public void setDistanceBetween(double distanceBetween) {
        this.distanceBetween = distanceBetween;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getDescripition() {
        return descripition;
    }

    public void setDescripition(String descripition) {
        this.descripition = descripition;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public String getRequestId() {
        return requestId;
    }
    //</editor-fold>
}
