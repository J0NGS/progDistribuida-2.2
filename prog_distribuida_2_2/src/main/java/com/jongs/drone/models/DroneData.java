package com.jongs.drone.models;

import java.io.Serializable;

public class DroneData implements Serializable{
    private static final long serialVersionUID = 1L;
    private String droneName;
    private String dateTime;
    private String pressure;
    private String solarRadiation;
    private String temperature;
    private String humidity;

    public DroneData(String droneName, String dateTime, String pressure, String solarRadiation, String temperature, String humidity) {
        this.droneName = droneName;
        this.dateTime = dateTime;
        this.pressure = pressure;
        this.solarRadiation = solarRadiation;
        this.temperature = temperature;
        this.humidity = humidity;
    }

    @Override
    public String toString() {
        return "------------------------------------------------------ \n" +
                "-> Drone name: " + droneName + "\n" +
                "------------------------------------------------------ \n" +
               "Date and Time: " + dateTime + "\n" +
               "Pressure: " + pressure + " hPa\n" +
               "Solar Radiation: " + solarRadiation + " W/m²\n" +
               "Temperature: " + temperature + " °C\n" +
               "Humidity: " + humidity + " %\n" +
               "------------------------------------------------------ \n";
    }


    public String getDroneName() {
        return this.droneName;
    }

    public void setDroneName(String droneName) {
        this.droneName = droneName;
    }

    public String getDateTime() {
        return this.dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getPressure() {
        return this.pressure;
    }

    public void setPressure(String pressure) {
        this.pressure = pressure;
    }

    public String getSolarRadiation() {
        return this.solarRadiation;
    }

    public void setSolarRadiation(String solarRadiation) {
        this.solarRadiation = solarRadiation;
    }

    public String getTemperature() {
        return this.temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getHumidity() {
        return this.humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }
}
