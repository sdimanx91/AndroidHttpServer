package ru.ds.AndroidHttpServer;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import java.io.BufferedInputStream;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpCookie;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import ru.ds.AndroidHttpServer.Const.DefaultHeaders;
import ru.ds.AndroidHttpServer.Const.HTTPMethods;
import ru.ds.AndroidHttpServer.Parser.Body.FormData;
import ru.ds.AndroidHttpServer.Parser.Body.MultipartFormData;
import ru.ds.AndroidHttpServer.Parser.Body.MultipartValue;
import ru.ds.AndroidHttpServer.Parser.HeaderParser.ContentType;
import ru.ds.AndroidHttpServer.Parser.HeaderParser.Data.FirstLine;
import ru.ds.AndroidHttpServer.Parser.HeaderParser.Data.Header;
import ru.ds.AndroidHttpServer.Parser.HeaderParser.Head;

/**
 * Parse the http request
 */
public class HttpRequest {
    final static private String TAG               = "HttpRequest";
    final static private long   MAX_BUFFER_SIZE   = 1024*1024*5;
    final static private long   MAX_STRING_LENGTH = 1024*5;

    final private HashMap<String, Header> mHeaders = new HashMap<String, Header>();

    private Socket    mSocket      = null;
    private FormData  mFormData    = null;
    private FirstLine mRequestData = null;
    private byte[]    mBinary;    // binary data in the request body (if contains)

    public HttpRequest() { }

    /**
     * get the data which contains in the first line of the request
     */
    public FirstLine getRequestData() {
        return mRequestData;
    }

    /**
     * Get connection socket
     */
    public Socket getSocket() {
        return mSocket;
    }

    /**
     * Parsing mUrl in the request and return it as Uri object
     * @return parsed Uri
     */
    public URL getUrl() {
        String host = getHeaderValue(DefaultHeaders.Host);
        if (host == null || host.isEmpty()) {
            host = "";
        } else {
            if (!host.startsWith("http")) {
                host = "http://" + host;
            }
        }
        if (mRequestData == null) {
            return null;
        }
        String url = mRequestData.getUrlString();
        if (url == null || url.isEmpty()) {
            return null;
        }
        if (host.isEmpty())
        {
            //todo: add the current host
            host = "http:/localhost";
        }

        // getting the request
        String urlString;
        urlString = host + url;
        URL retUrl = null;
        try {
            retUrl = new URL(urlString);
        } catch (MalformedURLException e) {
            Log.e(TAG, "malformed url: " + urlString);
            e.printStackTrace();
            return null;
        }
        return retUrl;
    }

    /**
     * return Post, Put, etc... FormValue as String (for x-www-form-urlencoded only).
     * @param key This is a key from the request
     * @return if String with a key contains in the mFormData return that String, null otherwise
     */
    public String getFormValue(String key) {
        if (mFormData == null) {
            return  "";
        }
        return mFormData.getFormValue(key);
    }

    /**
     * @return List of cookies from the request
     */
    public List<Pair<String, String>> getCookies() {
        Header cookieHeader = getHeader(DefaultHeaders.Cookie);
        if (cookieHeader == null) {
            return null;
        }
        List<Pair<String, String>> buffer= new ArrayList<Pair<String, String>>();
        String[] cookies = cookieHeader.getHeaderValue().split(";");
        if (cookies == null || cookies.length == 0) {
            return null;
        }

        for (  int i=0; i<cookies.length; i++  ) {
            String cookiePairString = cookies[i].trim();
            int eqIndex = cookiePairString.indexOf("=");
            if (eqIndex <=0 || eqIndex >= cookiePairString.length()-1) {
                continue;
            }
            String key = cookiePairString.substring(0, eqIndex);
            String value = cookiePairString.substring(eqIndex+1);
            buffer.add(new Pair<String, String>(  key, value  ));
        }
        return buffer;
    }

