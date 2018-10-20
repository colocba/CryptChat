package com.example.amirbaum.cryptchat;

import android.util.Base64;
import android.util.Log;

import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by amirbaum on 13/10/2018.
 */

public final class EncryptionDecryptionUtility {

    public EncryptionDecryptionUtility() {
    }

    public static String RSAdecryptMessage(String message, String encodedPrivateKey) {

        try {

            byte[] decodedKey = Base64.decode(encodedPrivateKey, Base64.DEFAULT);
            byte[] decodedEncryptedMessage = Base64.decode(message, Base64.DEFAULT);

            PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decodedKey));
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            return new String(cipher.doFinal(decodedEncryptedMessage), "UTF-8");

        } catch (Exception e) {
            Log.d("RSA_DECRYPTION_ERROR", e.toString());
        }

        return null;

    }
    // FOR MESSAGES ENCRYPTION PURPOSE
    public static byte[] RSAencryptMessage(String plainText, String encodedPublicKey) {

        // WORKS !
        try {

            byte[] decodedPublicKey = Base64.decode(encodedPublicKey, Base64.DEFAULT);

            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decodedPublicKey));
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            return cipher.doFinal(plainText.getBytes("UTF-8"));

        } catch (Exception e) {
            Log.d("RSA_ENCRYPTION_ERROR", e.toString());
        }

        return null;

    }

    // FOR MESSAGES ENCRYPTION PURPOSE
    public static byte[] AESencryptMessage(String plainText, String encodedKey) {

        try {

            byte[] decodedKey = Base64.decode(encodedKey, Base64.DEFAULT);

            Key originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, originalKey);

            return cipher.doFinal(plainText.getBytes("UTF-8"));

        } catch (Exception e) {
            Log.d("AES_DECRYPTION_ERROR", e.toString());
        }

        return null;

    }
    // FOR IMAGES ENCRYPTION PURPOSE
    public static byte[] AESencryptMessage(byte[] plainText, String encodedKey) {

        try {

            byte[] decodedKey = Base64.decode(encodedKey, Base64.DEFAULT);

            Key originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, originalKey);

            return cipher.doFinal(plainText);

        } catch (Exception e) {
            Log.d("AES_DECRYPTION_ERROR", e.toString());
        }

        return null;

    }

    public static String AESdecryptMessage(String message, String encodedKey) {

        try {

            byte[] decodedKey = Base64.decode(encodedKey, Base64.DEFAULT);
            byte[] decodedEncryptedMessage = Base64.decode(message, Base64.DEFAULT);

            Key originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, originalKey);

            return new String(cipher.doFinal(decodedEncryptedMessage), "UTF-8");

        } catch (Exception e) {
            Log.d("AES_DECRYPTION_ERROR", e.toString());
        }

        return null;

    }

    public static byte[] AESdecryptMessage(byte[] message, String encodedKey) {

        try {

            byte[] decodedKey = Base64.decode(encodedKey, Base64.DEFAULT);

            Key originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, originalKey);

            return cipher.doFinal(message);

        } catch (Exception e) {
            Log.d("AES_DECRYPTION_ERROR", e.toString());
        }

        return null;

    }

}
