package bsk.project.chatapp;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;

public class MainWindowController {
    @FXML
    private TextArea conversation;
    @FXML
    private TextArea messageText;
    private ObjectOutputStream outStream;

    public void setOutStream(ObjectOutputStream outStream) {
        this.outStream = outStream;
    }

    @FXML
    protected void onFileChooseClick() {
        Stage stage = (Stage) conversation.getScene().getWindow();
        FileChooser file = new FileChooser();
        file.setTitle("Open File");
        //loadedFile to be used later
        File loadedFile = file.showOpenDialog(stage);
    }

    @FXML
    protected void onSendButtonClick() throws IOException {
        String input = messageText.getText();
        if(!input.isBlank()) {
            conversation.setText(conversation.getText().concat("Me: ").concat(input).concat("\n"));
            //conversation.appendText("Me: " + input + "\n");
            messageText.clear();
            outStream.writeObject(new Message(input));
        }
    }

    public void onMessageReceived(String message){
        conversation.setText(conversation.getText().concat("Him: ").concat(message).concat("\n"));
        //conversation.appendText("Him: " + message + "\n");
    }
}