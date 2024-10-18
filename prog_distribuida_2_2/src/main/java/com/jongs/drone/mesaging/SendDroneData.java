package com.jongs.drone.mesaging;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class SendDroneData {
    private String host;
    private Connection connection;
    private Channel channel;
    private static final String EXCHANGE_NAME = "drone_data_exchange";
    private static final String QUEUE_NAME = "drone_data_queue";

    public SendDroneData(String host) {
        this.host = host;
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(host);
            this.connection = factory.newConnection();
            this.channel = connection.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME, "topic");
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "drones.#");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendData(String bindingKey, String message) {
        try {
            channel.basicPublish(EXCHANGE_NAME, bindingKey, null, message.getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}