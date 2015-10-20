package ru.ds.AndroidHttpServer;

import android.util.Log;

import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;

public class HttpRouter {

    private static final String TAG = "HttpRouter";
    //hash map with String key and Http routing request value
    private HashMap<String, HttpRoutingRequest> routingRequests = new HashMap<String, HttpRoutingRequest>();

    /**
     * append to router new request processor
     * @param URL url associated with processor
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
     * executing request method associated with http-method
     * @param request http request object
     * @return http response object
     * @see ru.ds.AndroidHttpServer.Const.HTTPMethods
     * @see HttpRequestProcessor
     */
    public void execute(HttpRequest request, OutputStream outputStream) {
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
        }
        if (requestProcessor != null) {
            if (!requestProcessor.callMethod(request, response)) {
                response.render404();
            }
        } else {
            response.render404();
        }
    }
}
