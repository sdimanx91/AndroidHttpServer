package ru.ds.AndroidHttpServer.Requests;

import android.content.Context;
import android.util.Log;

import ru.ds.AndroidHttpServer.Const.HTTPMethods;
import ru.ds.AndroidHttpServer.HttpRequest;
import ru.ds.AndroidHttpServer.HttpResponse;

/**
 * Http request userProcessor
 *
 * this is function's called in the background thread.
 * if you need execute the code in ui thread, use Handler or runOnUiThread of Activity, etc.
 *
 * @see android.os.Handler
 * @see android.app.Activity
 */
public class HttpRoutingRequest {

    /**
     * Call the method function
     * @see ru.ds.AndroidHttpServer.Const.HTTPMethods
     */
    public boolean callMethod(HttpRequest request, HttpResponse response, Context context) {
        String method = request.getRequestData().getMethod();
        if (method.equals(HTTPMethods.OPTIONS)) {
            return doOptions(request, response, context);
        } else if (method.equals(HTTPMethods.GET)) {
            return doGet(request, response, context);
        } else if (method.equals(HTTPMethods.POST)) {
            return doPost(request, response, context);
        } else if (method.equals(HTTPMethods.HEAD)) {
            return doHead(request, response, context);
        } else if (method.equals(HTTPMethods.PUT)) {
            return doPut(request, response, context);
        } else if (method.equals(HTTPMethods.DELETE)) {
            return doDelete(request, response, context);
        } else if (method.equals(HTTPMethods.TRACE)) {
            return doTrace(request, response, context);
        } else if (method.equals(HTTPMethods.CONNECT)) {
            return doConnect(request, response, context);
        } else if (method.equals(HTTPMethods.PATCH)) {
            doPatch(request, response, context);
        } else if (method.equals(HTTPMethods.COPY)) {
            doCopy(request, response, context);
        } else if (method.equals(HTTPMethods.LINK)) {
            return doLink(request, response, context);
        } else if (method.equals(HTTPMethods.UNLINK)) {
            return doUnlink(request, response, context);
        } else if (method.equals(HTTPMethods.PURGE)) {
            return doPurge(request, response, context);
        } else if (method.equals(HTTPMethods.LOCK)) {
            return doLock(request, response, context);
        } else if (method.equals(HTTPMethods.UNLOCK)) {
            return doUnlock(request, response, context);
        } else if (method.equals(HTTPMethods.VIEW)) {
            return doView(request, response, context);
        } else if (method.equals(HTTPMethods.PROPFIND)) {
            return doProfind(request, response, context);
        }
        return false;
    }

    protected boolean doOptions(HttpRequest request, HttpResponse response, Context context) {return false;}
    protected boolean doGet(HttpRequest request, HttpResponse response, Context context) {return false;}
    protected boolean doPost(HttpRequest request, HttpResponse response, Context context) {return false;}
    protected boolean doHead(HttpRequest request, HttpResponse response, Context context) {return false;}
    protected boolean doPut(HttpRequest request, HttpResponse response, Context context) {return false;}
    protected boolean doDelete(HttpRequest request, HttpResponse response, Context context) {return false;}
    protected boolean doTrace(HttpRequest request, HttpResponse response, Context context) {return false;}
    protected boolean doConnect(HttpRequest request, HttpResponse response, Context context) {return false;}
    protected boolean doPatch(HttpRequest request, HttpResponse response, Context context) {return false;}
    protected boolean doCopy(HttpRequest request, HttpResponse response, Context context) {return false;}
    protected boolean doLink(HttpRequest request, HttpResponse response, Context context) {return false;}
    protected boolean doUnlink(HttpRequest request, HttpResponse response, Context context) {return false;}
    protected boolean doPurge(HttpRequest request, HttpResponse response, Context context) {return false;}
    protected boolean doLock(HttpRequest request, HttpResponse response, Context context) {return false;}
    protected boolean doUnlock(HttpRequest request, HttpResponse response, Context context) {return false;}
    protected boolean doView(HttpRequest request, HttpResponse response, Context context) {return false;}
    protected boolean doProfind(HttpRequest request, HttpResponse response, Context context) {return false;}

}
