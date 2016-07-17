package org.edu.sample;

import org.apache.log4j.Logger;
import org.edu.sample.telegram.MyTelegramBot;
import org.edu.sample.telegram.botapi.TelegramBot;
import org.edu.sample.telegram.botapi.requests.ApiResponse;
import org.edu.sample.telegram.botapi.types.User;

public class ChatMain {
    private final static Logger log = Logger.getLogger(ChatMain.class);

    public static void main(String[] args) {
        final TelegramBot bot = new MyTelegramBot();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                log.info("Shutting down bot...");
                bot.stop();
            }
        }));
        bot.start();
        ApiResponse<User> resp = bot.getMe();
        log.info("Bot         id = " + resp.getResult().getId());
        log.info("Bot   username = " + resp.getResult().getUsername());
        log.info("Bot first name = " + resp.getResult().getFirstName());
        log.info("Bot last name  = " + resp.getResult().getLastName());
    }


}