    /**
     * return Post, Put, etc... FormValue as MultipartValue (for multipart only).
     * @param key This is a key from the request
     * @return if String with a key contains in the mFormData return that String, null otherwise
     * @see ru.ds.AndroidHttpServer.Parser.Body.MultipartValue
     */
    public MultipartValue getMultipartFormValue(String key) {
        if (mFormData == null) {
            return null;
        }
        if (mFormData instanceof MultipartFormData) {
            return ((MultipartFormData) mFormData).getValue(key);
        }
        return null;
    }

    /**
     * return form keys set
     */
    public Set<String> getFormKeysSet() {
        if (mFormData == null) {
            return null;
        }
        return mFormData.getFormKeysSet();
    }

    /** get all header names as Set **/
    public Set<String> getHeadersSet() {
        return mHeaders.keySet();
    }

    /**
     * @param headerKey the header name
     * @return the header value, if exists or null
     * @see ru.ds.AndroidHttpServer.Const.DefaultHeaders
     * @see ru.ds.AndroidHttpServer.Parser.HeaderParser.Data.Header
     */
    public Header getHeader(String headerKey) {
       return mHeaders.get(headerKey.toLowerCase());
    }

    /**
     * get the row data from the http body
     */
    public String getRawString() {
        return new String(mBinary);
    }

    /**
     * get the binary (octet) from the http body
     */
    public byte[] getBinaryData() {
        return mBinary;
    }

    /**
     * get the header string value
     * if header is not exists return null
     */
    public String getHeaderValue(String headerKey) {
        Header header = getHeader(headerKey);
        if (header == null) {
            return null;
        }
        return header.getString();
    }

    /**
     * @return parsed Content-Type header value
     * @see ru.ds.AndroidHttpServer.Parser.HeaderParser.ContentType
     */
    public ContentType getContentType() {
        return new ContentType(
                getHeader(DefaultHeaders.ContentType)
        );
    }

    /**
     * @return value of Content-Length header as long
     */
    public long getContentLength() {
        Header contentLengthHeader = getHeader(DefaultHeaders.ContentLength);
        if (contentLengthHeader == null) {
            return 0;
        }
        return contentLengthHeader.getLong();
    }

    /**
     * The builder of the HttpRequest
     */
    public static class HttpRequestBuilder {
        private static final String TAG = "HttpRequestBuilder";
        HttpRequest buffered = new HttpRequest();

        /**
         * It gets the data in reader (BufferedReader) and it parses them;
         * @param inputStream InputStream of the socket
         * @return new HttpRequestBuilder
         */
        public static HttpRequestBuilder parse(InputStream inputStream, Socket socket) {
            final HttpRequestBuilder builder = new HttpRequestBuilder();
            builder.buffered.mSocket = socket;
            String firstLine;
            try {
                firstLine = HttpRequestBuilder.readStringFromBuffer(inputStream);
            } catch (IOException e) {
                Log.e(TAG, "Failed parse first line of request.");
                return null;
            }
            if (firstLine == null || firstLine.trim().isEmpty()) {
                Log.e(TAG, "First line of the request is empty");
                return null;
            }
            FirstLine firstLineParsed = Head.parseFirstLine(firstLine);
            if (firstLine == null) {
                Log.e(TAG, "Failed parse first line of request.");
                return null;
            } else {
                builder.buffered.mRequestData = firstLineParsed;
            }

            if (!builder.parseHead(inputStream, new ParserHeaderInterface() {
                @Override
                public void use(Header header) {
                    if (header != null) {
                        builder.buffered.mHeaders.put(header.getKey(), header);
                    }
                }
            })) {
                Log.e(TAG, "Failed parse headers of request");
                return null;
            }

            if (!builder.parseBody(inputStream, inputStream)) {
                Log.e(TAG, "Failed parse body of request");
                return null;
            }
            return builder;
        }

