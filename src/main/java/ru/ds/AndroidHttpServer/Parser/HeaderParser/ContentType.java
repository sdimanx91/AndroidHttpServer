package ru.ds.AndroidHttpServer.Parser.HeaderParser;

import android.util.Log;

import java.util.Iterator;

import ru.ds.AndroidHttpServer.Parser.HeaderParser.Data.Header;

/**
 * Content-Type header parser
 */
public class ContentType {

    private static final String CHARSET_FIELD = "charset";
    private static final String BOUNDARY_FIELD = "boundary";
    private static final String TAG = "HTTP.ContentType";

    private Header mHeader;
    private String mCharset;
    private String mType;
    private String mSubtype;
    private String mBoundary;

    public ContentType(Header header) {
        this.mHeader = header;
        if (mHeader == null) {
            return ;
        }
        String value = header.getString();
        String[] contentTypeArray = value.split("\\/");
        if (contentTypeArray.length != 2) {
            Log.e(TAG, "ContentType parse error. Incorrect format.");
            return;
        }

        mType     = contentTypeArray[0];
        mSubtype  = contentTypeArray[1];
        mBoundary = mHeader.getParameterValue(BOUNDARY_FIELD);
        mCharset  = mHeader.getParameterValue(CHARSET_FIELD);
    }

    /** @return charset of content-type */
    public String getCharset() {
        return mCharset;
    }

    /** @return type of content-type */
    public String getType() {
        return mType;
    }

    /** @return type of content-type */
    public String getSubtype() {
        return mSubtype;
    }

    /** @return bundary string**/
    public String getBoundary() {
        return mBoundary;
    }

    /**
     * type = application
     * subtype =x-www-form-urlencoded
     */
    public boolean isXWwwFormUrlencoded() {
        return     mType != null
                && mType.equals("application")
                && mSubtype != null
                && mSubtype.equals("x-www-form-urlencoded");
    }

    /**
     * type = application
     * subtype = form-data
     */
    public boolean isMultipartFormData() {
        return     mType != null
                && mType.equals("multipart")
                && mSubtype != null
                && mSubtype.equals("form-data");
    }

    // todo: This function handles only text/json/javascript value, but it can be equal other values
    public boolean isRaw() {
        if (mType != null && mType.equals("text")) {
            return true;
        }
        if (mSubtype != null && mType != null)
        {
            if (mType.equals("application") &&
                    mSubtype.equals("json") || mSubtype.equals("javascript"))
            {
                return true;
            }
        }
        return false;
    }

    /** binary data without indication format */
    public boolean isUntypedBinary() {
        return     mType != null
                && mType.equals("application")
                && mSubtype != null
                && mSubtype.equals("octet-stream");
    }

    /**  application/x-www-form-urlencoded;boundary=abrakadabra   **/
    public boolean boundaryContains() {
        return isStringExists(mBoundary);
    }

    /** application/x-www-form-urlencoded;charset=utf-8 **/
    public boolean charsetExists() {
        return isStringExists(mCharset);
    }

    private boolean isStringExists(String checkedValue) {
        return (checkedValue != null && !checkedValue.isEmpty());
    }
}
