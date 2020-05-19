/*
 * This file is part of JNotes. Copyright (C) 2020  Joy Chakravarty
 * 
 * JNotes is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JNotes is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JNotes.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * 
 */
package com.jc.jnotes.util;

import static com.jc.jnotes.JNotesConstants.ENCRYPTION_SALT;
import static com.jc.jnotes.JNotesConstants.LOCAL_ENCRYPTION_KEY;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

/*
 * This class has encrypt/decrypt methods using Spring security. 
 * 
 */
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
        if(StringUtils.isBlank(encryptionKey)) {
            return textToEncrypt;
        }
        TextEncryptor encryptor = Encryptors.queryableText(encryptionKey, ENCRYPTION_SALT);
        return encryptor.encrypt(textToEncrypt);
    }

    public static String decrypt(String encryptionKey, String textToDecrypt) {
        if (StringUtils.isBlank(textToDecrypt)) {
            return null;
        }
        if(StringUtils.isBlank(encryptionKey)) {
            return textToDecrypt;
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
