package com.hasib.bloodbank.server.provider;

import java.io.File;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class SendEmail {

    // Email configuration constants
    private static final String EMAIL_FROM = "triple.t.202020@gmail.com";
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "465";

    // TODO: Replace with your actual Gmail App Password
    // To generate: Gmail Settings > Security > 2-Step Verification > App passwords
    private static final String APP_PASSWORD = "your-16-digit-app-password-here";

    private static Properties getEmailProperties() {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", SMTP_HOST);
        properties.put("mail.smtp.port", SMTP_PORT);
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2");
        return properties;
    }

    private static Session getEmailSession() {
        return Session.getInstance(getEmailProperties(), new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_FROM, APP_PASSWORD);
            }
        });
    }

    //this is responsible to send the message with attachment
    private static void sendAttach(String path, String to) {
        String subject = "CodersArea : Confirmation";
        String message = "something";

        Session session = getEmailSession();
        session.setDebug(false); // Set to true only for debugging

        //Step 2 : compose the message [text,multi media]
        MimeMessage m = new MimeMessage(session);

        try {
            //from email
            m.setFrom(EMAIL_FROM);

            //adding recipient to message
            m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            //adding subject to message
            m.setSubject(subject);

            //attachement..
            MimeMultipart mimeMultipart = new MimeMultipart();

            //text
            MimeBodyPart textMime = new MimeBodyPart();
            textMime.setText(message);

            //file
            MimeBodyPart fileMime = new MimeBodyPart();
            try {
                File file = new File(path);
                fileMime.attachFile(file);

                mimeMultipart.addBodyPart(textMime);
                mimeMultipart.addBodyPart(fileMime);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to attach file: " + path, e);
            }

            m.setContent(mimeMultipart);

            //Step 3 : send the message using Transport class
            Transport.send(m);
            System.out.println("Email with attachment sent successfully to: " + to);

        } catch (Exception e) {
            System.err.println("Failed to send email with attachment: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Email sending failed", e);
        }
    }

    //this is responsible to send email verification code
    public static void sendEmail(int code, String to) {
        String subject = "Blood Bank - Password Recovery";
        String message = String.format(
            "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
            "<h2 style='color: #dc3545; text-align: center;'>🩸 Blood Bank System</h2>" +
            "<h3 style='color: #333;'>Password Recovery Request</h3>" +
            "<p>You have requested to reset your password. Please use the verification code below:</p>" +
            "<div style='background-color: #f8f9fa; padding: 20px; text-align: center; border-radius: 5px; margin: 20px 0;'>" +
            "<h1 style='color: #dc3545; font-size: 32px; margin: 0; letter-spacing: 3px;'>%06d</h1>" +
            "</div>" +
            "<p style='color: #666;'>This code will expire in 10 minutes for security reasons.</p>" +
            "<p style='color: #666; font-size: 12px;'>If you didn't request this, please ignore this email.</p>" +
            "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'>" +
            "<p style='color: #999; font-size: 12px; text-align: center;'>Blood Bank Management System</p>" +
            "</div>",
            code
        );

        try {
            Session session = getEmailSession();
            session.setDebug(false); // Set to true only for debugging

            //Step 2 : compose the message
            MimeMessage m = new MimeMessage(session);

            //from email
            m.setFrom(EMAIL_FROM);

            //adding recipient to message
            m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            //adding subject to message
            m.setSubject(subject);

            //set HTML content
            m.setContent(message, "text/html; charset=utf-8");

            //Step 3 : send the message using Transport class
            Transport.send(m);

            System.out.println("Password recovery email sent successfully to: " + to);
            System.out.println("Verification code: " + code);

        } catch (Exception e) {
            System.err.println("Failed to send password recovery email: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to send verification email to: " + to, e);
        }
    }
}

