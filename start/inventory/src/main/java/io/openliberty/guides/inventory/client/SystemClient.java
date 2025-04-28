// tag::copyright[]
/*******************************************************************************
 * Copyright (c) 2025 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
// end::copyright[]
package io.openliberty.guides.inventory.client;

import java.net.URI;
import java.util.Properties;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

public class SystemClient implements AutoCloseable {

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
            // tag::out1[]
            System.out.println("URISyntaxException while building system service URL: "
                    + e.getMessage());
            // end::out1[]
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
            // tag::out2[]
            System.out.println("Exception while creating REST client builder: "
                    + e.getMessage());
            // end::out2[]
            return null;
        }
    }

    // Helper method that processes the request
    protected Properties getPropertiesHelper(Builder builder) {
        try {
            Response response = builder.get();
            // tag::out3[]
            System.out.println("Received response with status: " + response.getStatus());
            // end::out3[]
            if (response.getStatus() == Status.OK.getStatusCode()) {
                return response.readEntity(Properties.class);
            } else {
                // tag::out4[]
                System.out.println("Response Status is not OK.");
                // end::out4[]
            }
        } catch (RuntimeException e) {
            // tag::out5[]
            System.out.println("Runtime exception while invoking system service: "
                    + e.getMessage());
            // end::out5[]
        } catch (Exception e) {
            // tag::out6[]
            System.out.println("Unexpected exception while processing system service request: "
                    + e.getMessage());
            // end::out6[]
        }
        return null;
    }

    @Override
    public void close() {
        if (client != null) {
            client.close();
        }
    }
}
