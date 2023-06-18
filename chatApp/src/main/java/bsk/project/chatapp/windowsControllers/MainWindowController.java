package bsk.project.chatapp.windowsControllers;

import bsk.project.chatapp.message.Message;
import bsk.project.chatapp.message.MessageType;
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
    protected void onFileChooseClick() throws Exception {
        Stage stage = (Stage) conversation.getScene().getWindow();
        FileChooser file = new FileChooser();
        file.setTitle("Open File");
        File loadedFilePath = file.showOpenDialog(stage);
        outStream.writeObject(new Message("file ready to be sent"));
        outStream.writeObject(new Message(MessageType.FILE_READY, loadedFilePath.getName()));
        sendFile(loadedFilePath.getPath());
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

    private void sendFile(String path) throws Exception{
        int bytes = 0;
        File file = new File(path);
        FileInputStream fileInputStream = new FileInputStream(file);

        // send file size
        outStream.writeLong(file.length());
        // break file into chunks
        byte[] buffer = new byte[4*1024];
        while ((bytes=fileInputStream.read(buffer))!=-1){
            outStream.write(buffer,0,bytes);
            outStream.flush();
        }
        fileInputStream.close();
    }

}