package ru.ds.AndroidHttpServer.Parser.HeaderParser.Data;

import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Class provided data which contains information about a header of a request
 *
 * HeaderName:HeaderValue;HeaderParamKey=HeaderParamVal1;...
 */
public class Header  {
    private static final String TAG = "Header";
    private String headerValueString;
    private String mKey  ;
    private String mValue;
    private HashMap<String, String> mHeaderParameters;
    private boolean mValid;

    public Header(String key, String valueString) {
        this.mKey   = key;
        this.mValid = true;
        this.headerValueString = valueString;
        if (valueString.isEmpty()) {
            mValid = false;
            return;
        }
        parse(valueString);
    }

    /**
     * overflow, for init other header types;
     */
    public void parse(String valueString) {
        if (!valueString.contains(";")) {
            mValue = valueString;
        }
        String[] headerParametersArrray = valueString.split(";");
        if (headerParametersArrray.length < 1) {
            return ;
        }
        mValue = headerParametersArrray[0];
        mHeaderParameters = new HashMap<String, String>();
        for (int i=1; i<headerParametersArrray.length; i++) {
            String[] paramKeyValue = headerParametersArrray[i].split("=");
            if (paramKeyValue.length != 2) {
                continue;
            }
            String paramKey   = paramKeyValue[0].trim().toLowerCase();
            String paramValue = paramKeyValue[1].trim();

            if (paramValue.startsWith("\"")) {
                paramValue = paramValue.substring(1);
            }
            if (paramValue.endsWith("\"")) {
                paramValue = paramValue.substring(0, paramValue.length()-1);
            }

            mHeaderParameters.put(paramKey, paramValue);
        }
    }

    public String getKey() {
        return mKey;
    }

    public String getValue() {
        return mValue;
    }

    public boolean isValid() {
        return mValid;
    }

    public String getParameterValue(String parameterKey) {
        return mHeaderParameters.get(parameterKey);
    }

    /** convert value to int format**/
    public int getInt() {
        try {
            return Integer.parseInt(getValue());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** convert value to long format**/
    public long getLong() {
        try {
            return Long.parseLong(getValue());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** return value as string **/
    public String getString() {
        return getValue();
    }
}
