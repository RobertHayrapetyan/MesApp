package com.example.myapplication;

import android.util.Log;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import javax.crypto.Cipher;

public class Cryptography {
    private Key publicKey, privateKey;

    public Key getPublicKey() {
        return publicKey;
    }

    public Key getPrivateKey() {
        return privateKey;
    }

    public void keyGenPair(){
        publicKey = null;
        privateKey = null;
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(1024);
            KeyPair kp = kpg.genKeyPair();
            publicKey = kp.getPublic();
            privateKey = kp.getPrivate();
        } catch (Exception e) {
            Log.d("LOG", "RSA key pair error");
        }
    }

    public byte[] encodeText(String text, Key privateKey){
        byte[] encodedBytes = null;
        try {
            Cipher c = Cipher.getInstance("RSA");
            c.init(Cipher.ENCRYPT_MODE, privateKey);
            encodedBytes = c.doFinal(text.getBytes());
        } catch (Exception e) {
            Log.e("LOG", "RSA encryption error");
        }

        return encodedBytes;
    }

    public String decodedText(byte[] encodedBytes, Key publicKey){
        byte[] decodedBytes = null;
        try {
            Cipher c = Cipher.getInstance("RSA");
            c.init(Cipher.DECRYPT_MODE, publicKey);
            decodedBytes = c.doFinal(encodedBytes);

        } catch (Exception e) {
            Log.e("Crypto", "RSA decryption error");
        }

        return new String(decodedBytes);
    }
}
