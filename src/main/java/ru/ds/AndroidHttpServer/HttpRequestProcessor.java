package ru.ds.AndroidHttpServer;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by dmitrijslobodchikov on 11.10.15.
 */
public class HttpRequestProcessor extends Thread {

    private static final String TAG = "HttpRequestProcessor";
    // route configuration
    private HttpRouter router;

    // connection objects
    private Socket mSocket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private Context mContext;

    /**
     * this is a constructor which initialize input and output stream
     * @param socket connected tcp socket
     * @param router the request router
     */
    public HttpRequestProcessor(Socket socket, HttpRouter router, Context context) {
        super();
        this.router   = router;
        this.mContext = context;
        try {
            this.inputStream  = socket.getInputStream();
            this.outputStream = socket.getOutputStream();
            this.mSocket      = socket;
        } catch (IOException ioException) {
            Log.e(TAG, "socket stream's error");
            ioException.printStackTrace();
            return ;
        }
    }

    /** Reading the Socket input buffer and line-to-line and process them**/
    @Override
    public void run() {
        HttpRequest.HttpRequestBuilder builder = HttpRequest.HttpRequestBuilder.parse(inputStream, mSocket);

        if (builder != null && builder.get() != null) {
            router.execute(builder.get(), outputStream, mContext);
        }
        try {
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            Log.e(TAG, "output stream flush error");
            e.printStackTrace();
        }
    }


}
