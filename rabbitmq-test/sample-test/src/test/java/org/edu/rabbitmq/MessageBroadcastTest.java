package org.edu.rabbitmq;

import com.rabbitmq.client.*;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by
 * User: vkhodyre
 * Date: 9/24/2015
 */
public class MessageBroadcastTest {

    private final static String EXCHANGE_NAME = "logs";


    @Test
    public void testBroadcastReceive() throws TimeoutException, IOException {
        AtomicBoolean finishFlag = new AtomicBoolean(false);
        CountDownLatch cdl = new CountDownLatch(2);
        Thread t1 = new Thread(new Publisher("Publisher", EXCHANGE_NAME, cdl, finishFlag));
        Thread t2 = new Thread(new Receiver("Receiver 1", EXCHANGE_NAME, cdl, finishFlag));
        Thread t3 = new Thread(new Receiver("Receiver 2", EXCHANGE_NAME, cdl, finishFlag));
        t1.start();
        t2.start();
        t3.start();
        try {
            t1.join();
            t2.join();
            t3.join();
        } catch (InterruptedException e) {
            //do nothing
        }
        assertTrue(true);
    }

    private void sleep(int n) {
        try {
            Thread.sleep(n * 1000);
        } catch (InterruptedException e) {
            //do nothing
        }
    }

    class Receiver implements Runnable {
        private final String EXCHANGE;
        private final CountDownLatch cdl;
        private final AtomicBoolean finishFlag;
        private final String myname;

        public Receiver(String name, String exchange, CountDownLatch cdl, AtomicBoolean finishFlag) {
            this.EXCHANGE = exchange;
            this.cdl = cdl;
            this.finishFlag = finishFlag;
            this.myname = name;
        }

        public void run() {
            Channel channel = null;
            Connection connection = null;
            try {
                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost("localhost");
                connection = factory.newConnection();
                channel = connection.createChannel();

                channel.exchangeDeclare(EXCHANGE, "fanout");
                final String queueName = channel.queueDeclare().getQueue();
                channel.queueBind(queueName, EXCHANGE, "");

                Consumer consumer = new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                            throws IOException {
                        String message = new String(body, "UTF-8");
                        System.out.println(myname + " [x] Received queue:" + queueName + " message: '" + message + "'");
                    }
                };
                channel.basicConsume(queueName, true, consumer);
                cdl.countDown();
                while (!finishFlag.get()) {
                    sleep(1);
                }

            } catch (TimeoutException e) {
                e.printStackTrace();
                fail(e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                fail(e.getMessage());
            } finally {
                try {
                    if (channel != null) {
                        channel.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (IOException e) {
                    //sorry
                } catch (TimeoutException e) {
                    //sorry
                }
            }
        }
    }

    class Publisher implements Runnable {
        private final String EXCHANGE;
        private final CountDownLatch cdl;
        private final AtomicBoolean finishFlag;
        private final String myname;

        public Publisher(String name, String exchange, CountDownLatch cdl, AtomicBoolean finishFlag) {
            this.EXCHANGE = exchange;
            this.cdl = cdl;
            this.finishFlag = finishFlag;
            this.myname = name;
        }

        public void run() {
            Channel channel = null;
            Connection connection = null;
            try {
                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost("localhost");
                connection = factory.newConnection();
                channel = connection.createChannel();

                channel.exchangeDeclare(EXCHANGE, "fanout");
                try {
                    cdl.await();
                } catch (InterruptedException e) {
                    return;
                }
                for (int i = 0; i < 10; i++) {
                    String message = "My Message " + i;
                    System.out.println(myname + " [x] Sending '" + message + "'");
                    channel.basicPublish(EXCHANGE, "", null, message.getBytes());
                    sleep(2);
                }
                finishFlag.set(true);
            } catch (TimeoutException e) {
                e.printStackTrace();
                fail(e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                fail(e.getMessage());
            } finally {
                try {
                    if (channel != null) {
                        channel.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (IOException e) {
                    //sorry
                } catch (TimeoutException e) {
                    //sorry
                }
            }
        }
    }
}
