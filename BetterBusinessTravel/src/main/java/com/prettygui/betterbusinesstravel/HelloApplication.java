package com.prettygui.betterbusinesstravel;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(),500, 550);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Invoice Processor");
        stage.setScene(scene);
        stage.show();

        Alert info = new Alert(Alert.AlertType.INFORMATION);
        String initTxt = "Hello And Welcome To Invoice Parser 1.0.0! If This Is Your First Launch Please Set Your Login Details As These Will Be Used By The BackEnd Application To Parse Invoices From Your Email When The Computer is Logged In. For The Login Password Use An App Password Generated For Use With Outlook To Bypass Normal 2FA (On Outlook -> Go To Your Profile In Top Right Corner -> Select 'My Microsoft Account' -> Select 'Security' Section And Then 'Advanced Security Options' -> Scroll Down The Page And Under 'App Passwords' Select 'Create New App Password'). To Test That These Credentials Will Work In The BackEnd Parser, Go To Options and Select 'Run BackEnd Parser'. Any Invoice Attachments Parsed From Email Will Be Downloaded and Stored In The Folder 'attachmentsFolder' Located In Documents and The Train/Car Invoice CSV Files Are Stored In The Folder 'CSV_Files' Located In Documents. Check README For Any More Information";
        info.setTitle("Welcome");
        info.setContentText(initTxt);
        info.show();
    }

    public static void main(String[] args) {
        launch();
    }
}

