package org.edu.sample;

import org.edu.sample.chatbot.BotFactory;
import org.edu.sample.chatbot.Cleverbot;

import java.util.Locale;

public class ChatMain {
    public static void main(String[] args) {
        try {
            Cleverbot bot1 = BotFactory.createCleverBot();
            Cleverbot.Session bot1session = bot1.createSession(new Locale("ru", "RU"), Locale.ENGLISH);
            Cleverbot.Session bot2session = bot1.createSession(new Locale("ru", "RU"), Locale.ENGLISH);

            String s = "Привет";
            for (int i = 0; i < 100; i++) {

                System.out.println("bot1> " + s);

                s = bot2session.think(s);
                System.out.println("bot2> " + s);

                s = bot1session.think(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
