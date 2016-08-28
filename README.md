# NetworkHelper-Volley
A networking library for Android that builds on top of Volley from Google.

This library has been developed in order to extend Volley, giving new features like:
  - Add ParserRequest class to parse JSON responses with Gson (or if you'd like to use Jackson, or any other serialization library, you can use that as well, but Gson is the default)
  - Support for configuring requests to only work on specific network connections (i.e WiFi or LTE).
  - Use annotations to avoid writing boilerplate code


## Usage
You can import this library from jCenter with Gradle:
```gradle
compile 'com.appsquare:networkhelper:1.0.1'
```

```java
@RequestMethod(method = Request.Method.POST, url = "/users/{userId}")
@ExpectedStatusCode(values = {200, 204}) // optional, here the response's status code can be set (from 200-299) that we accept
public class ModifyUserRequest extends ParserRequest<User> {
    @PathParam  //(this annotation can also be placed on a getter, like getUserId())
    private Long userId;

    @QueryParam
    private boolean enabled;
    
    @HeaderParam
    private boolean token;
    
    public ModifyUserRequest(User user, boolean enabled, String token, Response.Listener<ParsedResponse<User>> listener, Response.ErrorListener errorListener) {
        super(listener, errorListener);
        this.userId = user.getId();
        this.enabled = enabled;
        this.token = token;
        
        // this is optional, you can set any other encoder here, but these two implementations are available in the library
        setBodyEncoder(new GsonBodyMapper()); // Gson will be used to serialize your request DTO
        setBodyEncoder(new FormBodyMapper()); // the request DTO will be serialized as a simple POST request, with the following Content-Type: application/x-www-form-urlencoded.
        
        // this is optional, you can set any other decoder here, for example an XML parser
        setResponseDecoder(new GsonBodyMapper()); 
        
        // set the user object, so it will be serialized and placed in the body of the request
        setRequestDto(user);
        
        // optionally set that this request can only be sent via WiFi
        setConnectionType(ConnectivityManager.TYPE_WIFI);
}
```

And then use it in your code:
```java
        User user = new User("First Name", "Last Name", "hello@android.com");
        new ModifyUserRequest(user, true, "token", new Response.Listener<ParsedResponse<User>>() {
            @Override
            public void onResponse(ParsedResponse<User> response) {
                User user = response.getParsedResponse();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle error here
            }
        }).send(context);
```

## Futures
It is also possible to use futures and execute it either synchronously or asynchronously. Just create the request and call the getFuture() method on it, like below:

```java
        new ModifyUserRequest(user, true, "token", null, null).getFutureAndSend().execute();
```

## Contributions
Please feel free to submit and fix bugs, or even make a pull request to this library. :)

## Author
Gabor Szanto, 2016
