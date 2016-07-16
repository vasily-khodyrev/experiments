package org.edu.sample.telegram;

import org.edu.sample.chatbot.BotFactory;
import org.edu.sample.chatbot.Cleverbot;
import org.edu.sample.telegram.botapi.CommandHandler;
import org.edu.sample.telegram.botapi.DefaultHandler;
import org.edu.sample.telegram.botapi.MessageHandler;
import org.edu.sample.telegram.botapi.TelegramBot;
import org.edu.sample.telegram.botapi.types.Message;

import java.nio.charset.Charset;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by
 * User: Vasily Khodyrev
 * Date: 15.07.2016
 */
public class MyTelegramBot extends TelegramBot {
    private Cleverbot bot;

    private ConcurrentHashMap<String, Cleverbot.Session> chatOn = new ConcurrentHashMap<>();

    public MyTelegramBot() {
        super(System.getProperty("token"));
        bot = BotFactory.createCleverBot();
    }

    // This handler gets called whenever a user sends /start or /help
    @CommandHandler({"start", "help", "chaton", "chatoff"})
    public void handleCommands(Message message) {
        System.out.println(message);
        switch (message.getText()) {
            case "/start": {
                replyTo(message, "Ну жамкнул ты на старт и чего?");
                break;
            }
            case "/help": {
                replyTo(message, "/chaton - включить разговор\n/chatoff - выключить разговор");
                break;
            }
            case "/chaton": {
                String uId = "" + message.getFrom().getId();
                if (!chatOn.containsKey(uId)) {
                    chatOn.putIfAbsent(uId, bot.createSession(new Locale("ru", "RU"), Locale.ENGLISH));
                } else {
                    replyTo(message, "Да я и так с тобой разговариваю )");
                }
                break;
            }
            case "/chatoff": {
                String uId = "" + message.getFrom().getId();
                if (chatOn.containsKey(uId)) {
                    chatOn.remove(uId);
                    replyTo(message, "Больше с тобой не разговариваю...");
                } else {
                    replyTo(message, "Да я молчу молчу...");
                }
                break;
            }
        }
    }

    // This handler gets called whenever a user sends a general text message.
    @MessageHandler(contentTypes = Message.Type.TEXT)
    public void handleTextMessage(Message message) {
        if (!message.getText().startsWith("/")) {
            System.out.println(String.format("%s(%s): %s", message.getFrom().getFirstName(), message.getChat().getId(), message.getText()));
            System.out.println(message);
            try {
                String uId = "" + message.getFrom().getId();
                if (chatOn.containsKey(uId)) {
                    Cleverbot.Session botSession = chatOn.get(uId);
                    String reply = botSession.think(message.getText());
                    System.out.println("Reply: " + reply);
                    replyTo(message, reply);
                } else {
                    replyTo(message,"я молчу.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    // This is the default handler, called when the other two handlers don't apply.
    @DefaultHandler
    public void handleDefault(Message message) {
        System.out.println("DEFAULT:" + message);
        //replyTo(message, "Say what?");
    }
}
