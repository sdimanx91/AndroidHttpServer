package ru.ds.AndroidHttpServer.Parser.HeaderParser;

import android.util.Log;

import ru.ds.AndroidHttpServer.Parser.HeaderParser.Data.FirstLine;
import ru.ds.AndroidHttpServer.Parser.HeaderParser.Data.Header;

/**
 * This is the class which parses a strings in a head of a request.
 */
public class Head {

    private static final String TAG = "Parser.Headers.Head";

    /** Parse the first line of the request **/
    public static FirstLine parseFirstLine(String firstLineString) {
        String protocol;
        String url;
        String version;
        String method;

        final String[] firstValues = firstLineString.trim().split("\\ ");
        if (firstValues.length != 3) {
            Log.e(TAG, "incorrect first the http record format.");
            return null;
        }

        method = firstValues[0].toUpperCase().trim();
        url    = firstValues[1].toLowerCase().trim();

        String[] httpVersionArr = firstValues[2].trim().split("\\/");
        if (httpVersionArr.length != 2) {
            Log.e(TAG, "incorrect version record in the first http string");
            return null;
        }
        protocol = httpVersionArr[0].toLowerCase().trim();
        version  = httpVersionArr[1].toLowerCase().trim();

        return new FirstLine(protocol, url, version, method);
    }

    /** Parse the header **/
    public static Header parseHeader(String headerString) {
        int indexOfFirstDots = headerString.indexOf(":");
        if (indexOfFirstDots <= 0 || indexOfFirstDots >= headerString.length()-1) {
            Log.e(TAG, "header format error");
            return null;
        }
        String headerName  = headerString.substring(0, indexOfFirstDots).trim().toLowerCase();
        String headerValue = headerString.substring(indexOfFirstDots+1).trim().toLowerCase();

        return new Header(headerName, headerValue);
    }

}
