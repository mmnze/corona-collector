package de.mmenze.corona.web.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.Properties;

/**
 * The spring approach of sending mails did not work with GMX, hence this one.
 */
@Slf4j
@Component
public class SendMailService {

    @Value("${application.mail.smtp.host:none}")
    private String mailSmtpHost;
    @Value("${application.mail.smtp.user:none}")
    private String mailSmtpUser;
    @Value("${application.mail.smtp.password:none}")
    private String mailSmtpPassword;


    public void sendMail(String subject, String content) {
        sendMessage(subject, content, null, null);

    }

    public void sendMessageWithAttachment(File file, String filename) {
        sendMessage("Attachment: " + filename, "", file, filename);
    }


    private void sendMessage(String subject, String content, File file, String filename) {
        // could be moved to external properties later on...
        Properties properties = new Properties();
        properties.put("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.host", mailSmtpHost);
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.user", mailSmtpUser);
        properties.put("mail.smtp.password", mailSmtpPassword);
        properties.put("mail.smtp.starttls.enable", "true");

        Session mailSession = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(properties.getProperty("mail.smtp.user"),
                        properties.getProperty("mail.smtp.password"));
            }
        });

        try {
            Message message = new MimeMessage(mailSession);
            InternetAddress addressTo = new InternetAddress(properties.getProperty("mail.smtp.user"));
            message.setRecipient(Message.RecipientType.TO, addressTo);
            message.setSubject(subject);
            message.setFrom(addressTo);

            if (file != null && filename != null) {
                MimeBodyPart messageBodyPart = new MimeBodyPart();
                Multipart multipart = new MimeMultipart();
                messageBodyPart = new MimeBodyPart();
                DataSource source = new FileDataSource(file);
                messageBodyPart.setDataHandler(new DataHandler(source));
                messageBodyPart.setFileName(filename);
                multipart.addBodyPart(messageBodyPart);
                message.setContent(multipart);
            } else {
                message.setContent(content, "text/plain");
            }

            Transport.send(message);
        } catch (MessagingException e) {
            log.debug("Error during sending mail", e);
        }
    }

}
