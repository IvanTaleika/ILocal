package ILocal.service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.universalchardet.UniversalDetector;

public class ParseFile {

    public Map<String, String> parseFile(File fileName) throws IOException, JSONException {
        if(fileName.getName().contains("properties")) return parseProperties(fileName);
        if(fileName.getName().contains("json")) return parseJson(fileName);
        return null;
    }

    private Map<String, String> parseJson(File fileName) throws FileNotFoundException, JSONException {
        Gson gson = new Gson();
        JsonElement json = gson.fromJson(new FileReader(fileName.getPath()), JsonElement.class);
        JSONObject jsonObj = new JSONObject(json.toString());
        Map<String, String> map = new Gson().fromJson(jsonObj.toString(),Map.class);
        return null;
    }

    public Map<String, String> parseProperties(File fileName) throws IOException {
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
