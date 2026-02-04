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

package uk.ac.ebi.fg.annotare2.web.server.servlets;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fg.annotare2.core.properties.AnnotareProperties;

import javax.servlet.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class BannerFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(BannerFilter.class);

    @Inject
    private AnnotareProperties properties;

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void destroy() {
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        Properties bannerProperties = new Properties();
        String path = properties.getBannerPropertiesPath();
        if (path != null && !path.isEmpty()) {
            try (FileInputStream is = new FileInputStream(path)) {
                bannerProperties.load(is);
                String headline = bannerProperties.getProperty("banner.headline");
                String message = bannerProperties.getProperty("banner.message");
                servletRequest.setAttribute("bannerHeadline", headline == null ? "" : headline);
                servletRequest.setAttribute("bannerMessage", message == null ? "" : message);
            } catch (IOException e) {
                log.error("Unable to load banner properties from {}", path, e);
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
