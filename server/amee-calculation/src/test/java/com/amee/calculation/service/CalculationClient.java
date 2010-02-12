package com.amee.calculation.service;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.RpcClient;
import org.json.JSONObject;

public class CalculationClient {

    public static void main(String[] args) {
        try {
            Connection connection = new ConnectionFactory().newConnection("localhost", AMQP.PROTOCOL.PORT);
            Channel channel = connection.createChannel();
            channel.exchangeDeclare("platform.calculation", "direct");
            RpcClient rpcClient = new RpcClient(channel, "platform.calculation", "platform.calculation");

            JSONObject outbound = new JSONObject();
            outbound.put("dataItemUid", "7F0D7F6AE66D");
            JSONObject parameters = new JSONObject();
            parameters.put("numberOwned", "6");
            outbound.put("parameters", parameters);

            System.out.println(System.currentTimeMillis());
            for (int i = 0; i < 100; i++) {
                System.out.println(rpcClient.stringCall(outbound.toString()));
            }
            System.out.println(System.currentTimeMillis());

            connection.close();
        } catch (Exception e) {
            System.err.println("Main thread caught exception: " + e);
            e.printStackTrace();
            System.exit(1);
        }
    }
}
