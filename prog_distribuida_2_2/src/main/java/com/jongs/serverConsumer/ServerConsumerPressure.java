package com.jongs.serverConsumer;

import com.jongs.drone.models.DroneData;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerConsumerPressure implements Runnable {
    private ConcurrentHashMap<String, Map<String, String>> droneDataMap;
    private ConcurrentHashMap<String, List<String>> clientMessageQueues;
    private MulticastSocket messageDistribution;
    private MulticastSocket pressureSocket;
    private InetAddress group;
    private InetAddress pressureGroup;
    private int messagePort;
    private int pressurePort;

    public ServerConsumerPressure(String multicastGroupAddress, int messagePort, int pressurePort) {
        this.droneDataMap = new ConcurrentHashMap<>();
        this.clientMessageQueues = new ConcurrentHashMap<>();
        this.messagePort = messagePort;
        this.pressurePort = pressurePort;
        try {
            this.messageDistribution = new MulticastSocket(this.messagePort);
            this.pressureSocket = new MulticastSocket(pressurePort);
            this.group = InetAddress.getByName(multicastGroupAddress);
            this.pressureGroup = InetAddress.getByName(multicastGroupAddress);
            messageDistribution.joinGroup(group);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        byte[] buf = new byte[256];
        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                messageDistribution.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                if (isPressureMessage(received)) {
                    System.out.println(received);
                    saveMessage(received);
                    DatagramPacket pressurePacket = new DatagramPacket(
                        received.getBytes(), received.length(), pressureGroup, pressurePort);
                    pressureSocket.send(pressurePacket);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isPressureMessage(String message) {
        return message.contains("pressure");
    }

    private void saveMessage(String message) {
        DroneData droneData = parseDroneData(message);
        String droneName = droneData.getDroneName();
        Map<String, String> data = droneDataMap.getOrDefault(droneName, new ConcurrentHashMap<>());
        data.put("lastMessage", message);
        droneDataMap.put(droneName, data);
    }

    private DroneData parseDroneData(String message) {
        // Regex para extrair os dados da mensagem
        Pattern pattern = Pattern.compile("\\[ (.*?) \\] ::=---=:: (.*?) => (.*?): (.*?)$");
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            String timestamp = matcher.group(1);
            String droneName = matcher.group(2);
            String dataType = matcher.group(3).trim();
            String dataValue = matcher.group(4);

            if ("pressure".equals(dataType)) {
                return new DroneData(droneName, timestamp, dataValue, null, null, null);
            } else {
                throw new IllegalArgumentException("Unknown data type: " + dataType);
            }
        } else {
            throw new IllegalArgumentException("Message format is incorrect: " + message);
        }
    }

    public void close() {
        try {
            if (this.messageDistribution != null) {
                this.messageDistribution.leaveGroup(group);
                this.messageDistribution.close();
            }
            if (this.pressureSocket != null) {
                this.pressureSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter multicast group address: ");
        String multicastGroupAddress = scanner.nextLine();
        System.out.print("Enter message port: ");
        int messagePort = scanner.nextInt();
        System.out.print("Enter pressure port: ");
        int pressurePort = scanner.nextInt();

        ServerConsumerPressure server = new ServerConsumerPressure(multicastGroupAddress, messagePort, pressurePort);
        Thread serverThread = new Thread(server);
        serverThread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(server::close));
    }
}