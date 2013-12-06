
package com.xoom.research;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.xoom.oss.feathercon.FeatherCon;
import com.xoom.oss.feathercon.ServletConfiguration;
import com.xoom.oss.feathercon.WebSocketEndpointConfiguration;
import org.eclipse.jetty.servlet.DefaultServlet;

import javax.websocket.server.ServerEndpointConfig;
import java.util.HashSet;
import java.util.Set;

public class App implements Consumer {

    private Set<ServerSocket> sockets = new HashSet<ServerSocket>();

    public static void main(String[] args) throws Exception {
        new App().run();
    }

    private void run() throws Exception {
        Configurator serverEndpointConfigurator = new Configurator();
        ServerEndpointConfig config = ServerEndpointConfig.Builder
                .create(ServerSocket.class, "/events")
                .configurator(serverEndpointConfigurator)
                .build();

        WebSocketEndpointConfiguration wsconfig = new WebSocketEndpointConfiguration.Builder()
                .withServerEndpointConfig(config)
                .build();

        // Need this to deliver the initial html/javascript to the browser:  http://localhost:8080/index.html
        ServletConfiguration staticContentConfig = new ServletConfiguration.Builder()
                .withServletClass(DefaultServlet.class)
                .withPathSpec("/*")
                .withInitParameter("resourceBase", "html")
                .build();

        FeatherCon server = new FeatherCon.Builder()
                .withPort(8080)
                .withWebSocketConfiguration(wsconfig)
                .withServletConfiguration(staticContentConfig)
                .build();

        ProducerImpl producer = new ProducerImpl();
        producer.add(this);
        producer.start();

        server.start();
        server.join();
    }

    @Override
    public void consume(Object o) {
        System.out.printf("Sending message to websocket peers: %s\n", o.toString());
        for (ServerSocket socket : sockets) {
            socket.consume(o);
        }
    }

    public class Configurator extends ServerEndpointConfig.Configurator {

        private final Injector injector;

        public Configurator() {
            injector = Guice.createInjector(new WebSocketModule());
        }

        // The locus of Endpoint dependency injection with wired in collaborators.
        @Override
        public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
            T instance = injector.getInstance(endpointClass);
            sockets.add((ServerSocket) instance);
            return instance;
        }

        private class WebSocketModule extends AbstractModule {
            @Override
            protected void configure() {
            }
        }
    }
}
