package ru.ds.AndroidHttpServer;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
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
    private String mStatus                   = "200 OK";
    private HashMap<String, String> mHeaders = new HashMap<String, String>();
    private boolean mHeadersIsSended          = false;

    public HttpResponse(OutputStream outputStream) {
        this.mOutputStream = outputStream;

        // set the default headers
        mHeaders.put(DefaultHeaders.Server, "AndroidLiteHTTPServer/0.1 beta");
        mHeaders.put(DefaultHeaders.Date, new Date().toString());
        mHeaders.put(DefaultHeaders.ContentType, "text/html; charset=utf-8");
        mHeaders.put(DefaultHeaders.Connection, "close");
    }

    /** render the 404 page **/
    public void render404() {
        mStatus = "404 Not Found";
        renderHeaders();

        writelnToBody("404 error. Page not found.");
    }

    /** set response status **/
    public void setStatus(String status) {
        mStatus = status;
    }

    /** set header for response **/
    public void setHeader(String headerName, String headerValue) {
        mHeaders.put(headerName, headerValue);
    }

    /** append new string to response body and flush output stream**/
    public void writeToBody(String string) {
        renderHeaders();
        this.write(string);
        try {
            mOutputStream.flush();
        } catch (IOException e) {
            Log.e(TAG, "error of flushing output stream on writeToBody");
            e.printStackTrace();
        }
    }

    /** append new line to response body and flush output stream**/
    public void writelnToBody(String line) {
        renderHeaders();
        writeToBody(line + "\n");
    }

    /** append bytes from buffer to response and flush output stream **/
    public void writeBytesToBody(byte[] responseBytes) {
        renderHeaders();
        try {
            mOutputStream.write(responseBytes);
            mOutputStream.flush();
        } catch (IOException e) {
            Log.e(TAG, "error the write data or flush in the output stream on putBodyBytes");
            e.printStackTrace();
        }
    }

    /** put response to output stream */
    private void renderHeaders() {
        if (mHeadersIsSended) {
            return;
        }

        // write status
        writeln("HTTP/1.x " + mStatus);

        // write headers
        Iterator headersIterator = mHeaders.entrySet().iterator();
        while (headersIterator.hasNext()) {
            HashMap.Entry<String, String> pair = (HashMap.Entry<String, String>) headersIterator.next();
            writeln(pair.getKey() + ": " + pair.getValue());
        }

        // flush headers
        try {
            mOutputStream.flush();
        } catch (IOException e) {
            Log.e(TAG, "error of flushing output stream on renderHeaders");
            e.printStackTrace();
        } finally {
            mHeadersIsSended = true;
            writeln();
        }
    }

    /** Write some string into output stream **/
    private void write(String s) {
        try {
            mOutputStream.write(s.getBytes("UTF-8"));
        } catch (IOException e) {
            Log.e(TAG, "write to output stream error");
            e.printStackTrace();
        }
    }

    /** Write some string and new line into output stream **/
    private void writeln(String s) {
        this.write(s + "\n");
    }

    /** Write is empty line **/
    private void writeln() {
        writeln("");
    }
}
