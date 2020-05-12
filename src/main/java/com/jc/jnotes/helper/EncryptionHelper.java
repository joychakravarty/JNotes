package com.jc.jnotes.helper;

import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

public class EncryptionHelper {

    public static void main(String[] args) {
        TextEncryptor encryptor = Encryptors.queryableText("password",
                "5c0744940b5c369b");
        String result = encryptor.encrypt("textadadqeqeeqeqegupou13uoqyeuhekjh i32 i863gh34kn");
        System.out.println(result);
        
        String origVal = encryptor.decrypt(result);
        System.out.println(origVal);
    }

}
