package ru.ds.AndroidHttpServer.Requests;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import ru.ds.AndroidHttpServer.HttpRequest;
import ru.ds.AndroidHttpServer.HttpResponse;

/**
 * Created by dmitrijslobodchikov on 20.10.15.
 */
public class StaticAssetRequest extends  StaticRequest {

    private static final String TAG = "StaticAssetRequest";

    public StaticAssetRequest(String pathToResource, String baseUrl) {
        super(pathToResource, baseUrl);
    }

    public StaticAssetRequest(String pathToResource, String baseUrl, String defaultName) {
        super(pathToResource, baseUrl, defaultName);
    }

    @Override
    protected InputStream getFileInputStream(HttpRequest request, Context context) {
        if (context == null) {
            return null;
        }
        AssetManager assetManager = null;
        synchronized (context) {
            assetManager = context.getAssets();
        }
        if (assetManager == null) {
            return null;
        }
        String pathToFile = getStaticPath(request);
        if (pathToFile == null) {
            return null;
        }

        try {
            return assetManager.open(pathToFile);
        }
        catch (FileNotFoundException fnf) {
            Log.e(TAG, "File: " + pathToFile + " not found");
        }
        catch (IOException e) {
            Log.e(TAG, "Asset Manager error");
            e.printStackTrace();
        }
        return null;
    }
}
