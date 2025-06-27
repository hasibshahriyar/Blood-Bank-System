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


    //this is responsible to send the message with attachment
    private static void sendAttach(String path,String to) {
        String subject = "CodersArea : Confirmation";
        String message = "something";
        String from = "triple.t.202020@gmail.com";

        //Variable for gmail
        String host="smtp.gmail.com";

        //get the system properties
        Properties properties = System.getProperties();
        System.out.println("PROPERTIES "+properties);

        //setting important information to properties object

        //host set
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port","465");
        properties.put("mail.smtp.ssl.enable","true");
        properties.put("mail.smtp.auth","true");

        //Step 1: to get the session object..
        Session session=Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("triple.t.202020@gmail.com", "i@mn1r?0n");
            }



        });

        session.setDebug(true);

        //Step 2 : compose the message [text,multi media]
        MimeMessage m = new MimeMessage(session);

        try {

            //from email
            m.setFrom(from);

            //adding recipient to message
            m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            //adding subject to message
            m.setSubject(subject);


            //attachement..

            //file path



            MimeMultipart mimeMultipart = new MimeMultipart();
            //text
            //file

            MimeBodyPart textMime = new MimeBodyPart();

            MimeBodyPart fileMime = new MimeBodyPart();

            try {

                textMime.setText(message);

                File file=new File(path);
                fileMime.attachFile(file);


                mimeMultipart.addBodyPart(textMime);
                mimeMultipart.addBodyPart(fileMime);


            } catch (Exception e) {

                e.printStackTrace();
            }


            m.setContent(mimeMultipart);


            //send

            //Step 3 : send the message using Transport class
            Transport.send(m);



        }catch (Exception e) {
            e.printStackTrace();
        }








        System.out.println("Sent success...................");


    }

    //this is responsible to send email..
    public static void sendEmail(int code, String to) {
        String subject = "Password recovery email";
        String message = " <h2>your verification code is</h2><br/><h1><b>"+"    "+code+"</b></h1> ";
        String from="triple.t.202020@gmail.com";
        //Variable for gmail
        String host="smtp.gmail.com";

        //get the system properties
        Properties properties = System.getProperties();
        System.out.println("PROPERTIES "+properties);

        //setting important information to properties object

        //host set
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port","465");
        properties.put("mail.smtp.ssl.enable","true");
        properties.put("mail.smtp.auth","true");

        //Step 1: to get the session object..
        Session session=Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication( from,"gspvweliuwlxxiec");
            }



        });

        session.setDebug(true);

        //Step 2 : compose the message [text,multi media]
        MimeMessage m = new MimeMessage(session);

        try {

            //from email
            m.setFrom(from);

            //adding recipient to message
            m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            //adding subject to message
            m.setSubject(subject);

            m.setContent(message,"text/html");

            //adding text to message
//            m.setText(message);

            //send

            //Step 3 : send the message using Transport class
            Transport.send(m);

            System.out.println("Sent success...................");


        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}