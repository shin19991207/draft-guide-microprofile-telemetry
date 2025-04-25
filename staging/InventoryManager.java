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
package io.openliberty.guides.inventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import jakarta.annotation.PreDestroy;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.DoubleHistogram;

import io.openliberty.guides.inventory.client.SystemClient;
import io.openliberty.guides.inventory.model.InventoryList;
import io.openliberty.guides.inventory.model.SystemData;

@ApplicationScoped
public class InventoryManager {

    @Inject
    @ConfigProperty(name = "system.http.port")
    private int SYSTEM_PORT;

    private List<SystemData> systems = Collections.synchronizedList(new ArrayList<>());
    private SystemClient systemClient = new SystemClient();

    // tag::meter[]
    @Inject
    Meter meter;
    // end::meter[]

    private LongCounter listCounter;
    private DoubleHistogram addHistogram;

    @PostConstruct
    public void init() {
        // tag::counterBuilder[]
        listCounter = meter.counterBuilder("inventory.list.count")
            .setDescription("Number of times the inventory list is requested")
            .setUnit("1")
            .build();
        // end::counterBuilder[]

        // tag::histogramBuilder[]
        addHistogram = meter.histogramBuilder("inventory.add.duration")
            .setDescription("Time taken to add a system to the inventory")
            .setUnit("ms")
            .build();
        // end::histogramBuilder[]

        // tag::gaugeBuilder[]
        meter.gaugeBuilder("inventory.size")
            .setDescription("Number of systems in the inventory")
            .setUnit("1")
            .buildWithCallback(g -> g.record((double) systems.size()));
        // end::gaugeBuilder[]
    }

    public Properties get(String hostname) {
        systemClient.init(hostname, SYSTEM_PORT);
        Properties properties = systemClient.getProperties();
        return properties;
    }

    // tag::list[]
    public InventoryList list() {
        listCounter.add(1);
        return new InventoryList(systems);
    }
    // end::list[]

    // tag::add[]
    public void add(String host, Properties systemProps) {
        long start = System.currentTimeMillis();
        try {
            Properties props = new Properties();
            props.setProperty("os.name", systemProps.getProperty("os.name"));
            props.setProperty("user.name", systemProps.getProperty("user.name"));
            SystemData system = new SystemData(host, props);
            if (!systems.contains(system)) {
                systems.add(system);
            }
        } finally {
            long duration = System.currentTimeMillis() - start;
            addHistogram.record((double) duration);
        }
    }
    // end::add[]

    int clear() {
        int propertiesClearedCount = systems.size();
        systems.clear();
        return propertiesClearedCount;
    }

    @PreDestroy
    public void shutdown() {
        systemClient.close();
    }
}
