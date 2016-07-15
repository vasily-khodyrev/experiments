package org.edu.sample.telegram.botapi;

public interface AbstractChatContextFactory {

    ChatContext createChatContext(int chatId, TelegramBot bot);

}
