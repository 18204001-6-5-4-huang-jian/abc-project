package com.abcft.apes.vitamin.filters;

import java.security.Principal;

/**
 * Created by zhyzhu on 17-4-23.
 */
public class AuthorPricinple implements Principal {
    private String name;

    public AuthorPricinple(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
