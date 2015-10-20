package ru.ds.AndroidHttpServer;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import ru.ds.AndroidHttpServer.Const.DefaultHeaders;

/**
 * HttpResponse class
 *
 * for write response you need:
 *     1. setStatus (if needed)
 *     2. setHeaders use function setHeader (if needed - you can use default headers in constructor)
 *     3. append value to response body (if needed)
 *
 *  This action's is required to comply with the order
 */
public class HttpResponse {

    private static final String TAG = "HttpResponse";

    private OutputStream mOutputStream;
    private String mStatus                    = "200 OK";
    private HashMap<String, String> mHeaders  = new HashMap<String, String>();
    private boolean mRendered                 = false;
    final ByteBuffer mBodyBuffer                    = ByteBuffer.allocate(1024*1024*5);

    public HttpResponse(OutputStream outputStream) {
        this.mOutputStream = outputStream;

        // set the default headers
        mHeaders.put(DefaultHeaders.Server, "AndroidLiteHTTPServer/0.1 beta");
        mHeaders.put(DefaultHeaders.Date, new Date().toString());
        mHeaders.put(DefaultHeaders.ContentType, "text/html; charset=UTF-8");
        mHeaders.put(DefaultHeaders.Connection, "close");

    }

    /** render the 404 page **/
    public void render404() {
        mStatus = "404 Not Found";
        Log.d(TAG, mStatus);
        writeln("404 error. Page not found.");
        render();
    }

    /** set response status **/
    public void setStatus(String status) {
        mStatus = status;
    }

    /** set header for response **/
    public void setHeader(String headerName, String headerValue) {
        mHeaders.put(headerName, headerValue);
    }

    /** put response to output stream */
    public void render() {
        if (mRendered) {
            return;
        }

        // set size header
        int contentLength = mBodyBuffer.position();
        setHeader(DefaultHeaders.ContentLength, Integer.toString(contentLength));

        Log.d(TAG, "r1");
        // write status
        writeToStream("HTTP/1.1 " + mStatus+"\n");

        Log.d(TAG, "r2");
        // write headers
        Iterator headersIterator = mHeaders.entrySet().iterator();
        while (headersIterator.hasNext()) {
            HashMap.Entry<String, String> pair = (HashMap.Entry<String, String>) headersIterator.next();
            writeToStream(pair.getKey() + ": " + pair.getValue() + "\n");
        }

        Log.d(TAG, "r3");

        // empty string before body
        writeToStream("\n");

        Log.d(TAG, "r4: " + Integer.toString(contentLength));
        //write body
        mBodyBuffer.position(0);
        byte[] secondBody = new byte[contentLength];

        mBodyBuffer.get(secondBody, 0, contentLength);
        writeToStream(secondBody);
        mBodyBuffer.clear();
        mRendered = true;
    }

    /** Write bytes into output stream **/
    public void writeBytes(byte[] bytes) {
        try {
            Log.d(TAG,"writeBytes:" + new String(bytes, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (mRendered) {
            return;
        }
        int sLen = bytes.length;
        int pos  = mBodyBuffer.position();

        if (sLen + pos >= mBodyBuffer.capacity()) {
            return;
        }
        mBodyBuffer.put(bytes);
    }

    /** Write some string into output stream **/
    public void write(String s) {
        try {
            writeBytes(s.getBytes("UTF-8"));
        } catch (IOException e) {
            Log.e(TAG, "write to response body cache error");
            e.printStackTrace();
        }
    }

    /** Write some string and new line into output stream **/
    public void writeln(String s) {
        this.write(s + "\n");
    }

    /** Write is empty line **/
    public void writeln() {
        writeln("");
    }

    /** put bytes to stream **/
    private void writeToStream(String string) {
        if (string.length() == 0) {
            return;
        }
        byte[] bytes = null;
        try {
            bytes = string.getBytes("UTF-8");
        } catch (IOException e) {
            Log.e(TAG, "write string to output stream error");
            e.printStackTrace();
        }
        writeToStream(bytes);
    }

    /** put string to stream **/
    private void writeToStream(byte[] bytes) {
        if (mRendered || bytes == null || bytes.length==0) {
            return;
        }
        try {
             Log.d(TAG, "write " + Integer.toString(bytes.length) + " bytes" );
             Log.d(TAG, "write: " + new String(bytes, "UTF-8"));
            mOutputStream.write(bytes);
             mOutputStream.flush();
        } catch (IOException e) {
            Log.e(TAG, "write bytes to output stream error");
            e.printStackTrace();
        }
    }
}
