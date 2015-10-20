package ru.ds.AndroidHttpServer;

import android.content.Context;
import android.util.Log;

import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import ru.ds.AndroidHttpServer.Requests.HttpRoutingRequest;
import ru.ds.AndroidHttpServer.Requests.StaticAssetRequest;
import ru.ds.AndroidHttpServer.Requests.StaticRequest;

public class HttpRouter {

    private static final String TAG = "HttpRouter";
    //hash map with String key and Http routing request value
    private HashMap<String, HttpRoutingRequest> routingRequests = new HashMap<String, HttpRoutingRequest>();

    /**
     * append to router new request processor
     * @param URLString url associated with processor
     * @param processor request processor
     */
    public void route(String URLString, HttpRoutingRequest processor) {
        if (processor == null || URLString == null || URLString.isEmpty()) {
            Log.e(TAG, "addMethodProcessor error");
            return ;
        }
        routingRequests.put(URLString, processor);
    }

    /**
     * serve the static resource at the directory.
     * = routeStatic(url, directory, "index.html", useAssets)
     *
     * @param url starts url
     * @param directory directory for static files
     * @param useAssets search files in application assets
     */
    public void routeStatic(String url, String directory) {
        StaticRequest staticRequest;

//        if (useAssets) {
            staticRequest = new StaticAssetRequest(directory, url);
//        } else {
//            staticRequest = new StaticRequest(directory, url);
//        }
        route(url, staticRequest);
    }

    /**
     * serve the static resource at the directory.
     * @param url starts url
     * @param directory directory for static files
     * @param defaultResponseFile if file in URL is empty, then use to request this file name.
     * @param useAssets search files in application assets
     */
    public void routeStatic(String url, String directory, String defaultResponseFile) {
        StaticRequest staticRequest;

//        if (useAssets) {
            staticRequest = new StaticAssetRequest(directory, url, defaultResponseFile);
//        } else {
//            staticRequest = new StaticRequest(directory, url, defaultResponseFile);
//        }
        route(url, staticRequest);
    }

    /**
     * executing request method associated with http-method
     * @param request http request object
     * @return http response object
     * @see ru.ds.AndroidHttpServer.Const.HTTPMethods
     * @see HttpRequestProcessor
     */
    public void execute(HttpRequest request, OutputStream outputStream, Context context) {
        HttpResponse response = new HttpResponse(outputStream);

        URL requestUri = request.getUrl();
        if (request == null) {
            Log.e(TAG, "Error, uri is null");
            response.render404();
            return;
        }

        HttpRoutingRequest requestProcessor = null;
        if (routingRequests != null) {
            requestProcessor = routingRequests.get(requestUri.getPath());

            // search static info ?
            if (requestProcessor == null) {
                Iterator<String> pathI = routingRequests.keySet().iterator();
                while (pathI.hasNext()) {
                    String path = pathI.next();
                    if (requestUri.getPath().startsWith(path)) {
                        HttpRoutingRequest bufProcessor = routingRequests.get(path);
                        if (bufProcessor instanceof StaticRequest ||
                            bufProcessor instanceof StaticAssetRequest)
                        {
                            requestProcessor = bufProcessor;
                            break;
                        }
                    }
                }
            }
        }
        if (requestProcessor != null) {
            if (!requestProcessor.callMethod(request, response, context)) {
                response.render404();
            }
        } else {
            response.render404();
        }
    }
}
