package com.jc.jnotes.util;

import static com.jc.jnotes.JNotesConstants.ENCRYPTION_SALT;
import static com.jc.jnotes.JNotesConstants.LOCAL_ENCRYPTION_KEY;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

public final class EncryptionUtil {

    public static String locallyEncrypt(String textToEncrypt) {
        return encrypt(LOCAL_ENCRYPTION_KEY, textToEncrypt);
    }

    public static String locallyDecrypt(String textToDecrypt) {
        return decrypt(LOCAL_ENCRYPTION_KEY, textToDecrypt);
    }

    public static String encrypt(String encryptionKey, String textToEncrypt) {
        if (StringUtils.isBlank(textToEncrypt)) {
            return null;
        }
        TextEncryptor encryptor = Encryptors.queryableText(encryptionKey, ENCRYPTION_SALT);
        return encryptor.encrypt(textToEncrypt);
    }

    public static String decrypt(String encryptionKey, String textToDecrypt) {
        if (StringUtils.isBlank(textToDecrypt)) {
            return null;
        }
        TextEncryptor encryptor = Encryptors.queryableText(encryptionKey, ENCRYPTION_SALT);
        return encryptor.decrypt(textToDecrypt);
    }

    public static void main(String[] args) {
        String encryptedVal = locallyEncrypt("Testing");
        System.out.println("encryptedVal " + encryptedVal);

        String decryptedVal = locallyDecrypt(encryptedVal);
        System.out.println("decryptedVal " + decryptedVal);
    }

}
