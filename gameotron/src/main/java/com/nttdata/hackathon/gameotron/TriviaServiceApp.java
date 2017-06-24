package com.nttdata.hackathon.gameotron;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by HUETTM on 24.06.2017.
 */
@ApplicationPath("/gamotron")
public class TriviaServiceApp extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> classes = new HashSet<Class<?>>();
        classes.add(TriviaService.class);
        classes.add(MultiPartFeature.class);
        return classes;
    }
}