        /**
         * for context usage
         */
        public static HttpRequestBuilder parse(InputStream inputStream, Socket socket, Context context) {
            HttpRequestBuilder builder = parse(inputStream, socket);
            return builder;
        }

        /**
         * parse the head of request (firstLine and headers)
         * @return successfully parsed
         */
        private boolean parseHead(InputStream inputStream, ParserHeaderInterface onParse) {
            try {
                String line = readStringFromBuffer(inputStream);
                // parse other headers
                while (line != null && !line.trim().isEmpty()) {
                    Header header = Head.parseHeader(line);
                    if (header == null || (header != null && !header.isValid())) {
                        Log.e(TAG, "invalid header: " + line);
                    } else {
                        onParse.use(header);
                    }
                    line = readStringFromBuffer(inputStream);
                }
            } catch (IOException e) {
                Log.e(TAG, "read buffer error");
                return false;
            }
            return true;
        }

        /**
         * parse the body of the request
         * body contains the value  depending on Content-Type header
         * Content-Type can be form-data, x-www-form-urlencoded, row, binary
         *
         * @param reader Buffer of request content
         * @return successfully parsed
         */
        private boolean parseBody(InputStream io, InputStream stream) {
            if (!HTTPMethods.MethodContainsTheBody(buffered.getRequestData().getMethod())) {
                // This request does not contain a body
                return true;
            }
            // parse the content-size of the body
            long contentLength = buffered.getContentLength();
            if (contentLength < 0) {
                Log.e(TAG, "Incorrect Content-Length header format.");
                return false;
            }
            if (contentLength > MAX_BUFFER_SIZE) {
                Log.e(TAG, "Request size exceeds the maximum size. Maximum size = " + Long.toString(MAX_BUFFER_SIZE) + ".");
                return false;
            }
            ContentType contentType = buffered.getContentType();
            if (contentType.isRaw()) {
                return parseBinary(stream, contentLength);
            } else if (contentType.isXWwwFormUrlencoded()) {
                buffered.mFormData = new FormData(io, contentLength);
                return true;
            } else if (contentType.isUntypedBinary()) {
                return parseBinary(stream, contentLength);
            } else if (contentType.isMultipartFormData()) {
                buffered.mFormData = new MultipartFormData(io, contentType);
                return true;
            }
            return false;
        }

        /** parse binary data **/
        private boolean parseBinary(InputStream stream, long bodySize) {
            int intSize = (int)bodySize;
            buffered.mBinary = new byte[intSize];
            try {
                if (intSize > stream.available()) {
                    return false;
                }
                int readedBytesCount = stream.read(buffered.mBinary, 0, intSize);
                if (readedBytesCount != bodySize) {
                    return false;
                } else {
                    return true;
                }
            } catch (IOException e) {
                Log.e(TAG, "read octet error exception.");
                e.printStackTrace();
                buffered.mBinary = null;
                return false;
            }
        }

        /**
         * after call this method the field buffered will have cleared
         * @return the buffered and filled request
         */
        public HttpRequest get() {
            return buffered;
        }

        private interface ParserHeaderInterface {
            void use(Header parsedHeader);
        }

        /**
         * @hide
         * for internal use only
         * @param reader
         * @return
         * @throws IOException
         */
        public static String readStringFromBuffer(InputStream inputStream) throws IOException {
            if (inputStream.available() == 0 || inputStream.available() > MAX_STRING_LENGTH) {
                return null;
            }
            int counted=0;
            int newChar=-1;
            ByteBuffer byteBuffer = ByteBuffer.allocate((int)MAX_STRING_LENGTH);
            do {
                if (counted >= MAX_STRING_LENGTH) {
                    break;
                }
                if (inputStream.available() == 0) {
                    break;
                }
                newChar = inputStream.read();
                byteBuffer.put((byte) newChar);
                counted++;
                // 10 - \n 13 - \r
                if (newChar == 10) {
                    break;
                }
            } while (true);
            return new String(byteBuffer.array(), "UTF-8");
        }
    }
}
