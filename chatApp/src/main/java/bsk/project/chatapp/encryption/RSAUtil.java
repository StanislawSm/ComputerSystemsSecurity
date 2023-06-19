package bsk.project.chatapp.encryption;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public class RSAUtil {
    public static String encryptWithRSA(String value, PublicKey publicKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] sessionKeyBytes = value.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedSessionKeyBytes = encryptCipher.doFinal(sessionKeyBytes);

        return Base64.getEncoder().encodeToString(encryptedSessionKeyBytes);
    }
    public static String encryptWithRSA(byte[] bytes, PublicKey publicKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedSessionKeyBytes = encryptCipher.doFinal(bytes);

        return Base64.getEncoder().encodeToString(encryptedSessionKeyBytes);
    }

    public static String decryptWithRSA(byte[] bytes, PrivateKey privateKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] encryptedBytes = encryptCipher.doFinal(bytes);

        return new String(encryptedBytes, StandardCharsets.UTF_8);
    }

    public static byte[] decryptWithRSAWithoutConversion(byte[] bytes, PrivateKey privateKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.DECRYPT_MODE, privateKey);

        return encryptCipher.doFinal(bytes);
    }
}
