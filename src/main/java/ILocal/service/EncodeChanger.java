package ILocal.service;

import org.springframework.stereotype.Service;

@Service
public class EncodeChanger {

    public String unicode2UnicodeEsc(String uniStr) {
        StringBuffer ret = new StringBuffer();
        if (uniStr == null) {
            return null;
        }
        int maxLoop = uniStr.length();
        for (int i = 0; i < maxLoop; i++) {
            char character = uniStr.charAt(i);
            if (character <= '') {
                ret.append(character);
            } else {
                ret.append("\\u");
                String hexStr = null;
                hexStr = Integer.toHexString(character).toLowerCase();
                int zeroCount = 4 - hexStr.length();
                for (int j = 0; j < zeroCount; j++) {
                    ret.append('0');
                }
                ret.append(hexStr);
            }
        }
        return ret.toString();
    }
}
