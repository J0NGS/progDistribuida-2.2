package com.jongs.serverConsumer;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Scanner;

public class ClientConsumer implements Runnable {
    private String clientName;
    private InetAddress group;
    private MulticastSocket socket;
    private int registrationPort;
    private int messagePort;

    public ClientConsumer(String clientName, String groupAddress, int registrationPort, int messagePort) {
        this.clientName = clientName;
        this.registrationPort = registrationPort;
        this.messagePort = messagePort;
        try {
            this.group = InetAddress.getByName(groupAddress);
            this.socket = new MulticastSocket(messagePort);
            socket.joinGroup(group);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void register() {
        try (MulticastSocket registrationSocket = new MulticastSocket()) {
            byte[] buf = clientName.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, group, registrationPort);
            registrationSocket.send(packet);
            System.out.println("Registered client: " + clientName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        receiveMessages();
    }

    public void receiveMessages() {
        byte[] buf = new byte[256];
        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Received message: " + message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void close() {
        try {
            if (socket != null) {
                socket.leaveGroup(group);
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter multicast group address: ");
        String groupAddress = scanner.nextLine();
        System.out.print("Enter client name: ");
        String clientName = scanner.nextLine();
        System.out.print("Enter registration port: ");
        int registrationPort = scanner.nextInt();
        System.out.print("Enter message port: ");
        int messagePort = scanner.nextInt();

        ClientConsumer clientConsumer = new ClientConsumer(clientName, groupAddress, registrationPort, messagePort);
        clientConsumer.register();

        Runtime.getRuntime().addShutdownHook(new Thread(clientConsumer::close));

        Thread clientThread = new Thread(clientConsumer);
        clientThread.start();
    }
}