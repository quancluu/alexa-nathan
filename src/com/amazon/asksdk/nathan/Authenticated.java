package com.amazon.asksdk.nathan;

/**
 * Created by qcluu on 11/18/17.
 */
public class Authenticated {
    String token_type;
    String access_token;
    public Authenticated(final String token_type, final String access_token) {
        this.token_type = token_type;
        this.access_token = access_token;
    }

    public String getToken_type() {
        return token_type;
    }

    public String getAccess_token() {
        return access_token;
    }
}