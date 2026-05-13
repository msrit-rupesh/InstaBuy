package org.example;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import jakarta.mail.Message;
import jakarta.mail.internet.MimeMessage;

public class Main {
    public static void main(String[] args) throws Exception {

        int port = 2525;


        GreenMail server = new GreenMail(
                new ServerSetup(port, "127.0.0.1", ServerSetup.PROTOCOL_SMTP)
        );
        server.start();

        System.out.println(" Fake SMTP server running on localhost:2525");
        System.out.println(" Waiting for emails...");


        int lastCount = 0;

        while (true) {
            Message[] msgs = server.getReceivedMessages();

            if (msgs.length > lastCount) {
                MimeMessage msg = (MimeMessage) msgs[lastCount++];

                System.out.println("EMAIL RECEIVED");
                System.out.println("To      : " + msg.getAllRecipients()[0]);
                System.out.println("Subject : " + msg.getSubject());
                System.out.println("Body    : " + msg.getContent());
                System.out.println("\n\n\n");
            }

            Thread.sleep(300);
        }
    }
}