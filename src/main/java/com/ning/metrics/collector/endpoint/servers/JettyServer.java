/*
 * Copyright 2010-2011 Ning, Inc.
 *
 * Ning licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.ning.metrics.collector.endpoint.servers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.servlet.GuiceFilter;
import com.ning.metrics.collector.binder.config.CollectorConfig;
import com.ning.metrics.collector.binder.modules.JettyListener;
import com.ning.metrics.collector.endpoint.setup.SetupJULBridge;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslConnector;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.EventListener;

@Singleton
public class JettyServer
{
    private static final Logger log = Logger.getLogger(JettyServer.class);

    private final CollectorConfig config;
    private boolean initialized = false;
    private Server server;

    @Inject
    public JettyServer(final CollectorConfig config)
    {
        this.config = config;
    }

    public void start() throws Exception
    {
        final long startTime = System.currentTimeMillis();

        server = new Server();

        final Connector connector = new SelectChannelConnector();
        connector.setHost(config.getLocalIp());
        connector.setPort(config.getLocalPort());
        server.addConnector(connector);

        if (config.isSSLEnabled()) {
            final SslConnector sslConnector = new SslSelectChannelConnector();
            sslConnector.setPort(config.getLocalSSLPort());
            sslConnector.setKeystore(config.getSSLkeystoreLocation());
            sslConnector.setKeyPassword(config.getSSLkeystorePassword());
            sslConnector.setPassword(config.getSSLkeystorePassword());
            server.addConnector(sslConnector);
        }

        server.setStopAtShutdown(true);

        final ServletContextHandler context = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
        context.addEventListener(new JettyListener());

        // Jersey insists on using java.util.logging (JUL)
        final EventListener listener = new SetupJULBridge();
        context.addEventListener(listener);

        // Make sure Guice filter all requests
        final FilterHolder filterHolder = new FilterHolder(GuiceFilter.class);
        context.addFilter(filterHolder, "/*", ServletContextHandler.NO_SESSIONS);

        final ServletHolder sh = new ServletHolder(DefaultServlet.class);
        sh.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
        sh.setInitParameter("com.sun.jersey.config.property.packages", "com.ning.metrics.collector.endpoint");
        context.addServlet(sh, "/*");

        server.start();

        final long secondsToStart = (System.currentTimeMillis() - startTime) / 1000;
        log.info(String.format("Jetty server started in %d:%02d", secondsToStart / 60, secondsToStart % 60));

        initialized = true;
    }

    public void stop()
    {
        if (!initialized) {
            return;
        }

        try {
            server.stop();
        }
        catch (Exception e) {
            log.warn("Got exception trying to stop Jetty", e);
        }
    }

    /**
     * Has Jetty finished its startup sequence?
     *
     * @return true iff Jetty has been setup
     */
    public boolean isInitialized()
    {
        return initialized;
    }
}
