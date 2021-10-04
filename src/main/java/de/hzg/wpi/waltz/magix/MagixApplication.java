package de.hzg.wpi.waltz.magix;

import org.jboss.resteasy.plugins.interceptors.CorsFilter;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * @author ingvord
 * @since 18.06.2020
 */
@ApplicationPath("/")
public class MagixApplication extends Application {

    @Override
    public Set<Object> getSingletons() {
        Set<Object> singletons = new HashSet<>();

        singletons.add(getCorsFilter());

        singletons.add(new MagixRestService());

        return singletons;
    }

    private CorsFilter getCorsFilter() {
        CorsFilter cors = new CorsFilter();
        cors.getAllowedOrigins().add("*");
        cors.setAllowCredentials(true);
        cors.setAllowedMethods("GET,POST,PUT,DELETE,HEAD");
        cors.setCorsMaxAge(1209600);
        cors.setAllowedHeaders("Origin,Accept,X-Requested-With,Content-Type,Access-Control-Request-Method,Access-Control-Request-Headers,Authorization,Accept-Encoding,Accept-Language,Access-Control-Request-Method,Cache-Control,Connection,Host,Referer,User-Agent");
        return cors;
    }
}
