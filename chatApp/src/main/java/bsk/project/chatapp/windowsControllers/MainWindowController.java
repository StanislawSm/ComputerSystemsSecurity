package bsk.project.chatapp.windowsControllers;

import bsk.project.chatapp.encryption.AESUtil;
import bsk.project.chatapp.message.Message;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.*;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

public class MainWindowController implements Initializable {
    private ObjectOutputStream outStream;
    private File _selectedFile;
    private String _sessionKey;
    @FXML
    private TextArea conversation;
    @FXML
    private TextArea messageText;
    @FXML
    private ComboBox<String> codingAlgorithmComboBox = new ComboBox<>();
    @FXML
    private Label sendFileLabel;
    @FXML
    private Button chooseFileButton = new Button();
    @FXML
    private Button sendFileButton = new Button();
    @FXML
    private Button sendMessageButton = new Button();
    //private final List<Button> _requireValidSessionKeyButtons = ;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        codingAlgorithmComboBox.setItems(FXCollections.observableArrayList("ECB", "CBC"));
        codingAlgorithmComboBox.setOnAction((event) -> {
            // TODO MS wybrana wartość powinna być wysłana do drugiego użytkownika
            System.out.println(codingAlgorithmComboBox.getValue());
        });

        sendFileLabel.setText("");

        disableButtons(Arrays.asList(chooseFileButton, sendFileButton, sendMessageButton));
    }

    public void setOutStream(ObjectOutputStream outStream) {
        this.outStream = outStream;
    }

    @FXML
    protected void onChooseFileButtonClick() throws Exception {
        Stage stage = (Stage) conversation.getScene().getWindow();
        FileChooser file = new FileChooser();
        file.setTitle("Choose File");
        _selectedFile = file.showOpenDialog(stage);

        if(_selectedFile != null){
            sendFileLabel.setText(_selectedFile.getName());
        }
    }

    @FXML
    protected void onSendFileButtonClick() throws Exception {
//        Stage stage = (Stage) conversation.getScene().getWindow();
//        FileChooser file = new FileChooser();
//        file.setTitle("Choose File");
//        File loadedFilePath = file.showOpenDialog(stage);
        if(_selectedFile == null){
            System.out.println("Choose file to send!");
            return;
        }

        System.out.println(_selectedFile.getName());
        // outStream.writeObject(new Message("file ready to be sent"));
        // outStream.writeObject(new Message(MessageType.FILE_READY, loadedFilePath.getName()));
        // sendFile(loadedFilePath.getPath());
    }

    @FXML
    protected void onSendMessageButtonClick()
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException,
            InvalidAlgorithmParameterException, NoSuchPaddingException,
            IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        var input = messageText.getText();

        if(!input.isBlank()) {
            var encrypted = encrypt(input);
            System.out.println(encrypted);

            outStream.writeObject(new Message(input));

            conversation.setText(conversation.getText().concat("Me: ").concat(input).concat("\n"));
            messageText.clear();
        }
    }

    public void onMessageReceived(String message){
        conversation.setText(conversation.getText().concat("Him: ").concat(message).concat("\n"));
    }

    @FXML
    public void onGenerateSessionKeyClick() {
        _sessionKey = UUID.randomUUID().toString();
        enableButtons(Arrays.asList(chooseFileButton, sendFileButton, sendMessageButton));



        System.out.println(_sessionKey);
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

    private String encrypt(String input)
            throws NoSuchAlgorithmException, InvalidKeySpecException,
            NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException,
            InvalidKeyException, InvalidAlgorithmParameterException {

        var ivSpec = AESUtil.generateIv();
        String password = _sessionKey;
        SecretKey key = AESUtil.getKeyFromPassword(password);

        var algorithm = codingAlgorithmComboBox.getValue();
        var algorithmKey = "AES/"+ algorithm +"/PKCS5Padding";

        return Objects.equals(algorithm, "ECB")
                ? AESUtil.encrypt(algorithmKey, input, key)
                : AESUtil.encrypt(algorithmKey, input, key, ivSpec);
    }

    private void disableButtons(List<Button> buttons){
        buttons.forEach(b -> b.setDisable(true));
    }

    private void enableButtons(List<Button> buttons){
        buttons.forEach(b -> b.setDisable(false));
    }
}