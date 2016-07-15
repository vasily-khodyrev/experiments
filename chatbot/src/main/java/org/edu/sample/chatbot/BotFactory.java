package org.edu.sample.chatbot;

/**
 * Created by
 * User: Vasily Khodyrev
 * Date: 14.07.2016
 */
public class BotFactory {

    public static Cleverbot createCleverBot() {
        return new Cleverbot("http://www.cleverbot.com", "http://www.cleverbot.com/webservicemin?uc=255", 35);
    }
}
