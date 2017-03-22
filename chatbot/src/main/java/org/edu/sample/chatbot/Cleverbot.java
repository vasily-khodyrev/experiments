package org.edu.sample.chatbot;

import net.sf.json.JSON;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class Cleverbot {
    private final String token;
    private final String serviceUrl="https://www.cleverbot.com/getreply";
    private int endIndex;

    public Cleverbot(String token) {
        this.token = token;
    }


    public Session createSession(Locale... locales) {
        return new Session(token, locales);
    }

    public class Session {
        private final String token;
        private final Map<String, String> headers;
        private final Map<String, String> cookies;

        public Session(String token, Locale... locales) {
            this.token =token;
            headers = new LinkedHashMap<String, String>();
            if (locales.length > 0)
                headers.put("Accept-Language", Utils.toAcceptLanguageTags(locales));
            cookies = new LinkedHashMap<String, String>();
            Map<String, String> params=new HashMap<String, String>();
            params.put("key", token);
            try {
                String response = Utils.request(serviceUrl, headers, cookies,params) ;
                if (response != null) {
                    JSON json = JSONObject.fromObject(response);
                    System.out.println(json);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public BotThought think(BotThought thought) throws Exception {

            BotThought responseThought = new BotThought();

            //responseThought.setMsg(Utils.stringAtIndex(responseValues, 0));

            return responseThought;
        }

        public synchronized String think(String text) throws Exception {
            BotThought thought = new BotThought();
            thought.setMsg(text);
            return think(thought).getMsg();
        }
    }
}