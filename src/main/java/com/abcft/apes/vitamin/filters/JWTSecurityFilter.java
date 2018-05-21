package com.abcft.apes.vitamin.filters;

import com.abcft.apes.vitamin.util.AccountUtil;
import com.abcft.apes.vitamin.util.StringUtils;
import com.abcft.apes.vitamin.util.TokenUtil;
import org.apache.log4j.Logger;
import org.glassfish.jersey.server.ContainerRequest;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.servlet.ServletContext;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;

/**
 * Created by zhyzhu on 17-4-23.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class JWTSecurityFilter implements ContainerRequestFilter {

    final static Logger logger = Logger.getLogger(JWTSecurityFilter.class);

    @Context
    ServletContext context;

    @Inject
    javax.inject.Provider<UriInfo> uriInfo;

    public static String extractJwtTokenFromAuthorizationHeader(String auth) {
        return auth.replaceFirst("token=", "").replace(" ", "");
    }

    private boolean filterGetPath(String path, String method) {
        HashSet<String> pathFilters = new HashSet<>();
        //pathFilters.add("test/hello");
        pathFilters.add("v1/products");
        pathFilters.add("v1/products/predictions");
        pathFilters.add("v1/plans");
        if ("get".equals(method) &&
            (pathFilters.contains(path) ||
                path.startsWith("v1/image")
                || path.startsWith("v1/orders/pay")
            )
            ) {
            return true;
        }

        return false;
    }

    private boolean filterPostPath(String path, String method) {
        HashSet<String> pathFilters = new HashSet<>();
        pathFilters.add("v1/account/login");
        pathFilters.add("v1/account/login-token");
        pathFilters.add("v1/account/login-admin");
        pathFilters.add("v1/account/logout");
        pathFilters.add("v1/account/register");
        pathFilters.add("v1/account/verify");
        pathFilters.add("v1/account/reset");
        pathFilters.add("v1/account/forget-pass");      //忘记密码
        pathFilters.add("v1/account/forget-resetpasswd");      //忘记密码
        pathFilters.add("v1/orders/create");
        pathFilters.add("v1/invite-code/verify");
        pathFilters.add("wechat/login");
        pathFilters.add("wechat/email/bind");
        pathFilters.add("wechat/email/verify");

        if ("post".equals(method) && pathFilters.contains(path)) {
            return true;
        }

        return false;
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException{
        String method = containerRequestContext.getMethod().toLowerCase();
        String path = ((ContainerRequest)containerRequestContext).getPath(true).toLowerCase();
        URL url = ((ContainerRequest) containerRequestContext).getBaseUri().toURL();
        String host = url.getHost().toLowerCase();

        // 共享图表
        boolean isLocalhost = host.equals("127.0.0.1");

        if (isLocalhost ||
            filterPostPath(path, method) ||
            filterGetPath(path, method)
            ) {
            // pass through the filter.
            containerRequestContext.setSecurityContext(
                new SecurityContextAuthorizer(uriInfo, new AuthorPricinple("pass"), new String[]{"pass"}));
            return;
        }

        //获取头信息中的token
        Cookie token =  ((ContainerRequest) containerRequestContext).getCookies().get("token");
        String authorizationHeader = token != null ? token.getValue() : null;

        //如果token为空抛出
        if (StringUtils.isEmpty(authorizationHeader)) {
            logger.warn("authorize header is empty: " + path) ;
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);//抛出未认证的错误
        }

        //把Bear Token换成Token
        String strToken = extractJwtTokenFromAuthorizationHeader(authorizationHeader);

        if (TokenUtil.isValid(strToken)){
            String id = TokenUtil.getName(strToken);
            String[] roles = TokenUtil.getRoles(strToken);
            int version = TokenUtil.getVersion(strToken);

            if(id !=null && roles.length!=0 && version!=-1){
                JsonObject userJson = AccountUtil.getUserById(id);
                if(userJson != null){
                    containerRequestContext.setSecurityContext(new SecurityContextAuthorizer(uriInfo, new AuthorPricinple(id), roles));
                    return;
                }
                else{
                    logger.warn("User not found " + id);
                }
            }
            else {
                logger.warn("name, roles or version missing from token");
            }
        }
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
}
