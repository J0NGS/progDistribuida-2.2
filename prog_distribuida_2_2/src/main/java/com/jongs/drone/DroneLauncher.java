package com.jongs.drone;

import java.util.Scanner;

import com.jongs.drone.mesaging.SendDroneData;
import com.jongs.drone.models.Drone;

public class DroneLauncher {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter RabbitMQ host: ");
        String host = scanner.nextLine();

        try {
            SendDroneData testConnection = new SendDroneData(host);
            testConnection.close();
            System.out.println("Connection successful!");

            System.out.print("Enter Drone name: ");
            String droneName = scanner.nextLine();

            Drone drone = new Drone(host, droneName);
            Thread droneThread = new Thread(drone);
            droneThread.start();
        } catch (Exception e) {
            System.err.println("Failed to connect to RabbitMQ: " + e.getMessage());
        }

        scanner.close();
    }
}