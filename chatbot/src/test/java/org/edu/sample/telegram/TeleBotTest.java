package org.edu.sample.telegram;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.edu.sample.telegram.botapi.TelegramBot;
import org.edu.sample.telegram.botapi.requests.ApiResponse;
import org.edu.sample.telegram.botapi.types.User;
import org.edu.sample.utils.WebClientConnector;
import org.junit.Test;

import java.util.Date;

/**
 * Created by
 * User: Vasily Khodyrev
 * Date: 15.07.2016
 */
public class TeleBotTest {

    private final static Logger log = org.apache.log4j.Logger.getLogger(TeleBotTest.class);

    @Test
    public void testBot() {
        String token = System.getProperty("token", System.getenv("token"));
        try {
            if (!StringUtils.isBlank(token)) {
                String url = "https://api.telegram.org/bot" + token;
                JSON json = WebClientConnector.requestJson(url + "/getupdates", null);
                printMessages(json);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        TelegramBot bot = new MyTelegramBot(null, token);
        bot.start();
        ApiResponse<User>  resp = bot.getMe();
        log.info("Bot         id = " + resp.getResult().getId());
        log.info("Bot   username = " + resp.getResult().getUsername());
        log.info("Bot first name = " + resp.getResult().getFirstName());
        log.info("Bot last name  = " + resp.getResult().getLastName());
        try {
            Thread.sleep(20 * 60 * 1000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bot.stop();
        }
    }

    private void printMessages(JSON json) {
        if (json instanceof JSONObject) {
            JSONObject jo = (JSONObject) json;
            JSONArray ja_result = (JSONArray) jo.get("result");
            for (Object o : ja_result) {
                JSONObject joi = (JSONObject) o;
                StringBuilder sb = new StringBuilder();
                sb.append("Update(" + joi.getString("update_id") + ") ");
                JSONObject jmess = (JSONObject) joi.get("message");
                sb.append("mId=" + jmess.getString("message_id") + " ");
                JSONObject jFrom = (JSONObject) jmess.get("from");
                sb.append("from(" + jFrom.getString("id") + "," + jFrom.getString("first_name") + ")");
                Date date = new Date(jmess.getInt("date") * 1000);
                JSONObject jChat = (JSONObject) jmess.get("chat");
                String chatType = jChat.getString("type");
                String msg = jmess.getString("text");
                sb.append("Type(" + chatType + ")");
                sb.append(": " + msg);
                log.info(sb.toString());
            }
        }
    }
}
