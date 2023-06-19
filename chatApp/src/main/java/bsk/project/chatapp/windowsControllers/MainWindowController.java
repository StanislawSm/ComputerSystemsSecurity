package bsk.project.chatapp.windowsControllers;

import bsk.project.chatapp.encryption.AESUtil;
import bsk.project.chatapp.encryption.RSAUtil;
import bsk.project.chatapp.keys.KeysUtil;
import bsk.project.chatapp.message.Message;
import bsk.project.chatapp.message.MessageType;
import bsk.project.chatapp.password.PasswordUtil;
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
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

public class MainWindowController implements Initializable {
    private ObjectOutputStream outStream;
    private File _selectedFile;
    private String _sessionKey;
    @FXML
    private TextArea conversation;
    @FXML
    private TextArea messageTextArea = new TextArea();
    @FXML
    private ComboBox<String> codingAlgorithmComboBox = new ComboBox<>();
    @FXML
    private Label sendFileLabel = new Label("");
    @FXML
    private Button chooseFileButton = new Button();
    @FXML
    private Button sendFileButton = new Button();
    @FXML
    private Button sendMessageButton = new Button();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        codingAlgorithmComboBox.setItems(FXCollections.observableArrayList("ECB", "CBC"));
        codingAlgorithmComboBox.setOnAction((event) -> {
            // Sending messages and files should be disabled until session key is generated and encryption algorithm chosen
            enableUiComponents();
        });

        // Sending messages and files should be disabled until session key is generated and encryption algorithm chosen
        disableUiComponents();
    }

    /**
     * Method disable some buttons and textFields
     */
    public void disableUiComponents() {
        codingAlgorithmComboBox.setDisable(true);
        messageTextArea.setDisable(true);
        disableButtons(Arrays.asList(chooseFileButton, sendFileButton, sendMessageButton));
    }

    /**
     * Method enable some buttons and textFields
     */
    public void enableUiComponents() {
        enableButtons(Arrays.asList(chooseFileButton, sendFileButton, sendMessageButton));
        messageTextArea.setDisable(false);
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

        if (_selectedFile != null) {
            sendFileLabel.setText(_selectedFile.getName());
        }
    }

    @FXML
    protected void onSendFileButtonClick() throws Exception {
//        Stage stage = (Stage) conversation.getScene().getWindow();
//        FileChooser file = new FileChooser();
//        file.setTitle("Choose File");
//        File loadedFilePath = file.showOpenDialog(stage);
        if (_selectedFile == null) {
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

        var userText = messageTextArea.getText().trim();

        if (!userText.isBlank()) {
            var encryptedText = encrypt(userText);
            System.out.println("Encrypted text: " + encryptedText);

            //outStream.writeObject(new Message(userText));
            var cipherMode = codingAlgorithmComboBox.getValue();
            outStream.writeObject(new Message(encryptedText, cipherMode));

            conversation.setText(conversation.getText().concat("Me: ").concat(userText).concat("\n"));
            messageTextArea.clear();
        }
    }

    public void onMessageReceived(Message message)
            throws InvalidAlgorithmParameterException, NoSuchPaddingException,
            IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException,
            BadPaddingException, InvalidKeyException {

        var decryptedText = decrypt(message);
        conversation.setText(conversation.getText().concat("Him: ").concat(decryptedText).concat("\n"));
    }

    @FXML
    public void onGenerateSessionKeyClick()
            throws IOException, NoSuchPaddingException, IllegalBlockSizeException,
            NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {

        _sessionKey = UUID.randomUUID().toString();
        System.out.println("Generated new session key: " + _sessionKey);
        codingAlgorithmComboBox.setDisable(false);

        // TODO MS get OTHER USER public RSA key here
        PublicKey publicKey = getClientPublicKey();
        var encrypted = RSAUtil.encryptSessionKeyWithRSA(_sessionKey, publicKey);
        System.out.println("Encrypted new session key: " + encrypted);
        outStream.writeObject(new Message(MessageType.ENCRYPTED_SECRET, encrypted, "none"));
    }

    public void onEncryptedSessionKeyReceived(Message message)
            throws NoSuchPaddingException, IllegalBlockSizeException,
            NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {

        var encryptedSessionKey = message.getText();
        var encryptedSessionKeyBytes = Base64.getDecoder().decode(message.getText());
        System.out.println("Received encrypted session key: " + encryptedSessionKey);

        // TODO MS get MY private RSA key here
        PrivateKey privateKey = getClientPrivateKey();
        var decryptedSessionKey = RSAUtil.decryptSessionKeyWithRSA(encryptedSessionKeyBytes, privateKey);
        setSessionKey(decryptedSessionKey);

        codingAlgorithmComboBox.setDisable(false);
    }

    private void setSessionKey(String sessionKey) {
        _sessionKey = sessionKey;
        System.out.println("New session key: " + _sessionKey);
    }

    private void sendFile(String path) throws Exception {
        int bytes = 0;
        File file = new File(path);
        FileInputStream fileInputStream = new FileInputStream(file);

        // send file size
        outStream.writeLong(file.length());
        // break file into chunks
        byte[] buffer = new byte[4 * 1024];
        while ((bytes = fileInputStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytes);
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
        var algorithmKey = "AES/" + algorithm + "/PKCS5Padding";

        return Objects.equals(algorithm, "ECB")
                ? AESUtil.encrypt(algorithmKey, input, key)
                : AESUtil.encrypt(algorithmKey, input, key, ivSpec);
    }

    private String decrypt(Message message)
            throws NoSuchAlgorithmException, InvalidKeySpecException,
            NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException,
            InvalidKeyException, InvalidAlgorithmParameterException {

        var ivSpec = AESUtil.generateIv(); // TODO MS co zorbić z generowaniem IV?
        String password = _sessionKey;
        SecretKey key = AESUtil.getKeyFromPassword(password);

        var algorithm = message.getCypherMode();
        var algorithmKey = "AES/" + algorithm + "/PKCS5Padding";

        return Objects.equals(algorithm, "ECB")
                ? AESUtil.decrypt(algorithmKey, message.getText(), key)
                : AESUtil.decrypt(algorithmKey, message.getText(), key, ivSpec);
    }

    private void disableButtons(List<Button> buttons) {
        buttons.forEach(b -> b.setDisable(true));
    }

    private void enableButtons(List<Button> buttons) {
        buttons.forEach(b -> b.setDisable(false));
    }

    public PrivateKey getClientPrivateKey() {
        return getClientKeyPair().getPrivate();
    }

    public PublicKey getClientPublicKey() {
        return getClientKeyPair().getPublic();
    }

    private KeyPair getClientKeyPair() {
        KeyPair clientKeyPair;
        try {
            clientKeyPair = KeysUtil.getKeyPairFromKeyStore("clientKeys/keystoreClient.jks", Objects.requireNonNull(PasswordUtil.getPassword()), "client");
            return clientKeyPair;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}