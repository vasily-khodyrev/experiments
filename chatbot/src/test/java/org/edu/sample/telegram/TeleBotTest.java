package org.edu.sample.telegram;

import org.edu.sample.telegram.botapi.TelegramBot;
import org.junit.Test;

/**
 * Created by
 * User: Vasily Khodyrev
 * Date: 15.07.2016
 */
public class TeleBotTest {

    @Test
    public void testBot() {
        TelegramBot bot = new MyTelegramBot();
    }
}
