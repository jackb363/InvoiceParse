package com.prettygui.betterbusinesstravel;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.util.Optional;

public class SchedulerController {
    @FXML
    Button closeButton;
    @FXML
    ComboBox freqOption;
    @FXML
    Spinner hourOption,minOption;
    @FXML
    protected void closeScheduler(){
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
    @FXML
    protected void submitSchedulerVals(){
        String hour = hourOption.getValue().toString().matches("\\d{2}") ? hourOption.getValue().toString() : "0"+hourOption.getValue();
        String min = minOption.getValue().toString().matches("\\d{2}") ? minOption.getValue().toString() : "0"+minOption.getValue();
        String fullTime = hour+":"+min;
        String freq = freqOption.getValue().toString().trim();
        String freqVal = "";
        if(freq.contains("30 Mins")){
            freqVal = "30";
        } else if(freq==("1 Hour")){
            freqVal = "60";
        } else if(freq.contains("2 Hours")){
            freqVal = "120";
        } else if(freq.contains("3 Hours")){
            freqVal = "180";
        } else{
            freqVal = "240";
        }
        Alert confirmTask = new Alert(Alert.AlertType.CONFIRMATION);
        confirmTask.setContentText("You Are About To Add A New Task That Runs The BackEnd Parser Once Every "+freq+". This Begins At "+fullTime+" Each Day.\nThis Task Only Runs When The Computer Is Logged In.\nIs This Correct?");

        Stage stage = (Stage) confirmTask.getDialogPane().getScene().getWindow();
        stage.initStyle(StageStyle.UNDECORATED);

        Optional<ButtonType> result = confirmTask.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            String batch = "SCHTASKS /CREATE /SC DAILY /TN InvoiceParser\\Backend-Parser /TR "+System.getProperty("user.dir")+"\\BackEndParser.exe /ST "+fullTime+" /F /RI "+freqVal+" /DU 24:00";
            try {
                Runtime.getRuntime().exec(batch);
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Success");
                success.setContentText("Successfully Created Task");
                success.show();
                Stage stageSchd = (Stage) closeButton.getScene().getWindow();
                stageSchd.close();
            } catch (Exception e) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Error");
                error.setContentText(e.getMessage());
                error.show();
            }
        } else {
            confirmTask.close();
        }

    }
}
