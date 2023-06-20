package bsk.project.chatapp.keys;

import bsk.project.chatapp.ChatServer;
import bsk.project.chatapp.windowsControllers.MainWindowController;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;

public class KeysUtil {
    /**
     @param fileName name of the file with keys saved, may be clientKeys/keystoreClient.jks or /serverKeys/keystoreServer.jks
     @param password a string with password used for login, must be 123456
     @param ownerName must be client or server
     */
    public static KeyPair getKeyPairFromKeyStore(String fileName, String password, String ownerName) throws Exception {
        //File initialFile = new File("./keys/" + fileName);
        // TODO MS
        File initialFile = new File("chatapp/keys/" + fileName);
        InputStream ins = new FileInputStream(initialFile);

        KeyStore keyStore = KeyStore.getInstance("JCEKS");
        keyStore.load(ins, password.toCharArray());   //Keystore password
        KeyStore.PasswordProtection keyPassword =       //Key password
                new KeyStore.PasswordProtection(password.toCharArray());

        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(ownerName, keyPassword);

        java.security.cert.Certificate cert = keyStore.getCertificate(ownerName);
        PublicKey publicKey = cert.getPublicKey();
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();

        ins.close();
        return new KeyPair(publicKey, privateKey);
    }
}
