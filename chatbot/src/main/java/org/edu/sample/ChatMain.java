package org.edu.sample;

import org.apache.log4j.Logger;
import org.edu.sample.gmail.GMailSender;
import org.edu.sample.gmail.MailNotifier;
import org.edu.sample.telegram.MyTelegramBot;
import org.edu.sample.telegram.botapi.TelegramBot;
import org.edu.sample.telegram.botapi.requests.ApiResponse;
import org.edu.sample.telegram.botapi.types.User;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatMain {
    private final static Logger log = Logger.getLogger(ChatMain.class);
    private final static SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public static void main(String[] args) {
        final MailNotifier mailNotifier = new GMailSender();
        final TelegramBot bot = new MyTelegramBot(mailNotifier);

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                mailNotifier.sendMessage("BOT NOTIFIER", "Shutting down bot...");
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
        mailNotifier.sendMessage("BOT NOTIFIER", "Bot started : Time = " + DF.format(new Date()) + "\n ID = " + resp.getResult().getId() + "\n Name = " + resp.getResult().getUsername());
    }


}
