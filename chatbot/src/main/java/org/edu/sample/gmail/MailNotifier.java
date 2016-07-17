package org.edu.sample.gmail;

/**
 * Created by
 * User: Vasily Khodyrev
 * Date: 17.07.2016
 */
public interface MailNotifier {
    public void sendMessage(final String subject, final String msg);
}
