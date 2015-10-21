package ru.ds.AndroidHttpServer;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
//    final ByteBuffer mBodyBuffer                    = ByteBuffer.allocate(1024*1024*5);
    final HashMap<String, String> mCookies = new HashMap<String, String>();
    final List<byte[]> mBodyBuffer = new ArrayList<byte[]>();
    private int contentLength=0;

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

    /**
     * append cookie
     *
     * todo: cookie must contain expires or max-age
     * domain can be null
     * key and value cannot be null
     **/
    public void putCookie(String key, String value, String domain, String path, long maxAge, Date expires) {
        if (key == null || value == null) {
            return;
        }
        StringBuffer cookieBuffer = new StringBuffer();

        cookieBuffer.append(key);
        cookieBuffer.append("=");
        cookieBuffer.append(value);
        cookieBuffer.append(";");

        if (domain != null) {
            cookieBuffer.append("domain=\"");
            cookieBuffer.append(domain);
            cookieBuffer.append("\";");
        }

        if (path != null) {
            cookieBuffer.append("path=");
            cookieBuffer.append(path);
            cookieBuffer.append(";");
        }

        if (maxAge >= 0) {
            cookieBuffer.append("Max-Age=");
            cookieBuffer.append(maxAge);
            cookieBuffer.append(";");
        }

        mCookies.put(key, cookieBuffer.toString());
    }

    public void putCookie(String key, String value) {
        putCookie(key, value, null, null, -1L, null);
    }
    public void putCookie(String key, String value, long maxAge) {
        putCookie(key, value, null, null, maxAge, null);
    }
    public void putCookie(String key, String value, Date expires) {
        putCookie(key, value, null, null, -1L, expires);
    }
    public void putCookie(String key, String value, String path) {
        putCookie(key, value, null, path, -1L, null);
    }
    public void putCookie(String key, String value, String path, long maxAge) {
        putCookie(key, value, null, path, maxAge, null);
    }
    public void putCookie(String key, String value,String path, Date expires) {
        putCookie(key, value, null, path, -1L, expires);
    }


    /** put response to output stream */
    public void render() {
        if (mRendered) {
            return;
        }

        setHeader(DefaultHeaders.ContentLength, Integer.toString(contentLength));

        // write status
        writeToStream("HTTP/1.1 " + mStatus+"\n");

        // write headers
        Iterator headersIterator = mHeaders.entrySet().iterator();
        while (headersIterator.hasNext()) {
            HashMap.Entry<String, String> pair = (HashMap.Entry<String, String>) headersIterator.next();
            writeToStream(pair.getKey() + ": " + pair.getValue() + "\n");
        }

        Iterator cookiesIterator = mCookies.entrySet().iterator();
        while (cookiesIterator.hasNext()) {
            HashMap.Entry<String, HttpCookie> pair = (HashMap.Entry<String, HttpCookie>) cookiesIterator.next();
            writeToStream(DefaultHeaders.SetCookie + ":" + pair.getValue() + "\n");
        }

        // empty string before body
        writeToStream("\n");

        //write body
        Iterator<byte[]> bodyBufferIterator = mBodyBuffer.iterator();
        while (!mBodyBuffer.isEmpty()) {
            byte[] mBodyCh = bodyBufferIterator.next();
            if (mBodyCh != null) {
                writeToStream(mBodyCh);
            }
            bodyBufferIterator.remove();
        }
        mRendered = true;
    }

    /** Write bytes into output stream **/
    public void writeBytes(byte[] bytes) {
        if (mRendered) {
            return;
        }
        if (mBodyBuffer.add(bytes)) {
            contentLength += bytes.length;
        }
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
            mOutputStream.write(bytes);
            mOutputStream.flush();
        } catch (IOException e) {
            Log.e(TAG, "write bytes to output stream error");
            e.printStackTrace();
        }
    }
}
