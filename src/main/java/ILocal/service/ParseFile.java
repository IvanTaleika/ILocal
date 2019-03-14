package ILocal.service;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import org.mozilla.universalchardet.UniversalDetector;

public class ParseFile {

    public Map<String, String> parseFile(File fileName) throws IOException {
        InputStream inputStream = new FileInputStream(fileName);
        Properties properties = new Properties();
        UniversalDetector universalDetector= new UniversalDetector(null);
        byte[] bytes = new byte[64000];
        int amountOfBytes;
        while ((amountOfBytes = inputStream.read(bytes)) > 0 && !universalDetector.isDone()) {
            universalDetector.handleData(bytes, 0, amountOfBytes);
        }
        universalDetector.dataEnd();
        String encodingCharset = universalDetector.getDetectedCharset();
        if (encodingCharset == null) {
            return null;
        }
        inputStream = new FileInputStream(fileName);
        properties.load(new InputStreamReader(inputStream, Charset.forName(encodingCharset)));
        HashMap<String, String> propertiesMap = new HashMap<String, String>((Map) properties);
        return propertiesMap;
    }
}