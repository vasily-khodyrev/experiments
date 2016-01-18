package org.edu.rabbitmq.helper;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.http.auth.Credentials;
import org.edu.rabbitmq.WebClientConnector;
import org.edu.rabbitmq.beans.Binding;
import org.edu.rabbitmq.beans.Exchange;

import java.io.IOException;
import java.util.*;

/**
 * Created by
 * User: vkhodyre
 * Date: 1/13/2016
 */
public class RestMngRabbitHelper {
    private final static String RABBIT_MNG_URL_TEMPLATE = "http://%s:15672";
    private final static String DEFAULT_HOST = "%2f";

    private final static String EXCHANGE_ALL_TEMPLATE = "/api/exchanges/%s/";
    private final static String BINDINGS_ALL_TEMPLATE = "/api/bindings/%s/";

    public static List<Exchange> getAllExchanges(String rabbitUrl, Credentials cr) throws IOException {
        try {
            JSON json = getMngRequest(rabbitUrl, String.format(EXCHANGE_ALL_TEMPLATE, DEFAULT_HOST), cr);
            return parseExchanges(json);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static List<Binding> getAllBindings(String rabbitUrl, Credentials cr) throws IOException {
        try {
            JSON json = getMngRequest(rabbitUrl, String.format(BINDINGS_ALL_TEMPLATE, DEFAULT_HOST), cr);
            return parseBindings(json);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static Map<String, Set<String>> getExchangeRoutingKeys(List<Binding> bindings) {
        Map<String, Set<String>> result = new HashMap<>();
        for (Binding b : bindings) {
            if (!StringUtils.isBlank(b.getSource())) {
                String src = b.getSource();
                Set<String> rk = result.get(src);
                if (rk == null) {
                    rk = new HashSet<>();
                }
                rk.add(b.getRoutingKey());
                result.put(src,rk);
            }
        }
        return result;
    }

    private static JSON getMngRequest(String rabbitAddress, String apiUrl, Credentials cr) throws IOException {
        String fullRabbitMngUrl = String.format(RABBIT_MNG_URL_TEMPLATE, rabbitAddress);
        String fullApiRequestUrl = fullRabbitMngUrl + apiUrl;
        return WebClientConnector.requestJson(fullApiRequestUrl, cr);
    }

    private static List<Binding> parseBindings(JSON json) {
        List<Binding> result = new LinkedList<>();
        if (json instanceof JSONArray) {
            JSONArray ja = (JSONArray) json;
            for (Object o : ja) {
                if (o instanceof JSONObject) {
                    JSONObject job = (JSONObject) o;
                    String source = job.getString("source");
                    String vhost = job.getString("vhost");
                    String destination = job.getString("destination");
                    String destination_type = job.getString("destination_type");
                    String routing_key = job.getString("routing_key");
                    result.add(new Binding(source, vhost, destination, destination_type, routing_key));
                }
            }
        }
        return result;
    }

    private static List<Exchange> parseExchanges(JSON json) {
        List<Exchange> result = new LinkedList<>();
        if (json instanceof JSONArray) {
            JSONArray ja = (JSONArray) json;
            for (Object o : ja) {
                if (o instanceof JSONObject) {
                    JSONObject job = (JSONObject) o;
                    String name = job.getString("name");
                    String vhost = job.getString("vhost");
                    String type = job.getString("type");
                    String policy = job.getString("policy");
                    result.add(new Exchange(name, vhost, type, policy));
                }
            }
        }
        return result;
    }
}
