package org.edu.sample.chatbot;

/**
 * Created by
 * User: Vasily Khodyrev
 * Date: 14.07.2016
 */
public class BotFactory {

    public static Cleverbot createCleverBot() {
        String bot_token = System.getProperty("bot_token",System.getenv("bot_token"));
        if (bot_token!=null) {
            return new Cleverbot(bot_token);
        } else {
            return null;
        }
    }
}
