package ru.ds.AndroidHttpServer.Parser.Body;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import ru.ds.AndroidHttpServer.Const.DefaultHeaders;
import ru.ds.AndroidHttpServer.Parser.HeaderParser.Data.Header;
import ru.ds.AndroidHttpServer.Parser.HeaderParser.Head;

/**
 * value of request, if Content-Type header value a multipart
 */
public class MultipartValue {
    private final static String NAME_HEADER      = "name";
    private final static String FILE_NAME_HEADER = "filename";
    private static final String TAG = "MultipartValue";

    private HashMap<String, Header>  mPartHeaders = new HashMap<String, Header>();
    private String mPartKey;
    private boolean mValid;
    private String mBody = "";

    public MultipartValue(ArrayList<String> multipartString) {
        readHeaders(multipartString);
        Header contentDisposition = mPartHeaders.get(DefaultHeaders.ContentDisposition);
        if (contentDisposition == null ||
            contentDisposition.getParameterValue(NAME_HEADER) == null ||
            contentDisposition.getParameterValue(NAME_HEADER).isEmpty())
        {
            mValid = false;
            return;
        }
        mPartKey = contentDisposition.getParameterValue(NAME_HEADER);

        Log.d(TAG, "nameKeyValue = " + mPartKey);
        readBody(multipartString);
        Log.d(TAG, "body=" + mBody);
        mValid = (mBody.length()>0);
    }

    /**
     * @return the key of current part
     */
    public String getPartKey() {
        return mPartKey;
    }

    public String getBody() {
        return mBody;
    }

    /**
     * @hide
     * internal use only
     */
    public boolean isValid() {
        return mValid;
    }

    private void readHeaders(ArrayList<String> multipartString) {
        Iterator<String> iterator = multipartString.iterator();
        while (iterator.hasNext()) {
            String value = iterator.next();
            Log.d(TAG, value);
            iterator.remove();
            if (value.trim().isEmpty()) {
                return;
            }
            Header header = Head.parseHeader(value);
            if (header != null) {
                mPartHeaders.put(header.getKey(), header);
            }
        }
    }

    private void readBody(ArrayList<String> multipartString) {
        // all other strings - body
        StringBuffer sBuffer = new StringBuffer();
        Iterator<String> iterator = multipartString.iterator();
        while (iterator.hasNext()) {
            String value = iterator.next();
            sBuffer.append(value);
        }
        mBody = sBuffer.toString();
    }
}
