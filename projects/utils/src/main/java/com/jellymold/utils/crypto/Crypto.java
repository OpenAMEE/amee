package com.jellymold.utils.crypto;

import com.sun.crypto.provider.SunJCE;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.*;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

// TODO: Clean up this class, particularly methods of instantiation, maybe make a singleton
public class Crypto {

    private static Key key = null;

    // TODO: read in the key Array from some external source eg keystore or file
    private static final byte[] keyArray = {
            (byte) 0xef, (byte) 0xba, (byte) 0xc7, (byte) 0xa7,
            (byte) 0xf7, (byte) 0xd5, (byte) 0xbc, (byte) 0xdf,
            (byte) 0xbc, (byte) 0xb6, (byte) 0x38, (byte) 0xf2,
            (byte) 0xdc, (byte) 0xb3, (byte) 0x46, (byte) 0x45,
            (byte) 0xb3, (byte) 0x13, (byte) 0x54, (byte) 0xea,
            (byte) 0x57, (byte) 0xe6, (byte) 0x26, (byte) 0x0e};


    private static final byte[] salt = {
            (byte) 0xc7, (byte) 0x73, (byte) 0x21, (byte) 0x8c,
            (byte) 0x7e, (byte) 0xc8, (byte) 0xee, (byte) 0x99};

    private static final IvParameterSpec iv = new javax.crypto.spec.IvParameterSpec(salt);

    public Crypto() {
        super();
    }

    public synchronized static void initialise() throws CryptoException {
        if (Crypto.key == null) {
            Security.addProvider(new SunJCE());
            try {
                DESedeKeySpec pass = new DESedeKeySpec(Crypto.keyArray);
                SecretKeyFactory skf = SecretKeyFactory.getInstance("DESede");
                key = skf.generateSecret(pass);
            } catch (InvalidKeyException e) {
                System.out.println("initialise() caught InvalidKeyException: " + e.getMessage());
                throw new CryptoException("InvalidKeyException", e);
            } catch (InvalidKeySpecException e) {
                System.out.println("initialise() caught InvalidKeySpecException: " + e.getMessage());
                throw new CryptoException("InvalidKeySpecException", e);
            } catch (NoSuchAlgorithmException e) {
                System.out.println("initialise() caught NoSuchAlgorithmException: " + e.getMessage());
                throw new CryptoException("NoSuchAlgorithmException ", e);
            }
        }
    }

    public static String encrypt(String toBeEncrypted) throws CryptoException {
        Crypto.initialise();
        byte[] data = toBeEncrypted.getBytes();
        try {
            Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, Crypto.key, Crypto.iv);
            byte[] result = cipher.doFinal(data);
            result = Base64.encodeBase64(result);
            return new String(result);
        } catch (InvalidAlgorithmParameterException e) {
            System.out.println("encrypt() caught InvalidAlgorithmParameterException: " + e.getMessage());
            throw new CryptoException("InvalidAlgorithmParameterException ", e);
        } catch (InvalidKeyException e) {
            System.out.println("encrypt() caught InvalidKeyException: " + e.getMessage());
            throw new CryptoException("Invalid Key Exception", e);
        } catch (IllegalBlockSizeException e) {
            System.out.println("encrypt() caught IllegalBlockSizeException: " + e.getMessage());
            throw new CryptoException("Illegal Block Size Exception", e);
        } catch (BadPaddingException e) {
            System.out.println("encrypt() caught BadPaddingException: " + e.getMessage());
            throw new CryptoException("Bad Padding Exception", e);
        } catch (NoSuchPaddingException e) {
            System.out.println("initialise() caught NoSuchPaddingException: " + e.getMessage());
            throw new CryptoException("NoSuchPaddingException", e);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("initialise() caught NoSuchAlgorithmException: " + e.getMessage());
            throw new CryptoException("NoSuchAlgorithmException", e);
        }
    }

    public static String decrypt(String toBeDecrypted) throws CryptoException {
        Crypto.initialise();
        try {
            Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
            byte[] data = toBeDecrypted.getBytes("UTF-8");
            data = Base64.decodeBase64(data);
            cipher.init(Cipher.DECRYPT_MODE, Crypto.key, Crypto.iv);
            byte[] result = cipher.doFinal(data);
            return new String(result);
        } catch (InvalidAlgorithmParameterException e) {
            System.out.println("decrypt() caught InvalidAlgorithmParameterException: " + e.getMessage());
            e.printStackTrace();
            throw new CryptoException("InvalidAlgorithmParameterException: ", e);
        } catch (UnsupportedEncodingException e) {
            System.out.println("decrypt() caught UnsupportedEncodingException: " + e.getMessage());
            throw new CryptoException("Unsupported Encoding Exception", e);
        } catch (InvalidKeyException e) {
            System.out.println("decrypt() caught InvalidKeyException: " + e.getMessage());
            throw new CryptoException("Invalid Key Exception", e);
        } catch (IllegalBlockSizeException e) {
            System.out.println("decrypt() caught IllegalBlockSizeException: " + e.getMessage());
            throw new CryptoException("Illegal Block Size Exception", e);
        } catch (BadPaddingException e) {
            System.out.println("decrypt() caught BadPaddingException: " + e.getMessage());
            throw new CryptoException("Bad Padding Exception", e);
        } catch (NoSuchPaddingException e) {
            System.out.println("initialise() caught NoSuchPaddingException: " + e.getMessage());
            throw new CryptoException("NoSuchPaddingException ", e);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("initialise() caught NoSuchAlgorithmException: " + e.getMessage());
            throw new CryptoException("NoSuchAlgorithmException ", e);
        }
    }
}