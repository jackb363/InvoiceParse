module com.prettygui.betterbusinesstravel {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.mail;
    requires org.jsoup;
    requires org.apache.poi.ooxml;
    requires org.apache.pdfbox;
    requires javafx.graphics;
    requires org.apache.commons.codec;
    requires org.apache.commons.io;

    opens com.prettygui.betterbusinesstravel to javafx.fxml;
    exports com.prettygui.betterbusinesstravel;
}