package ru.ds.AndroidHttpServer.Parser.HeaderParser.Data;

/**
 * Class provided data which contains information about a first line of a request
 */
public class FirstLine {
    private String mProtocol;
    private String mUrl;
    private String mVersion;
    private String mMethod;

    public FirstLine(String protocol, String url, String version, String method) {
        this.mProtocol = protocol;
        this.mUrl = url;
        this.mVersion  = version;
        this.mMethod = method;
    }

    public String getProtocol() {
        return mProtocol;
    }

    /**
     * @return Had url string specified in the first line of the  request
     */
    public String getUrlString() {
        return mUrl;
    }

    /**
     * @return Had http version string specified in the first line of the  request
     */
    public String getHttpVersion() {
        return mVersion;
    }

    /**
     * @return request's http method
     */
    public String getMethod() {
        return mMethod;
    }
}
