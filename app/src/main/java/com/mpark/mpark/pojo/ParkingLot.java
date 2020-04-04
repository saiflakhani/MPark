package com.mpark.mpark.pojo;

/**
 * Created by saif on 2018-01-15.
 */

public class ParkingLot {
    String latitude,longitude;

    public ParkingLot()
    {}

    public ParkingLot(String id,String name ,String mac){
        setId(id);
        setName(name);
        setMacAddress(mac);
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    String macAddress;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    String id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    String name;
}
