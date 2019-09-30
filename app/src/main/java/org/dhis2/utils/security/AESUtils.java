package org.dhis2.utils.security;


import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;

public class AESUtils
{

    public static byte[] encryptMsg(byte[] message, String password)
            throws GeneralSecurityException {
        return AESCrypt.encrypt(password, new String(message)).getBytes();
    }

    public static byte[] decryptMsg(byte[] cipherText, String password)
            throws GeneralSecurityException {
        return AESCrypt.decrypt(password, new String(cipherText)).getBytes();
    }

}