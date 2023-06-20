package bsk.project.chatapp.windowsControllers;

import bsk.project.chatapp.alertBox.AlertBox;
import bsk.project.chatapp.encryption.AESUtil;
import bsk.project.chatapp.encryption.RSAUtil;
import bsk.project.chatapp.keys.KeysUtil;
import bsk.project.chatapp.message.Message;
import bsk.project.chatapp.message.MessageType;
import bsk.project.chatapp.password.PasswordUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.net.URL;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

public class MainWindowController implements Initializable {
    private boolean _isServer;
    private ObjectOutputStream outStream;
    private File _selectedFile;
    private String _sessionKey;
    private IvParameterSpec _ivSpec;
    private String _cipherMode;
    @FXML
    private TextArea conversation;
    @FXML
    private TextArea messageTextArea = new TextArea();
    @FXML
    private ComboBox<String> codingAlgorithmComboBox = new ComboBox<>();
    @FXML
    private Label sendFileLabel = new Label("");
    @FXML
    private Button generateSessionKeyButton = new Button();
    @FXML
    private Button chooseFileButton = new Button();
    @FXML
    private Button sendFileButton = new Button();
    @FXML
    private Button sendMessageButton = new Button();
    @FXML
    private ProgressBar progressBar = new ProgressBar();
    private double progressBarProgres = 0.0;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        codingAlgorithmComboBox.setItems(FXCollections.observableArrayList("ECB", "CBC"));
        codingAlgorithmComboBox.setOnAction((event) -> {
            // Sending messages and files should be disabled until session key is generated and encryption algorithm chosen
            enableUiComponents();
            _cipherMode = codingAlgorithmComboBox.getValue();
            _ivSpec = AESUtil.generateIv();
            try {
                sendCypherModeToClient();
            } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException | IOException e) {
                throw new RuntimeException(e);
            }
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
        generateSessionKeyButton.setDisable(true);
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
        if (_selectedFile == null) {
            AlertBox.infoBox("Choose file to send first!", "File not selected");
            return;
        }
        System.out.println("[INFO] " + _selectedFile.getName() + " ready to be sent");

        // Start the file sending task on a separate thread
        Thread thread = new Thread(() -> {
            try {
                sendFileTask(_selectedFile.getPath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    @FXML
    protected void onSendMessageButtonClick() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        var userText = messageTextArea.getText().trim();

        if (!userText.isBlank()) {
            var encryptedText = encrypt(userText);
            System.out.println("[INFO] Send encrypted text: " + encryptedText);

            outStream.writeObject(new Message(encryptedText));

            conversation.setText(conversation.getText().concat("Me: ").concat(userText).concat("\n"));
            messageTextArea.clear();
        }
    }

    public void onMessageReceived(Message message) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        System.out.println("[INFO] Received encrypted text: " + message.getText());
        var decryptedText = decrypt(message);
        conversation.setText(conversation.getText().concat("Him: ").concat(decryptedText).concat("\n"));
    }

    @FXML
    public void onGenerateSessionKeyClick() throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        setSessionKey(UUID.randomUUID().toString());
        sendSessionKeyToClient();
        codingAlgorithmComboBox.setDisable(false);
    }

    public void onEncryptedSessionKeyReceived(Message message) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        setSessionKey(decryptSessionKey(message.getText()));
        if(_isServer) {
            codingAlgorithmComboBox.setDisable(false);
        }
    }

    public void onCipherModeChange(Message message) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        _cipherMode = message.getCypherMode();
        System.out.println("[INFO] Received cypher mode and encrypted ivSpec from server: " + _cipherMode + "/" + message.getText());
        var encryptedIvSpec = Base64.getDecoder().decode(message.getText());
        _ivSpec = new IvParameterSpec(RSAUtil.decryptWithRSAWithoutConversion(encryptedIvSpec, getClientPrivateKey()));
        enableUiComponents();
    }

    public void setIsServer(boolean isServer) {
        _isServer = isServer;
        if(_isServer){
            generateSessionKeyButton.setDisable(false);
        }
    }

    private void sendCypherModeToClient() throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException {
        var publicKey = getClientPublicKey();
        var encryptedIvSpec = RSAUtil.encryptWithRSA(_ivSpec.getIV(), publicKey);
        System.out.println("[INFO] Cypher mode and encrypted ivSpec send to client: " + _cipherMode + "/" + encryptedIvSpec);
        outStream.writeObject(new Message(MessageType.CYPHER_MODE_CHANGED, encryptedIvSpec, _cipherMode));
    }

    private void sendSessionKeyToClient() throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException {
        var encryptedKey = encryptWithRSA(_sessionKey);
        System.out.println("[INFO] Encrypted session key send to client: " + encryptedKey);
        outStream.writeObject(new Message(MessageType.ENCRYPTED_SECRET, encryptedKey));
    }

