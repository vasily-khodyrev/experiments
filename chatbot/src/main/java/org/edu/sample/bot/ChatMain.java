package org.edu.sample.bot;

import com.google.code.chatterbotapi.BotFactory;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.Cleverbot;

import java.util.Locale;

public class ChatMain {
    public static void main(String[] args) {
        try {
            Cleverbot bot1 = BotFactory.createCleverBot();
            ChatterBotSession bot1session = bot1.createSession(new Locale("ru", "RU"), Locale.ENGLISH);
            ChatterBotSession bot2session = bot1.createSession(new Locale("ru", "RU"), Locale.ENGLISH);

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