# NetworkHelper-Volley
A networking library for Android that builds on top of Volley from Google.

This library has been developed in order to extend Volley, giving new features like:
  - Add ParserRequest class to parse JSON responses with Gson (or if you'd like to use Jackson, or any other serialization library, you can use that as well)
  - Use annotations to avoid writing boilerplate code

## Usage
You can import this library from jCenter with Gradle:
```gradle
compile 'com.appsquare:networkHelper:0.9.11'
```

```java
@RequestMethod(method = Request.Method.POST, url = "/users/{userId}")
public class ModifyUserRequest extends ParserRequest<User> {
    @PathParam
    private Long userId;

    @QueryParam
    private boolean enabled;
    
    @HeaderParam
    private boolean token;
    
    public ModifyUserRequest(Long userId, boolean enabled, String token, Response.Listener<ParsedResponse<User>> listener, Response.ErrorListener errorListener) {
        super(new TypeToken<User>(){}.getType(), listener, errorListener);
        this.userId = userId;
        this.enabled = enabled;
        this.token = token;
}
```

And then use it in your code:
```java
        new ModifyUserRequest(1L, true, "token", new Response.Listener<ParsedResponse<User>>() {
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

## Contributions
Please feel free to submit and fix bugs, or even make a pull request to this library. :)

## Author
Gabor Szanto, 2016
