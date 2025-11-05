package tetish_inn_backend.tetish_inn.common.utils;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailDispatcher {

    private static JavaMailSender emailSender;

    public EmailDispatcher(JavaMailSender emailSender){
        EmailDispatcher.emailSender = emailSender;
    }

    public static void dispatchEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@abzedtetz.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        System.out.println("SEND EMAIL------------" + message);
        emailSender.send(message);
    }
}
