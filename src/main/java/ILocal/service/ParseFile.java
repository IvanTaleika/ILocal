package ILocal.service;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
           encodingCharset = "windows-1251";
        }
        inputStream = new FileInputStream(fileName);
        properties.load(new InputStreamReader(inputStream, Charset.forName(encodingCharset)));
        HashMap<String, String> propertiesMap = new HashMap<String, String>((Map) properties);
        return propertiesMap;
    }
}