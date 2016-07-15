package org.edu.sample.telegram;

import org.edu.sample.telegram.botapi.CommandHandler;
import org.edu.sample.telegram.botapi.DefaultHandler;
import org.edu.sample.telegram.botapi.MessageHandler;
import org.edu.sample.telegram.botapi.TelegramBot;
import org.edu.sample.telegram.botapi.types.Message;

/**
 * Created by
 * User: Vasily Khodyrev
 * Date: 15.07.2016
 */
public class MyTelegramBot extends TelegramBot {

    public MyTelegramBot() {
        super(System.getProperty("token"));
    }

    // This handler gets called whenever a user sends /start or /help
    @CommandHandler({"start", "help"})
    public void handleHelp(Message message) {
        replyTo(message, "Hi there! I am here to echo all your kind words back to you!");
    }

    // This handler gets called whenever a user sends a general text message.
    @MessageHandler(contentTypes = Message.Type.TEXT)
    public void handleTextMessage(Message message) {
        System.out.println(String.format("%s: %s", message.getChat().getId(), message.getText()));
        replyTo(message, message.getText());
    }

    // This is the default handler, called when the other two handlers don't apply.
    @DefaultHandler
    public void handleDefault(Message message) {
        replyTo(message, "Say what?");
    }
}
