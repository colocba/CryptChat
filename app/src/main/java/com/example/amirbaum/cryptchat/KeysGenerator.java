package com.example.amirbaum.cryptchat;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;

/**
 * Created by amirbaum on 12/10/2018.
 */

public class KeysGenerator {

    private String mStringPrivateKey;
    private String mStringPublicKey;
    private String mAESKey;

    private PrivateKey mPrivateKey;
    private PublicKey mPublicKey;
    private KeyPairGenerator mKeyPairGenerator;
    private KeyPair mKeyPair;

    private KeyGenerator mKeyGenerator;
    private Key mKey;

    public KeysGenerator() {

        try {

            mKeyPairGenerator = KeyPairGenerator.getInstance("RSA");
            mKeyPairGenerator.initialize(512);
            mKeyPair = mKeyPairGenerator.generateKeyPair();

            mKeyGenerator = KeyGenerator.getInstance("AES");
            mKey = mKeyGenerator.generateKey();

        } catch (NoSuchAlgorithmException e) {
            Log.d("KEY_GENERATOR", e.toString());
        }

    }

    public String getPublicKey() {

        return mStringPublicKey;

    }
    public String getPrivateKey() {

        return mStringPrivateKey;

    }
    public void generateRSAKey() {

        mPrivateKey = mKeyPair.getPrivate();
        mPublicKey = mKeyPair.getPublic();

        mStringPublicKey = Base64.encodeToString(mPublicKey.getEncoded(), Base64.DEFAULT);
        mStringPrivateKey = Base64.encodeToString(mPrivateKey.getEncoded(), Base64.DEFAULT);

    }

    public void generateAESKey() {
        mAESKey = Base64.encodeToString(mKey.getEncoded(), Base64.DEFAULT);
    }

    public String getAESKey () {return mAESKey;}

    public KeyPair generateKeyPair() {

        try {

            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");

            SecureRandom random = new SecureRandom();

            keyGen.initialize(256, random);

            KeyPair generateKeyPair = keyGen.generateKeyPair();
            return generateKeyPair;

        } catch (Exception e) {
            Log.d("GENERATE_KEYS_ERROR", e.toString());
        }

        return null;
    }

}
