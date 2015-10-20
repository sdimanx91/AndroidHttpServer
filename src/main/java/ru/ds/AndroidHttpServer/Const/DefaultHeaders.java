package ru.ds.AndroidHttpServer.Const;

/**
 * List of available http headers
 */
public class DefaultHeaders {

    // request only
    public static final String Accept             = "accept";
    public static final String AcceptCharset      = "accept-charset";
    public static final String AcceptEncoding     = "accept-encoding";
    public static final String AcceptLanguage     = "accept-language";
    public static final String Authorization      = "authorization";
    public static final String Expect             = "expect";
    public static final String Host               = "host";
    public static final String IfMatch            = "if-match";
    public static final String IfModifiedSince    = "if-modified-since";
    public static final String IfNoneMatch        = "if-none-match";
    public static final String IfRange            = "if-range";
    public static final String IfUnmodifiedSince  = "if-unmodified-since";
    public static final String MaxForwards        = "max-forwards";
    public static final String ProxyAuthorization = "proxy-authorization";
    public static final String Range              = "range";
    public static final String Referer            = "referer";
    public static final String TE                 = "te";
    public static final String UserAgent          = "useragent";

    // response only
    public static final String Age                = "age";
    public static final String Allow              = "allow";
    public static final String Alternates         = "alternates";
    public static final String ETag               = "etag";
    public static final String Location           = "location";
    public static final String ProxyAuthenticate  = "proxy-authenticate";
    public static final String Public             = "public";
    public static final String RetryAfter         = "retry-after";
    public static final String Server             = "server";
    public static final String AcceptRange        = "accept";
    public static final String Vary               = "vary";
    public static final String WWWAuthenticate    = "www-authenticate";

    // general
    public static final String CacheControl       = "cache-control";
    public static final String Connection         = "connection";
    public static final String Date               = "date";

    // entity
    public static final String ContentDisposition = "content-disposition";
    public static final String ContentEncoding    = "content-encoding";
    public static final String ContentLanguage    = "content-language";
    public static final String ContentLength      = "content-length";
    public static final String ContentMD5         = "content-md5";
    public static final String ContentRange       = "content-range";
    public static final String ContentType        = "Content-Type";
    public static final String ContentVersion     = "content-version";
    public static final String DerivedFrom        = "content-from";
    public static final String From               = "from";
    public static final String Link               = "link";
    public static final String LastModified       = "last-modified";
    public static final String MIMEVersion        = "mime-version";
    public static final String Pragma             = "pragma";
    public static final String Title              = "title";
    public static final String Trailer            = "trailer";
    public static final String TransferEncoding   = "transfer-encoding";
    public static final String Upgrade            = "upgrade";
    public static final String Via                = "via";
    public static final String Warning            = "warning";
}
