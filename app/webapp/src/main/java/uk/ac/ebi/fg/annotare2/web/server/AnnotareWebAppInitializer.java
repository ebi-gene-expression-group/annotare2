/*
 * Copyright 2009-2016 European Molecular Biology Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.ebi.fg.annotare2.web.server;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.netflix.governator.InjectorBuilder;
import com.netflix.governator.LifecycleInjector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fg.annotare2.core.AnnotarePluginModules;
import uk.ac.ebi.fg.annotare2.core.properties.AnnotareProperties;
import uk.ac.ebi.fg.annotare2.magetabcheck.CheckerModule;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

public class AnnotareWebAppInitializer extends GuiceServletContextListener {

    private final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private LifecycleInjector injector = null;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        logger.info("Annotare is starting up...");

        super.contextInitialized(servletContextEvent);

        ServletContext sc = servletContextEvent.getServletContext();
        sc.setAttribute("docbotBackendUrl", injector.getInstance(AnnotareProperties.class).getDocbotBackendUrl());
        sc.setAttribute(Injector.class.getName(), injector);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        logger.info("Annotare is shutting down...");

        ServletContext sc = servletContextEvent.getServletContext();
        sc.removeAttribute(Injector.class.getName());

        super.contextDestroyed(servletContextEvent);
        if (null != injector) {
            injector.shutdown();
        }
    }

    @Override
    public Injector getInjector() {
        if (null == injector) {
            injector = InjectorBuilder
                    .fromModule(new CheckerModule())
                    .overrideWith(new AnnotareWebAppModule())
                    .overrideWith(new AnnotarePluginModules())
                    .createInjector();
        }
        return injector;
    }

//    private void lookupPropertiesInContext() {
//        try {
//            Context context = new InitialContext();
//            setSystemProperty("annotare.properties", (String) lookup(context, "java:comp/env/annotareProperties"));
//            setSystemProperty("checker.properties", (String) lookup(context, "java:comp/env/mageTabCheckProperties"));
//        } catch (NamingException e) {
//            log.error("Naming context initialization failure", e);
//        }
//    }
//
//    private Object lookup(Context context, String name) throws NamingException {
//        try {
//            return context.lookup(name);
//        } catch (NameNotFoundException e) {
//            log.debug("name '{}' not found in the naming context", name);
//            return null;
//        }
//    }
//
//    private void setSystemProperty(String name, String value) {
//        if (!isNullOrEmpty(value)) {
//            log.info("set: {}='{}'", name, value);
//            System.setProperty(name, value);
//        }
//    }
}
