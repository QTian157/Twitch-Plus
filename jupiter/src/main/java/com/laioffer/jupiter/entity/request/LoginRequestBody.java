package com.laioffer.jupiter.entity.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
// Let’s create the classes for JSON serialization and deserialization in the entity package/folder.
//
// LoginRequestBody.java
// For store the login information sent from frontend

public class LoginRequestBody {
    private final String userId;
    private final String password;

    @JsonCreator // 和直接在fied上加 @JsonProperty("name") 一样
    // https://www.tutorialspoint.com/jackson_annotations/jackson_annotations_jsoncreator.htm
    public LoginRequestBody(@JsonProperty("user_id") String userId, @JsonProperty("password") String password) {
        this.userId = userId;
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }
}
