package com.abcft.apes.vitamin.filters;

import org.glassfish.jersey.server.ContainerResponse;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import java.io.IOException;

/**
 * Created by zhyzhu on 17-5-11.
 */
public class CORSFilter implements ContainerResponseFilter {

    static String ORIGIN_HOST = "127.0.0.1";

    public static void init(String origin) {
        ORIGIN_HOST = origin;
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                                    ContainerResponseContext responseContext)
            throws IOException {
        responseContext.getHeaders().add("Access-Control-Allow-Origin", ORIGIN_HOST);
        responseContext.getHeaders().add("Access-Control-Allow-Headers",
                "origin, content-type, accept, authorization");
        responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
        responseContext.getHeaders().add("Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, OPTIONS, HEAD");
    }
}
