package org.edu.rabbitmq;

import com.alu.ice.mq.broker.rabbit.SerializationHelper;
import com.rabbitmq.client.*;
import net.sf.json.JSON;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.edu.rabbitmq.beans.Binding;
import org.edu.rabbitmq.beans.Exchange;
import org.edu.rabbitmq.helper.RestMngRabbitHelper;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.fail;

/**
 * Created by
 * User: vkhodyre
 * Date: 1/12/2016
 */
public class GetQueueMessagesTest {

    private final QUEUE_TYPE TEST_EXCHANGE = QUEUE_TYPE.CFG_API;

    //private static final String HOST = "ruspbvm5726.ru.alcatel-lucent.com";
    private static final String HOST = "135.247.57.26";
    private static final int PORT = 5672;
    public static final String MB_LOGIN = "administrator";
    public static final String MB_PWD = "alcatel";

    private final static String QUEUE_NAME = "testrpc";

    static enum QUEUE_TYPE {
        RPC("cms.intercmsapi.rpc", "cms.intercmsapi.rpc"),
        CFG_API("cms.cfgapi", "cms.cfgapi"),
        NETWORKING("cms.networkingapi", "cms.networkingapi"),
        CACHE("openjpa.remote.commit.provider", "amq.gen-Je73WwQwn5kOMFSLZfMB6Q");

        private String exchange;
        private String routingKey;

        QUEUE_TYPE(String exchange, String routingKey) {
            this.exchange = exchange;
            this.routingKey = routingKey;
        }

        public String getExchange() {
            return exchange;
        }

        public String getRoutingKey() {
            return routingKey;
        }
    }

    @Test
    public void testMngRabbit() throws IOException {
        UsernamePasswordCredentials cr = new UsernamePasswordCredentials(MB_LOGIN, MB_PWD);
        List<Exchange> exchanges = RestMngRabbitHelper.getAllExchanges(HOST, cr);
        for (Exchange ex : exchanges) {
            System.out.println(ex);
        }
        List<Binding> bindings = RestMngRabbitHelper.getAllBindings(HOST, cr);
        for (Binding b : bindings) {
            System.out.println(b);
        }
        Map<String,Set<String>> exchangeRoutes = RestMngRabbitHelper.getExchangeRoutingKeys(bindings);
        for (Map.Entry<String,Set<String>> entry : exchangeRoutes.entrySet()) {
            System.out.println("Exchange: " + entry.getKey() + " -> " + entry.getValue());
        }
    }

    @Test
    public void testGetAllMessages() throws TimeoutException, IOException {
        UsernamePasswordCredentials cr = new UsernamePasswordCredentials(MB_LOGIN, MB_PWD);
        List<Exchange> exchanges = RestMngRabbitHelper.getAllExchanges(HOST, cr);
        for (Exchange ex : exchanges) {
            System.out.println(ex);
        }
        List<Binding> bindings = RestMngRabbitHelper.getAllBindings(HOST, cr);
        for (Binding b : bindings) {
            System.out.println(b);
        }
        Map<String,Set<String>> exchangeRoutes = RestMngRabbitHelper.getExchangeRoutingKeys(bindings);
        for (Map.Entry<String,Set<String>> entry : exchangeRoutes.entrySet()) {
            System.out.println("Exchange: " + entry.getKey() + " -> " + entry.getValue());
        }
        Channel channel = null;
        Connection connection = null;
        try {
            connection = getConnection();
            System.out.println("Connection established to : " + HOST + ":" + PORT);
            channel = connection.createChannel();
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            System.out.println("Queue created: " + QUEUE_NAME);
            channel.basicConsume(QUEUE_NAME, true, new MyConsumer(channel));
            System.out.println("Subscribed to queue.");
            for(Map.Entry<String,Set<String>> entry : exchangeRoutes.entrySet()) {
                for(String routingKey : entry.getValue()) {
                    channel.queueBind(QUEUE_NAME, entry.getKey(), routingKey);
                    System.out.println("Queue '" + QUEUE_NAME + "' binded to exchange:'" + entry.getKey() + "' with Routing Key:'" + routingKey + "'");
                }
            }
            System.out.println("Listening...");
            sleep(100);
        } catch (TimeoutException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            if (channel != null) {
                try {
                    System.out.println("Unbinding queue from exchange(s)...");
                    for (Map.Entry<String, Set<String>> entry : exchangeRoutes.entrySet()) {
                        for (String routingKey : entry.getValue()) {
                            channel.queueUnbind(QUEUE_NAME, entry.getKey(), routingKey);
                        }
                    }
                    System.out.println("Deleting queue...");
                    channel.queueDelete(QUEUE_NAME);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("Channel closing...");
                    channel.close();
                }
            }
            if (connection != null) {
                System.out.println("Connection closing...");
                connection.close();
            }
            System.out.println("Test completed.");
        }
    }

