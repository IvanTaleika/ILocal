package ILocal.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordEncoderMD5 {

    public String createPassword(String password) throws NoSuchAlgorithmException {
        StringBuilder code = new StringBuilder();
        MessageDigest messageDigest;
        messageDigest = MessageDigest.getInstance("MD5");
        byte bytes[] = password.getBytes();
        byte digest[] = messageDigest.digest(bytes);
        for (byte aDigest : digest) {
            code.append(Integer.toHexString(0x0100 + (aDigest & 0x00FF)).substring(1));
        }
        password = code.toString();
        return password;
    }
}
