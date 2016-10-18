package utility;

import java.math.BigDecimal;
import java.util.Properties;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import model.User;

public class Mail {

    private static String fromEmail;
    private static String targetEmail;
    private static String username;
    private static String password;

    public Session getSession() {
        String host = "smtp.gmail.com";
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "587");

        // Get the Session object.
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        return session;
    }

    /**
     * Sends an mail to the specified Email in the config file upon an error.
     * @param error
     */
    public void sendError(Exception error) {
        // Get the Session object.
        Session session = getSession();         
        try {
            // Create a default MimeMessage object.
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(targetEmail));
            message.setSubject(error.toString());
            BodyPart messageBodyPart = new MimeBodyPart();

            // Now set the actual message
            messageBodyPart.setText("An error occured.\n" + error.toString());

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            message.setContent(multipart);

            // Send message
            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sends a mail to the specified Email in the config file upon a withdrawel/deposit from an user.
     * @param type
     * @param amount
     * @param user
     */
    public void sendTransactionNotice(String type, BigDecimal amount, User user) {
        Session session = getSession();         
        try {
            // Create a default MimeMessage object.
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(targetEmail));
            message.setSubject(type + " processed");
            BodyPart messageBodyPart = new MimeBodyPart();

            // Now set the actual message
            messageBodyPart.setText("A " + type + " has been processed.\nUser: " + user.getUsername() + "\nAmount: " + amount.toString() + "BLK.");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            message.setContent(multipart);

            // Send message
            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setFromEmail(String fromEmail) {
        Mail.fromEmail = fromEmail;
    }

    public static void setUsername(String username) {
        Mail.username = username;
    }

    public static void setPassword(String password) {
        Mail.password = password;
    }

    public static void setTargetEmail(String targetEmail) {
        Mail.targetEmail = targetEmail;
    }
}
