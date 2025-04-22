package io.openliberty.guides.inventory.client;

import java.net.URI;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

public class SystemClient implements AutoCloseable {

    private static final Logger logger = Logger.getLogger(SystemClient.class.getName());

    // Constants for building URI to the system service.
    private final String SYSTEM_PROPERTIES = "/system/properties";
    private final String PROTOCOL = "http";

    private String url;
    private Client client;
    private Builder clientBuilder;

    public void init(String hostname, int port) {
        this.initHelper(hostname, port);
    }

    // Helper method to set the attributes.
    private void initHelper(String hostname, int port) {
        this.url = buildUrl(PROTOCOL, hostname, port, SYSTEM_PROPERTIES);
        this.clientBuilder = buildClientBuilder(this.url);
        logger.log(Level.INFO, "SystemClient initialized with URL: {0}", this.url);
    }

    // Wrapper function that gets properties
    public Properties getProperties() {
        return getPropertiesHelper(this.clientBuilder);
    }

    /**
     * Builds the URI string to the system service for a particular host.
     * @param protocol
     *          - http or https.
     * @param host
     *          - name of host.
     * @param port
     *          - port number.
     * @param path
     *          - Note that the path needs to start with a slash!!!
     * @return String representation of the URI to the system properties service.
     */
    protected String buildUrl(String protocol, String host, int port, String path) {
        try {
            URI uri = new URI(protocol, null, host, port, path, null, null);
            return uri.toString();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "URISyntaxException while building system service URL", e);
            return null;
        }
    }

    // Method that creates the client builder
    protected Builder buildClientBuilder(String urlString) {
        try {
            this.client = ClientBuilder.newClient();
            Builder builder = client.target(urlString).request();
            return builder.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception while creating REST client builder", e);
            return null;
        }
    }

    // Helper method that processes the request
    protected Properties getPropertiesHelper(Builder builder) {
        try {
            Response response = builder.get();
            logger.log(Level.INFO, "Received response with status: {0}", response.getStatus());
            if (response.getStatus() == Status.OK.getStatusCode()) {
                return response.readEntity(Properties.class);
            } else {
                logger.log(Level.WARNING, "Response Status is not OK: {0}", response.getStatus());
            }
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Runtime exception while invoking system service", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected exception while processing system service request", e);
        }
        return null;
    }

    @Override
    public void close() {
        if (client != null) {
            client.close();
            logger.info("SystemClient HTTP client closed.");
        }
    }
}
