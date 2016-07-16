package org.edu.sample.chatbot;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class Cleverbot {
    private final String baseUrl;
    private final String serviceUrl;
    private int endIndex;

    public Cleverbot(String baseUrl, String serviceUrl, int endIndex) {
        this.baseUrl = baseUrl;
        this.serviceUrl = serviceUrl;
        this.endIndex = endIndex;
    }


    public Session createSession(Locale... locales) {
        return new Session(locales);
    }

    public class Session {
        private final Map<String, String> vars;
        private final Map<String, String> headers;
        private final Map<String, String> cookies;

        public Session(Locale... locales) {
            vars = new LinkedHashMap<String, String>();
            vars.put("stimulus", "");
            vars.put("islearning", "1");
            vars.put("icognoid", "wsf");

            headers = new LinkedHashMap<String, String>();
            if (locales.length > 0)
                headers.put("Accept-Language", Utils.toAcceptLanguageTags(locales));
            cookies = new LinkedHashMap<String, String>();
            try {
                Utils.request(baseUrl, headers, cookies, null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public BotThought think(BotThought thought) throws Exception {
            vars.put("stimulus", thought.getMsg());

            String formData = Utils.parametersToWWWFormURLEncoded(vars);
            String formDataToDigest = formData.substring(9, endIndex);
            String formDataDigest = Utils.toMD5(formDataToDigest);
            vars.put("icognocheck", formDataDigest);

            String response = Utils.request(serviceUrl, headers, cookies, vars);

            String[] responseValues = response.split("\r");

            //vars.put("", Utils.stringAtIndex(responseValues, 0)); ??
            vars.put("sessionid", Utils.stringAtIndex(responseValues, 1));
            vars.put("logurl", Utils.stringAtIndex(responseValues, 2));
            vars.put("vText8", Utils.stringAtIndex(responseValues, 3));
            vars.put("vText7", Utils.stringAtIndex(responseValues, 4));
            vars.put("vText6", Utils.stringAtIndex(responseValues, 5));
            vars.put("vText5", Utils.stringAtIndex(responseValues, 6));
            vars.put("vText4", Utils.stringAtIndex(responseValues, 7));
            vars.put("vText3", Utils.stringAtIndex(responseValues, 8));
            vars.put("vText2", Utils.stringAtIndex(responseValues, 9));
            vars.put("prevref", Utils.stringAtIndex(responseValues, 10));
            //vars.put("", Utils.stringAtIndex(responseValues, 11)); ??
//            vars.put("emotionalhistory", Utils.stringAtIndex(responseValues, 12));
//            vars.put("ttsLocMP3", Utils.stringAtIndex(responseValues, 13));
//            vars.put("ttsLocTXT", Utils.stringAtIndex(responseValues, 14));
//            vars.put("ttsLocTXT3", Utils.stringAtIndex(responseValues, 15));
//            vars.put("ttsText", Utils.stringAtIndex(responseValues, 16));
//            vars.put("lineRef", Utils.stringAtIndex(responseValues, 17));
//            vars.put("lineURL", Utils.stringAtIndex(responseValues, 18));
//            vars.put("linePOST", Utils.stringAtIndex(responseValues, 19));
//            vars.put("lineChoices", Utils.stringAtIndex(responseValues, 20));
//            vars.put("lineChoicesAbbrev", Utils.stringAtIndex(responseValues, 21));
//            vars.put("typingData", Utils.stringAtIndex(responseValues, 22));
//            vars.put("divert", Utils.stringAtIndex(responseValues, 23));

            BotThought responseThought = new BotThought();

            responseThought.setMsg(Utils.stringAtIndex(responseValues, 0));

            return responseThought;
        }

        public synchronized String think(String text) throws Exception {
            BotThought thought = new BotThought();
            thought.setMsg(text);
            return think(thought).getMsg();
        }
    }
}