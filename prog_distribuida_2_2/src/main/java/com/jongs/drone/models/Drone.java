package com.jongs.drone.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import com.jongs.drone.mesaging.SendDroneData;

public class Drone implements Runnable {
    private SendDroneData sendDroneData;
    private String droneName;

    public Drone(String host, String droneName) {
        this.sendDroneData = new SendDroneData(host);
        this.droneName = droneName;
    }

    @Override
    public void run() {
        Random random = new Random();
        Thread.currentThread().setName(droneName);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");
        while (true) {
            try {
                String currentDateTime = dateFormat.format(new Date());
                String pressure = String.valueOf(950 + random.nextInt(100));
                String solarRadiation = String.valueOf(random.nextInt(1000));
                String temperature = String.valueOf(15 + random.nextInt(20));
                String humidity = String.valueOf(30 + random.nextInt(70));

                sendDroneData.sendData("drones." + droneName + ".pressure", "[ " + currentDateTime + " ] ::=---=:: " + droneName + " => pressure : " + pressure);
                System.out.println("send data pressure");
                sendDroneData.sendData("drones." + droneName + ".solarRadiation", "[ " + currentDateTime + " ] ::=---=:: " + droneName + " => solarRadiation : " + solarRadiation);
                System.out.println("send data solarRadiation");
                sendDroneData.sendData("drones." + droneName + ".temperature", "[ " + currentDateTime + " ] ::=---=:: " + droneName + " => temperature : " + temperature);
                System.out.println("send data temperature");
                sendDroneData.sendData("drones." + droneName + ".humidity", "[ " + currentDateTime + " ] ::=---=:: " + droneName + " => humidity : " + humidity);
                System.out.println("send data humidity");

                Thread.sleep(3000); // Simula a coleta de dados a cada 3 segundos
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}