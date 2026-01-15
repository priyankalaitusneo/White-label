package com.laitsneo.mipPay.service;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Service
public class EmailService {

    @Autowired
    private Configuration config;


    public String changePassword(String email, String otp) {
        Map<String, Object> model = new HashMap<>();
        System.out.println("otp-->" + otp);
        model.put("otp", otp);
        String to = email;
        String from = "support@myuniqpay.com";
        final String username = "support@myuniqpay.com";
        final String password = "ppaztkbqidncviyp";
        String host = "smtp.gmail.com";
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", 587);
        properties.put("mail.smtp.ssl.trust", host);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        Authenticator authenticator = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };
        Session session = Session.getInstance(properties, authenticator);
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));
            message.setSubject("Verification code");
            Template t = config.getTemplate("email_Otp.ftl");
            System.out.println("model: " + model);
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(t, model);
            message.setContent(html, "text/html");
            Transport.send(message);
            return "Otp sent successful..!";
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        } catch (TemplateException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
