package connector;

import com.sun.mail.util.MailSSLSocketFactory;
import parsers.Email_Parser;
import parsers.PDF_Parser;
import parsers.WordDoc_Parser;

import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Properties;

public class ConnectToEmail {
    public static Store connectToOutlook(String host, String port, String user, String password) throws MessagingException, GeneralSecurityException {
        // Sets properties to allow connection to outlook
        MailSSLSocketFactory sf = new MailSSLSocketFactory();
        sf.setTrustAllHosts(true);

        Properties properties = new Properties();
        properties.setProperty("mail.imap.host", host);
        properties.setProperty("mail.imap.port", port);
        properties.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.TLSSocketFactory");
        properties.setProperty("mail.imap.socketFactory.fallback", "false");
        properties.setProperty("mail.imap.socketFactory.port", String.valueOf(port));
        properties.setProperty("mail.imap.auth", "true");
        properties.setProperty("mail.imap.ssl.trust", "*");
        properties.put("mail.imap.ssl.socketFactory", sf);
        // adds properties to session object
        Session session = Session.getDefaultInstance(properties, null);

        // connects to the message store
        Store store;
        // connects to the message store with imaps protocol
        store = session.getStore("imaps");
        store.connect(host, user, password);

        return store;
    }

    public static String downloadFromOutlook(Store connection)
            throws ParseException, MessagingException, IOException {
        String returnMessage = "";
        int rows = 0;
        Folder inbox = connection.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);

        // search for all "unseen" messages and adds to string array
        Flags seen = new Flags(Flags.Flag.SEEN);
        FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
        Message messages[] = inbox.search(unseenFlagTerm);

        ArrayList<String> arrayForCSV;
        if (messages.length == 0) {
            returnMessage = "No New Invoices to Parse";
        }
        // loops through messages array
        for (int i = 0; i < messages.length; i++) {
            Message message = messages[i];
            String contentType = message.getContentType();
            String messageContent = "";
            // store attachment file name, separated by comma
            String attachFiles = "";
            String userPC = System.getProperty("user.name");
            String dir = "C:\\Users\\"+userPC+"\\Documents\\attachmentsFolder\\";
            if (contentType.contains("multipart")) {
                // content may contain attachments
                MimeMultipart multiPart = (MimeMultipart) message.getContent();
                int numberOfParts = multiPart.getCount();

                for (int partCount = 0; partCount < numberOfParts; partCount++) {

                    MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);

                    if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                        // this part is attachment
                        String fileName = part.getFileName().toLowerCase();
                        attachFiles += fileName + ", ";
                        //checks file extension and sends to appropriate parser
                        if (fileName.contains(".pdf")) {
                            part.saveFile(dir + File.separator + part.getFileName());
                            PDF_Parser.parsePDF(dir + part.getFileName());
                            rows++;
                        } else if (fileName.contains(".docx")) {
                            part.saveFile(dir + File.separator + part.getFileName());
                            WordDoc_Parser.parseWord(dir + part.getFileName());
                            rows++;
                        }
                    } else {
                        // this part may be the message content
                        messageContent = Email_Parser.getTextFromMimeMultipart(multiPart);
                        if (messageContent.contains("trainline")) {
                            Email_Parser.parseTrainLine(messageContent);
                            rows++;
                            break;
                        }
                    }
                }
            }
            returnMessage = rows+" Invoices Parsed From Outlook";
        }
        return returnMessage;
    }
}

