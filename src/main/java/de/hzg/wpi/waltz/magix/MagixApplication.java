package de.hzg.wpi.waltz.magix;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * @author ingvord
 * @since 18.06.2020
 */
@ApplicationPath("/api")
public class MagixApplication extends Application {

    @Override
    public Set<Object> getSingletons() {
        Set<Object> singletons = new HashSet<>();

        singletons.add(new MagixRestService());

        return singletons;
    }
}
