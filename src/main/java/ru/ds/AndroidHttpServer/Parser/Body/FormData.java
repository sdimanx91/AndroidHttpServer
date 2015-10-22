package ru.ds.AndroidHttpServer.Parser.Body;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import ru.ds.AndroidHttpServer.HttpRequest;

/**
 * class provided data of the Form, recieved in request
 */
public class FormData  {
    private static final String TAG = "FormData";
    private HashMap<String, Object> mFormData = new HashMap<String, Object>();

    /**
     * Parse the form body
     * @param reader BufferedReader of the Socket
     * @param requestedSize value of Content-Length header
     */
    public FormData(InputStream inputStream, long requestedSize, Socket socket) {
        if (inputStream == null) {
            return;
        }
        StringBuffer sBuffer = new StringBuffer();
        do {
            String line = null;
            line = HttpRequest.HttpRequestBuilder.readStringFromBuffer(inputStream, socket);
            if (line == null) {
                break;
            }
            sBuffer.append(line.trim());
        } while (true);
        String buffer = sBuffer.toString();
        if (buffer.isEmpty()) {
            return;
        }
        String[] pairs;
        if (!buffer.contains("&")) {
            pairs = new String[1];
            pairs[0] = buffer;
        } else {
            pairs = buffer.split("\\&");
        }
        for (int i=0; i<pairs.length; i++) {
            String[] pair = pairs[i].split("=");
            if (pair.length !=2) {
                continue;
            }
            mFormData.put(pair[0], pair[1]);
        }
    }

    public Set<String> getFormKeysSet() {
        return mFormData.keySet();
    }

    /**
     * @param key key in a form hash
     * @return if this object is a instance of FormData then String value or empty string otherwise
     */
    public String getFormValue(String key) {
        Object string = getObject(key);
        if (string instanceof String) {
            return (String) string;
        }
        return "";
    }

    /**
     * get the Object which can be a String or MultipartValue instance different a Content-Type header
     * @param key key in a form hash
     * @return the object value instance
     */
    protected Object getObject(String key) {
        Iterator<Map.Entry<String, Object>> i = mFormData.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry<String, Object> e = i.next();
        }
        return mFormData.get(key);
    }

    /**
     * only for internal use;
     * for a childred
     */
    protected void putValue(String key, Object value) {
        mFormData.put(key, value);
    }
}
