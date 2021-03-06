package org.edu.sample.telegram;

import org.apache.log4j.Logger;
import org.edu.sample.chatbot.BotFactory;
import org.edu.sample.chatbot.Cleverbot;
import org.edu.sample.gmail.MailNotifier;
import org.edu.sample.telegram.botapi.CommandHandler;
import org.edu.sample.telegram.botapi.DefaultHandler;
import org.edu.sample.telegram.botapi.MessageHandler;
import org.edu.sample.telegram.botapi.TelegramBot;
import org.edu.sample.telegram.botapi.types.Message;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by
 * User: Vasily Khodyrev
 * Date: 15.07.2016
 */
public class MyTelegramBot extends TelegramBot {
    private final static Logger log = Logger.getLogger(MyTelegramBot.class);
    private final static SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private final MailNotifier mailNotifier;
    private Cleverbot bot;

    private ConcurrentHashMap<String, Cleverbot.Session> chatOn = new ConcurrentHashMap<>();

    public MyTelegramBot(MailNotifier mailNotifier, String token) {
        super(token);
        bot = BotFactory.createCleverBot();
        this.mailNotifier = mailNotifier;
    }

    // This handler gets called whenever a user sends /start or /help
    @CommandHandler({"start", "help", "chaton", "chatoff"})
    public void handleCommands(Message message) {
        log.info("Command received: " + message);
        switch (message.getText()) {
            case "/start": {
                replyToAndLog(message, "Ну жамкнул ты на старт и чего? Давай чат включай!");
                break;
            }
            case "/help": {
                replyToAndLog(message, "/chaton - включить разговор\n/chatoff - выключить разговор\n В любом случае все логи читаются по вечерам )");
                break;
            }
            case "/chaton": {
                String uId = "" + message.getFrom().getId();
                if (!chatOn.containsKey(uId)) {
                    chatOn.putIfAbsent(uId, bot.createSession(new Locale("ru", "RU"), Locale.ENGLISH));
                    replyToAndLog(message, "Я Вас слушаю!");
                } else {
                    replyToAndLog(message, "Да я и так с тобой разговариваю )");
                }
                break;
            }
            case "/chatoff": {
                String uId = "" + message.getFrom().getId();
                if (chatOn.containsKey(uId)) {
                    chatOn.remove(uId);
                    replyToAndLog(message, "Больше с тобой не разговариваю...");
                } else {
                    replyToAndLog(message, "Да я молчу молчу...");
                }
                break;
            }
        }
    }

    // This handler gets called whenever a user sends a general text message.
    @MessageHandler(contentTypes = Message.Type.TEXT)
    public void handleTextMessage(Message message) {
        if (!message.getText().startsWith("/")) {
            log.info("Message received: " + message);
            log.info(String.format("%s(%s): %s", message.getFrom().getFirstName(), message.getChat().getId(), message.getText()));
            try {
                String uId = "" + message.getFrom().getId();
                if (chatOn.containsKey(uId)) {
                    Cleverbot.Session botSession = chatOn.get(uId);
                    String reply = botSession.think(message.getText());
                    replyToAndLog(message, reply);
                } else {
                    replyToAndLog(message, "я молчу. хочешь поговорить - включи чат.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    // This is the default handler, called when the other two handlers don't apply.
    @DefaultHandler
    public void handleDefault(Message message) {
        log.info("DEFAULT handler:" + message);
        replyToAndLog(message, "Say what?");
    }

    private void replyToAndLog(Message message, String text) {
        String received = String.format("From: mId(%s) %s(%s): %s", message.getMessageId(), message.getFrom().getFirstName(), message.getChat().getId(), message.getText());
        String reply = String.format("Reply mId(%s): %s", message.getMessageId(), text);
        ChatLog.log.info(received);
        ChatLog.log.info(reply);
        replyTo(message, text);
        mailNotifier.sendMessage("Message", "DATE: " + DF.format(new Date()) + "\n\n" + received + "\n\n" + reply);

    }

}
