package com.amee.calculation.service;

import com.rabbitmq.client.*;
import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CalculationClient {

    public static class CalculateTask implements Runnable {

        public void run() {
            try {
                Channel channel = connection.createChannel();
                channel.exchangeDeclare("platform.live.calculation", "direct");
                RpcClient rpcClient = new RpcClient(channel, "platform.live.calculation", "platform.live.calculation");
                for (int i = 0; i < 1000; i++) {
                    System.out.println(rpcClient.stringCall(outbound.toString()));
                }
            } catch (Exception e) {
                System.err.println("Main thread caught exception: " + e);
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    private static Connection connection;
    private static JSONObject outbound;

    public static void main(String[] args) {
        try {
        	ConnectionFactory connectionFactory = new ConnectionFactory();
        	connectionFactory.setHost("127.0.0.1");
        	connectionFactory.setPort(AMQP.PROTOCOL.PORT);
            connection = connectionFactory.newConnection();

            outbound = new JSONObject();
            outbound.put("dataItemUid", "7F0D7F6AE66D");
            JSONObject parameters = new JSONObject();
            parameters.put("numberOwned", "6");
            outbound.put("parameters", parameters);

            long t = -System.currentTimeMillis();

            ExecutorService threadExecutor = Executors.newFixedThreadPool(3);
            threadExecutor.execute(new CalculateTask());
            threadExecutor.execute(new CalculateTask());
            threadExecutor.execute(new CalculateTask());
            threadExecutor.shutdown();
            threadExecutor.awaitTermination(1, TimeUnit.MINUTES);

            System.out.println(t + System.currentTimeMillis());

            connection.close();

        } catch (Exception e) {
            System.err.println("Main thread caught exception: " + e);
            e.printStackTrace();
            System.exit(1);
        }
    }
}
