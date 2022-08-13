package com.prettygui.betterbusinesstravel;

import connector.ConnectToEmail;
import functions.Serialisation;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import parsers.PDF_Parser;
import parsers.WordDoc_Parser;

import javax.mail.MessagingException;
import javax.mail.Store;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Optional;

public class HelloController{
    @FXML
    TextField usernameTextField,passwordPasswordField;
    @FXML
    Button loginButton,editButton,browseButton;
    @FXML
    TextFlow logArea;
    @FXML
    protected void saveLogin(){
        String host = "imap-mail.outlook.com";
        String port = "993";
        String email = usernameTextField.getText();
        String password = passwordPasswordField.getText();
        ArrayList<String> login = new ArrayList<>();

        String fileName = "login.ser";
        try {
            login.add(Serialisation.encrypt(email));
            login.add(Serialisation.encrypt(password));
            Serialisation.ser(login, fileName);
            Store connection = ConnectToEmail.connectToOutlook(host,port,email,password);
            String message = ConnectToEmail.downloadFromOutlook(connection);
            printToLog("Successfully Connected","");
            printToLog(message,"");
        } catch (MessagingException | ParseException | IOException | GeneralSecurityException e) {
            printToLog(e.toString(),e.getMessage());
        }
    }

    @FXML
    protected void editPref(){
        loginButton.setDisable(false);
        usernameTextField.setDisable(false);
        passwordPasswordField.setDisable(false);
    }
    @FXML
    protected void browseFiles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Word & PDF Files", "*.docx","*.pdf"));
        File selectedFile = fileChooser.showOpenDialog((browseButton.getScene().getWindow()));
        int rows = 0;
        StringBuilder logText = new StringBuilder();
        if(selectedFile!=null){
                String fileName = selectedFile.getName();
                String dir = selectedFile.getPath();
                if (fileName.contains(".pdf")) {
                    try {
                        PDF_Parser.parsePDF(dir);
                        logText.append(fileName).append(" Parsed");
                        rows++;
                    } catch (IOException e) {
                        printToLog(e.toString(), e.getMessage());
                    }
                } else {
                    try {
                        WordDoc_Parser.parseWord(dir);
                        logText.append(fileName).append(" Parsed");
                        rows++;
                    } catch (IOException e) {
                        printToLog(e.toString(), e.getMessage());
                    }
                }
            printToLog(logText.toString(),logText+" Document Parsed");
        }
        else {
            printToLog("No Files Chosen","");
        }
    }

    @FXML
    protected void clearFolder(){
        Alert confirmDelete = new Alert(Alert.AlertType.CONFIRMATION);
        String user = System.getProperty("user.name");

        File attachDir = new File("C:/Users/"+user+"/Documents/attachmentsFolder");
        String[] files = attachDir.list();
        confirmDelete.setTitle("Are You Sure You Want To Clear The Cache?");
        confirmDelete.setContentText("This Will Delete "+files.length+" Files Previously Downloaded And Parsed From Email That Are Currently In The Attachments Folder");
        Optional<ButtonType> result = confirmDelete.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            for(int i = 0; i< files.length; i++){
                File current = new File(attachDir+"/"+files[i]);
                current.delete();
            }
            printToLog(files.length+" Files Cleared From Cache","");
        } else {
            confirmDelete.close();
        }
    }
    @FXML
    protected void launchBackEnd(){
        String batch = "cmd /c start %CD%\\BackEndParser.exe";
        try {
            Runtime.getRuntime().exec(batch);
        } catch (IOException e) {
            printToLog(e.toString(),e.getMessage());
        }
    }
    @FXML
    protected void closeApp() {
        Platform.exit();
    }
    @FXML
    protected void createTaskSchd() {
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("TaskScheduler.fxml"));
            Scene scene = new Scene(fxmlLoader.load(),300, 200);
            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void printToLog(String message,String alert){
        String ee = message.toLowerCase();
        if(ee.contains("exception")) {
            Alert err = new Alert(Alert.AlertType.ERROR);
            err.setTitle("ERROR");
            err.setContentText("An Error Has Occurred : " + alert);
            err.show();
            Text errMsg = new Text(message+"\n");
            errMsg.setFill(Color.RED);
            logArea.getChildren().add(errMsg);
        } else{
            Text infoMsg = new Text(message+"\n");
            infoMsg.setFill(Color.BLACK);
            logArea.getChildren().add(infoMsg);
        }
    }
}