    private String encryptWithRSA(String value) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        PublicKey publicKey = _isServer ? getClientPublicKey() : getServerPublicKey();
        return RSAUtil.encryptWithRSA(value, publicKey);
    }

    private String decryptSessionKey(String encryptedSessionKey) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        PrivateKey privateKey = _isServer ? getServerPrivateKey() : getClientPrivateKey();
        var encryptedSessionKeyBytes = Base64.getDecoder().decode(encryptedSessionKey);
        System.out.println("[INFO] Received encrypted session key: " + encryptedSessionKey);

        return RSAUtil.decryptWithRSA(encryptedSessionKeyBytes, privateKey);
    }

    private void setSessionKey(String sessionKey) {
        _sessionKey = sessionKey;
        System.out.println("[INFO] New session key: " + _sessionKey);
    }

    private void sendFileTask(String path) throws Exception {
        outStream.writeObject(new Message(MessageType.FILE_READY, encrypt(_selectedFile.getName())));
        int bytes = 0;
        File file = new File(path);
        FileInputStream fileInputStream = new FileInputStream(file);
        File encryptedFile = encryptFile(file);

        // TODO SS powinieneś mieć tutaj zaszyfrowany plik

        // send file size
        outStream.writeLong(file.length());
        long fileSize = file.length();
        long sentChunks = 0;
        // break file into chunks
        byte[] buffer = new byte[4 * 1024];
        while ((bytes = fileInputStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytes);
            outStream.flush();
            sentChunks += 4*1024;

            Thread.sleep(1000);

            double progress = (double) sentChunks / fileSize;
            Platform.runLater(() -> setProgressBarProgress(progress));


        }
        fileInputStream.close();
        encryptedFile.delete();
    }

    private void receiveFile(String fileName, ObjectInputStream in) throws Exception {
        int bytes = 0;
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);

        long currentSize = in.readLong();     // read file size
        long size = currentSize;
        byte[] buffer = new byte[4 * 1024];
        while (currentSize > 0 && (bytes = in.read(buffer, 0, (int) Math.min(buffer.length, currentSize))) != -1) {
            fileOutputStream.write(buffer, 0, bytes);
            currentSize -= bytes;      // read up to file size
            setProgressBarProgress(1.0 - (double)currentSize/size);
        }
        fileOutputStream.close();
    }

    private String encrypt(String input) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        SecretKey key = AESUtil.getKeyFromPassword(_sessionKey);
        var algorithm = "AES/" + _cipherMode + "/PKCS5Padding";

        return Objects.equals(_cipherMode, "ECB")
                ? AESUtil.encrypt(algorithm, input, key)
                : AESUtil.encrypt(algorithm, input, key, _ivSpec);
    }

    public void onFileReadyMessageReceived(Message message, ObjectInputStream in) throws Exception {
        System.out.println("[INFO] Received File Ready message");
        var filename = decrypt(message);
        conversation.setText(conversation.getText().concat("Him: ").concat(filename).concat("\n"));
        receiveFile(filename, in);
    }

    private File encryptFile(File file) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, IOException, BadPaddingException, InvalidKeyException {
        SecretKey key = AESUtil.getKeyFromPassword(_sessionKey);
        var algorithm = "AES/" + _cipherMode + "/PKCS5Padding";

        var encryptedFileName = file.getAbsolutePath() + "ENC";
        var encryptedFile = new File(encryptedFileName);

        if(_cipherMode.equals("ECB")){
            AESUtil.encryptFile(algorithm, key, null, file, encryptedFile);
        } else {
            AESUtil.encryptFile(algorithm, key, _ivSpec, file, encryptedFile);
        }
        return encryptedFile;
    }

    private String decrypt(Message message) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        SecretKey key = AESUtil.getKeyFromPassword(_sessionKey);
        var algorithm = "AES/" + _cipherMode + "/PKCS5Padding";

        return Objects.equals(_cipherMode, "ECB")
                ? AESUtil.decrypt(algorithm, message.getText(), key)
                : AESUtil.decrypt(algorithm, message.getText(), key, _ivSpec);
    }

    private void disableButtons(List<Button> buttons) {
        buttons.forEach(b -> b.setDisable(true));
    }

    private void enableButtons(List<Button> buttons) {
        buttons.forEach(b -> b.setDisable(false));
    }

    private PrivateKey getClientPrivateKey() {
        return getClientKeyPair().getPrivate();
    }

    private PublicKey getClientPublicKey() {
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

    private PrivateKey getServerPrivateKey() {
        return getServerKeyPair().getPrivate();
    }

    private PublicKey getServerPublicKey() {
        return getServerKeyPair().getPublic();
    }

    private KeyPair getServerKeyPair() {
        KeyPair serverKeyPair;
        try {
            serverKeyPair = KeysUtil.getKeyPairFromKeyStore("serverKeys/keystoreServer.jks", Objects.requireNonNull(PasswordUtil.getPassword()), "server");
            return serverKeyPair;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public void setProgressBarProgress(double progress){
        progressBar.setProgress(progress);
    }
}