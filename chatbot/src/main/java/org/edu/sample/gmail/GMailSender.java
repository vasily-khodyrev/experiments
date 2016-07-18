package org.edu.sample.gmail;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.Loader;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Properties;

/**
 * Created by
 * User: Vasily Khodyrev
 * Date: 17.07.2016
 */
public class GMailSender implements MailNotifier {
    private final static Logger log = Logger.getLogger(GMailSender.class);
    private final static Properties props;
    public static final String GMAIL_PROPERTIES = "gmail.properties";

    static {
        props = new Properties();
        if (!StringUtils.isBlank(System.getProperty("http.proxyhost"))) {
            props.setProperty("proxySet", "true");
            //props.setProperty("http.proxyHost", System.getProperty("http.proxyhost"));
            //props.setProperty("http.proxyPort", System.getProperty("http.proxyport"));
            props.setProperty("socksProxyHost", System.getProperty("http.proxyhost"));
            props.setProperty("socksProxyPort", System.getProperty("http.proxyport"));
        }
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        //props.put("mail.debug","true");
    }

    private String user;
    private String to;
    private Session session;

    public GMailSender() {
        URL url = Loader.getResource(GMAIL_PROPERTIES);

        if (url != null) {
            Properties prop = new Properties();
            try {
                prop.load(new FileInputStream(new File(url.toURI())));
            } catch (Exception e) {
                log.error("Unable to read properties " + GMAIL_PROPERTIES, e);
                throw new RuntimeException(e);
            }
            this.user = prop.getProperty("gmail.user");
            this.to = prop.getProperty("gmail.to");
            final String pwd = prop.getProperty("gmail.password");
            session = Session.getDefaultInstance(props,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(user, pwd);
                        }
                    });
        } else {
            throw new RuntimeException(GMAIL_PROPERTIES + " not found");
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
        if (System.getProperty("skipMail") == null) {
            try {

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(user));
                message.setRecipients(Message.RecipientType.TO,
                        InternetAddress.parse(to));
                message.setSubject(subject);
                message.setText(msg);
                Transport.send(message);
                log.debug("Sending message from:" + user + " to:" + to + " with text: " + msg);
            } catch (MessagingException e) {
                log.error("Error while sending email: ", e);
                throw new RuntimeException(e);
            }
        }
    }

    public void sendMessage(final String subject, final String msg) {
        try {
            sendMessage(to, subject, msg);
        } catch (Exception e) {
            log.error("unable to send message", e);
        }
    }
}