    @Test
    public void test() throws TimeoutException, IOException {
        Channel channel = null;
        Connection connection = null;
        try {
            connection = getConnection();
            System.out.println("Connection established to : " + HOST + ":" + PORT);
            channel = connection.createChannel();
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            System.out.println("Queue created: " + QUEUE_NAME);
            channel.basicConsume(QUEUE_NAME, true, new MyConsumer(channel));
            System.out.println("Subscribed to queue.");
            channel.queueBind(QUEUE_NAME, TEST_EXCHANGE.getExchange(), TEST_EXCHANGE.getRoutingKey());
            System.out.println("Queue '" + QUEUE_NAME + "' binded to exchange:'" + TEST_EXCHANGE.getExchange() + "' with Routing Key:'" + TEST_EXCHANGE.getRoutingKey() + "'");
            System.out.println("Listening...");
            sleep(100);
        } catch (TimeoutException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            if (channel != null) {
                try {
                    System.out.println("Unbinding queue from exchange...");
                    channel.queueUnbind(QUEUE_NAME, TEST_EXCHANGE.getExchange(), TEST_EXCHANGE.getRoutingKey());
                    System.out.println("Deleting queue...");
                    channel.queueDelete(QUEUE_NAME);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("Channel closing...");
                    channel.close();
                }
            }
            if (connection != null) {
                System.out.println("Connection closing...");
                connection.close();
            }
            System.out.println("Test completed.");
        }
    }

    private Connection getConnection() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setPort(PORT);
        factory.setUsername(MB_LOGIN);
        factory.setPassword(MB_PWD);
        Connection connection = factory.newConnection();
        return connection;
    }

    private void sleep(int n) {
        try {
            Thread.sleep(n * 1000);
        } catch (InterruptedException e) {
            //do nothing
        }
    }

    public class MyConsumer extends DefaultConsumer {
        public MyConsumer(Channel channel) {
            super(channel);
        }

        public <T> T readObject(byte[] body) throws IOException, ClassNotFoundException {
            ByteArrayInputStream bais = null;
            ObjectInputStream ois = null;
            T result = null;
            try {
                bais = new ByteArrayInputStream(body);
                ois = new ObjectInputStream(bais);
                result = (T) ois.readObject();
                return result;
            } finally {
                if (ois != null) {
                    ois.close();
                }
                if (bais != null) {
                    bais.close();
                }
            }
        }

        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
            synchronized (this) {
                System.out.println("ConsumetTag = " + consumerTag);
                System.out.println("Exchange = " + envelope.getExchange());
                System.out.println("RoutingKey = " + envelope.getRoutingKey());
                System.out.println("Message = " + new String(body, "UTF-8"));
                Map<SerializationHelper.Key, ByteBuffer> datamap = SerializationHelper.getDataMap(body);
                for (SerializationHelper.Key key : datamap.keySet()) {
                    System.out.println("" + key + "=" + SerializationHelper.getString(datamap, key));
                }
                byte[] data = SerializationHelper.getBytes(datamap, SerializationHelper.Key.DATA);
                try {
                    try {
                        Object dataObject = readObject(data);
                        System.out.println("Deserialized: " + getObjectOutput(dataObject));
                    } catch (IOException e) {
                        System.out.println("Not a serialized object - json maybe\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public String getObjectOutput(Object o) throws SecurityException {
            StringBuilder sb = new StringBuilder();
            sb.append("Object class:" + o.getClass().getName() + "\n");
            for (java.lang.reflect.Method m : o.getClass().getDeclaredMethods()) {
                sb.append("Method name:" + m.getName());
                if (m.getName().startsWith("get") && m.getParameterTypes().length == 0) {
                    try {
                        Object res = m.invoke(o);
                        sb.append("->" + res.toString() + "\n");
                    } catch (ReflectiveOperationException e) {
                        // do nothing just tried ;)
                        sb.append("->" + e.getMessage() + "\n");
                    }
                } else {
                    sb.append("\n");
                }
            }
            return sb.toString();
        }
    }
}
