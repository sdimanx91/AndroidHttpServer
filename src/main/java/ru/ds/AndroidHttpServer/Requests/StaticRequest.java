package ru.ds.AndroidHttpServer.Requests;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import ru.ds.AndroidHttpServer.HttpRequest;
import ru.ds.AndroidHttpServer.HttpResponse;

/**
 * ....
 */
public class StaticRequest extends HttpRoutingRequest {
    protected static final int STREAMS_IN_OUT_BUFFER =  1024*5;
    private static final String TAG = "StaticRequest";
    private String mPathToResource;
    private String mBaseUrl;
    private String mDefaultName = "index.html";

    public StaticRequest(String pathToResource, String baseUrl) {
        this.mPathToResource = pathToResource;
        this.mBaseUrl        = baseUrl;
    }

    public StaticRequest(String pathToResource, String baseUrl, String defaultName) {
        this.mPathToResource = pathToResource;
        this.mBaseUrl        = baseUrl;
        this.mDefaultName    = defaultName;
    }

    public String getPathToResource() {
        return mPathToResource;
    }

    public String getDefaultName() {
        return mDefaultName;
    }

    protected String getStaticPath(HttpRequest request) {
        if (request == null) {
            return null;
        }
        URL url = request.getUrl();
        if (url == null) {
            return null;
        }

        String path = url.getPath();
        path = path.substring(mBaseUrl.length());
        if (path.trim().isEmpty()) {
            path = "/" + getDefaultName();
        }
        StringBuffer sBuffer = new StringBuffer();
        sBuffer.append(mPathToResource);
        sBuffer.append(path);
        return sBuffer.toString();
    }

    protected InputStream getFileInputStream(HttpRequest request, Context context) {
        String pathToFile = getStaticPath(request);
        if (pathToFile == null) {
            return null;
        }
        File file = new File(pathToFile);
        if (!file.exists() || !file.canRead()) {
            return null;
        }
        InputStream stream;

        try {
            stream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return stream;
    }

    @Override
    protected boolean doGet(HttpRequest request, HttpResponse response, Context context) {
        InputStream stream = getFileInputStream(request, context);
        if (stream == null) {
            return false; // render 404
        }
        try {
            while (stream.available()>0) {
                int bufferSize = stream.available();
                if (bufferSize > STREAMS_IN_OUT_BUFFER) {
                    bufferSize = STREAMS_IN_OUT_BUFFER;
                }
                byte[] buffer = new byte[bufferSize];
                stream.read(buffer,0,bufferSize);
                response.writeBytes(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
}
