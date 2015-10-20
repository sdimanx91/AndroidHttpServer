package ru.ds.AndroidHttpServer.Const;

/**
 * list of the http methods
 */
public class HTTPMethods {
    public final static String OPTIONS   = "OPTIONS";
    public final static String GET       = "GET";
    public final static String HEAD      = "HEAD";
    public final static String POST      = "POST";
    public final static String PUT       = "PUT";
    public final static String DELETE    = "DELETE";
    public final static String TRACE     = "TRACE";
    public final static String CONNECT   = "CONNECT";
    public final static String PATCH     = "PATCH";
    public final static String COPY      = "COPY";
    public final static String LINK      = "LINK";
    public final static String UNLINK    = "UNLINK";
    public final static String PURGE     = "PURGE";
    public final static String LOCK      = "LOCK";
    public final static String UNLOCK    = "UNLOCK";
    public final static String VIEW      = "VIEW";
    public final static String PROPFIND  = "PROPFIND";

    /**
     * List of the methods which request contain the body
     */
    public final static String[] MethodsContainsARequestBody = new String[] {
        POST, PUT, PATCH, DELETE, OPTIONS, LINK, UNLINK, LOCK, PROPFIND
    };

    /**
     * @param methodName checked method
     * @return MethodsContainsARequestBody contain the methodName
     */
    public final static boolean MethodContainsTheBody(String methodName) {
        for (int i=0; i<MethodsContainsARequestBody.length; ++i) {
            if (MethodsContainsARequestBody[i].equals(methodName)) {
                return true;
            }
        }
        return false;
    }


}
