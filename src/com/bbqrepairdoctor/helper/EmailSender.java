package com.bbqrepairdoctor.helper;

import com.bbqrepairdoctor.model.ClickInfo;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class EmailSender {
    public static void sendEmail(List<ClickInfo> clickDetails, String title) throws Exception {
        Properties props = new Properties();
        try(InputStream is = EmailSender.class.getResourceAsStream("/resources.properties")) {
            if (is == null){
                throw new RuntimeException("place resources.properties with email and password under /src folder");
            }
            props.load(is);
        }

        String email = props.getProperty("email");
        String password = props.getProperty("password");
        props.clear();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.transport.protocol", "smtp");

        String msg="";
        for (ClickInfo clickInfo:clickDetails){
            String clickDetail = clickInfo.clickDetails;
            String keyWord = clickInfo.keyWord;
            msg = msg+"Keyword: "+keyWord+". Click: "+clickDetail+"\r\n\r\n";
        }

        Session getMailSession = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(email, password);
            }
        });
        MimeMessage generateMailMessage = new MimeMessage(getMailSession);
        generateMailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
        generateMailMessage.addRecipient(Message.RecipientType.CC, new InternetAddress(email));
        generateMailMessage.setSubject(title+ ". Click details for " +clickDetails.size()+" clicks.");

        Multipart multipart = new MimeMultipart();

        MimeBodyPart textPart = new MimeBodyPart();
        String textContent = "Please find the Attachment.";
        textPart.setText(textContent);
        multipart.addBodyPart(textPart);

        byte[] bytes = msg.getBytes();
        InputStream is = new ByteArrayInputStream(bytes);

        MimeBodyPart attachementPart = new MimeBodyPart();
        DataSource fds = new ByteArrayDataSource(is, "text/plain");
        attachementPart.setDataHandler(new DataHandler(fds));
        attachementPart.setFileName("details.txt");
        multipart.addBodyPart(attachementPart);

        generateMailMessage.setContent(multipart);
        Transport transport = getMailSession.getTransport("smtp");
        transport.connect("smtp.gmail.com", email, password);
        transport.sendMessage(generateMailMessage, generateMailMessage.getAllRecipients());
        transport.close();
    }
}
