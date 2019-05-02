package ILocal.service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ParseFile {

	public Map<String, String> parseFile(File fileName) throws IOException, JSONException {
		if (fileName.getName().endsWith("properties")) return parseProperties(fileName);
		if (fileName.getName().endsWith("json")) return parseJson(fileName);
		return null;
	}

	private Map<String, String> parseJson(File fileName) throws IOException, JSONException {
		Gson gson = new Gson();
		JsonElement json = gson.fromJson(new FileReader(fileName.getPath()), JsonElement.class);
		if (json.toString().contains("[") || json.toString().contains("]")) return null;
		JSONObject jsonObj = new JSONObject(json.toString());
		Map<String, String> map = new Gson().fromJson(jsonObj.toString(), Map.class);
		return editMapByLength(map);
	}

	public Map<String, String> parseProperties(File fileName) throws IOException {
		InputStream inputStream = new FileInputStream(fileName);
		Properties properties = new Properties();
		UniversalDetector universalDetector = new UniversalDetector(null);
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
		Map<String, String> propertiesMap = new HashMap<String, String>((Map) properties);
		inputStream.close();
		fileName.delete();
		return editMapByLength(propertiesMap);
	}

	private Map<String, String> editMapByLength(Map<String, String> termMap){
		Map<String, String> resultMap = new HashMap<>();
		for (String key : termMap.keySet()) {
			String temp = termMap.get(key);
			if(key.length()>2000) key = key.substring(0,2000);
			if(temp.length()>5000) temp = temp.substring(0,5000);
			resultMap.put(key, temp);
		}
		return resultMap;
	}
}
