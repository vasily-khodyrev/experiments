package org.edu.sample.gmail;

import org.apache.log4j.Logger;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Created by
 * User: Vasily Khodyrev
 * Date: 17.07.2016
 */
public class GMailSender {
    private final static Logger log = Logger.getLogger(GMailSender.class);
    private final static Properties props;

    static {
        props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
    }

    private String user;
    private Session session;

    public GMailSender() {
        File p = new File("./gmailsender.properties");
        if (p.exists() && p.isFile()) {
            Properties prop = new Properties();
            try {
                prop.load(new FileInputStream(p));
            } catch (Exception e) {
                log.error("Unable to read properties.", e);
                throw new RuntimeException(e);
            }
            this.user = prop.getProperty("gmail.user");
            final String pwd = prop.getProperty("gmail.password");
            session = Session.getDefaultInstance(props,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(user, pwd);
                        }
                    });
        }
    }

    public GMailSender(final String user, final String pwd) {
        this.user = user;
        session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(user, pwd);
                    }
                });
    }

    public void sendMessage(final String to, final String subject, final String msg) {

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(user));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(msg);
            Transport.send(message);
        } catch (MessagingException e) {
            log.error("Error while sending email: ", e);
            throw new RuntimeException(e);
        }
    }
}

