package com.jongs.serverConsumer;


import com.rabbitmq.client.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeoutException;

public class ServerConsumer implements Runnable {
    private String host;
    private Connection connection;
    private Channel channel;
    private static final String EXCHANGE_NAME = "drone_data_exchange";
    private static final String QUEUE_NAME = "drone_data_queue";
    private ConcurrentHashMap<String, Map<String, String>> droneDataMap;
    private ConcurrentHashMap<String, List<String>> clientMessageQueues;
    private MulticastSocket clientRegistrationSocket;
    private MulticastSocket messageDistributionSocket;
    private InetAddress group;
    private static final int REGISTRATION_PORT = 4446;
    private static final int MESSAGE_PORT = 4447;

    public ServerConsumer(String host) {
        this.host = host;
        this.droneDataMap = new ConcurrentHashMap<>();
        this.clientMessageQueues = new ConcurrentHashMap<>();
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(host);
            this.connection = factory.newConnection();
            this.channel = connection.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME, "topic");
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "drones.#");

            // Setup multicast sockets
            this.clientRegistrationSocket = new MulticastSocket(REGISTRATION_PORT);
            this.messageDistributionSocket = new MulticastSocket();
            this.group = InetAddress.getByName("230.0.0.0");
            clientRegistrationSocket.joinGroup(group);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        new Thread(this::handleClientRegistrations).start();
        while (true) {
            try {
                consumeData();
                Thread.sleep(3000); // Espera 3 segundos antes de consumir novamente
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleClientRegistrations() {
        byte[] buf = new byte[256];
        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                clientRegistrationSocket.receive(packet);
                String clientName = new String(packet.getData(), 0, packet.getLength());
                clientMessageQueues.putIfAbsent(clientName, new CopyOnWriteArrayList<>());
                System.out.println("Client registered: " + clientName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void consumeData() throws IOException {
        while (true) {
            GetResponse response = channel.basicGet(QUEUE_NAME, true);
            if (response == null) {
                break; // Sai do loop se não houver mais mensagens
            }
            String message = new String(response.getBody(), "UTF-8");
            String routingKey = response.getEnvelope().getRoutingKey();
            System.out.println("Received '" + routingKey + "':'" + message + "'");

            String[] parts = routingKey.split("\\.");
            if (parts.length == 3) {
                String droneName = parts[1];
                String attribute = parts[2];

                droneDataMap.putIfAbsent(droneName, new ConcurrentHashMap<>());
                droneDataMap.get(droneName).put(attribute, message);

                // Distribute message to all clients
                for (Map.Entry<String, List<String>> entry : clientMessageQueues.entrySet()) {
                    entry.getValue().add(message);
                }

                // Send message to multicast group
                byte[] buf = message.getBytes();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, group, MESSAGE_PORT);
                messageDistributionSocket.send(packet);
            }
        }
    }

    public void close() {
        try {
            if (this.channel != null) {
                this.channel.close();
            }
            if (this.connection != null) {
                this.connection.close();
            }
            if (this.clientRegistrationSocket != null) {
                this.clientRegistrationSocket.leaveGroup(group);
                this.clientRegistrationSocket.close();
            }
            if (this.messageDistributionSocket != null) {
                this.messageDistributionSocket.close();
            }
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter RabbitMQ host: ");
        String host = scanner.nextLine();

        ServerConsumer serverConsumer = new ServerConsumer(host);
        Thread consumerThread = new Thread(serverConsumer);
        consumerThread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(serverConsumer::close));
    }
}