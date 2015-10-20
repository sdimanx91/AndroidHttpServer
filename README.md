Lite Android HTTP Server. 
!Not yet tested.

### How to use.

* Import module at Android Studio
* Create instance of class HttpRouter
```
  HttpRouter router = new HttpRouter();
```
* Create class inherited from HttpRoutingRequest

```
  public class ApiExample extends HttpRoutingRequest {}
```
* Override a necessary methods.

```
   public class ApiExample extends HttpRoutingRequest {
      
      //You can use doPost, doOptions, etc...
      @Override
      public void doGet(HttpRequest request, HttpResponse response) {
        // If this method returns false then response will contain 404 error.
        return true;
      }
   }
```
* Append created API to router
```
  router.route("/ApiTest", new ApiExample()); 
```
* Use static folder
```
  router.routeStatic("/getPage", "static/html", "index.html)
```
or (if default page is <i>index.html</i>)
```
  router.routeStatic("/getPage", "static/html")
```
* Start listen
```
  http = HttpServerSocket.Listen(9090, router);
```
* Stop listen
```
  http.stop();
  http = null;
```

