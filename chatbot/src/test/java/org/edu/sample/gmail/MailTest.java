package org.edu.sample.gmail;

import org.junit.Test;

/**
 * Created by
 * User: Vasily Khodyrev
 * Date: 17.07.2016
 */
public class MailTest {
    @Test
    public void testMail() {
        GMailSender mailSender = new GMailSender();

        try {
            mailSender.sendMessage("telegram update", "hello");
            System.out.println("mail send");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
