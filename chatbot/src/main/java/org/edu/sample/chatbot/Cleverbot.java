package org.edu.sample.chatbot;

import net.sf.json.JSON;
import net.sf.json.JSONObject;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class Cleverbot {
    private final String token;
    private final String serviceUrlTemplate ="https://www.cleverbot.com/getreply?key=%s";
    private final String serviceUrlFullTemplate="https://www.cleverbot.com/getreply?key=%s&cs=%s&input=%s";
    private int endIndex;

    public Cleverbot(String token) {
        this.token = token;
    }


    public Session createSession(Locale... locales) {
        return new Session(token, locales);
    }

    public class Session {
        private final String token;
        private volatile String currentState;
        private final Map<String, String> headers;
        private final Map<String, String> cookies;

        public Session(String token, Locale... locales) {
            this.token =token;
            headers = new LinkedHashMap<String, String>();
            if (locales.length > 0)
                headers.put("Accept-Language", Utils.toAcceptLanguageTags(locales));
            cookies = new LinkedHashMap<String, String>();
            try {
                String response = Utils.request(String.format(serviceUrlTemplate,token), headers, cookies,null) ;
                if (response != null) {
                    JSONObject json = JSONObject.fromObject(response);
                    currentState = json.getString("cs");
                    json.getString("output");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public BotThought think(BotThought thought) throws Exception {
            BotThought responseThought = new BotThought();
            Map<String, String> params=new HashMap<String, String>();
            params.put("key", token);
            params.put("input", URLEncoder.encode(thought.getMsg(),"UTF-8"));
            params.put("cs", currentState);

            String request = String.format(serviceUrlFullTemplate, token, params.get("cs"), params.get("input"));
            try {
                String response = Utils.request(request, headers, cookies, null);
                if (response != null) {
                    JSONObject json = JSONObject.fromObject(response);
                    currentState = json.getString("cs");
                    responseThought.setMsg(json.getString("output"));
                }
            } catch (Exception e) {
                responseThought.setMsg("Error!");
            }

            return responseThought;
        }

        public synchronized String think(String text) throws Exception {
            BotThought thought = new BotThought();
            thought.setMsg(text);
            return think(thought).getMsg();
        }
    }
}