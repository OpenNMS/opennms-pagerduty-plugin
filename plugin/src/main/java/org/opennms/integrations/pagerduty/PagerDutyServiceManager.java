/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.integrations.pagerduty;

import java.util.Dictionary;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.opennms.integration.api.v1.alarms.AlarmLifecycleListener;
import org.opennms.pagerduty.client.api.PDClientFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;

/**
 * This class is responsible for managing the lifecycle of {@link PagerDutyForwarder}s
 * that correspond to the services configured in ${OPENNMS_HOME}/etc/org.opennms.plugins.pagerduty.services-*.cfg files.
 *
 * Each service has its own routing key, filter and so on...
 *
 */
public class PagerDutyServiceManager implements ManagedServiceFactory {
    private static final Logger LOG = LoggerFactory.getLogger(PagerDutyServiceManager.class);
    
    public static final String ROUTING_KEY_PROP = "routingKey";
    public static final String JEXL_FILTER_PROP = "jexlFilter";

    private final BundleContext bundleContext;
    private final PDClientFactory pdClientFactory;
    private final PagerDutyPluginConfig pluginConfig;

    private static class Entity {
        private PagerDutyForwarder plugin;
        private ServiceRegistration<AlarmLifecycleListener> alarmLifecycleListener;
    }

    private Map<String, Entity> entities = new LinkedHashMap<>();

    public PagerDutyServiceManager(BundleContext bundleContext, PDClientFactory pdClientFactory, PagerDutyPluginConfig pluginConfig) {
        this.bundleContext = Objects.requireNonNull(bundleContext);
        this.pdClientFactory = Objects.requireNonNull(pdClientFactory);
        this.pluginConfig = Objects.requireNonNull(pluginConfig);
    }

    @Override
    public String getName() {
        return "PagerDuty Service Manager";
    }

    @Override
    public void updated(String pid, Dictionary<String, ?> properties) {
        if (this.entities.containsKey(pid)) {
            LOG.info("Updating existing plugin for pid: {}", pid);
            deleted(pid);
        } else {
            LOG.info("Creating new plugin for pid: {}", pid);
        }
        // Convert dictionary to property map
        Map<String,String> props = Maps.toMap(Iterators.forEnumeration(properties.keys()),
                key -> (String) properties.get(key));
        // Build the service config
        final String routingKey = props.get(ROUTING_KEY_PROP);
        final String jexlFilter = props.get(JEXL_FILTER_PROP);
        PagerDutyServiceConfig serviceConfig = new PagerDutyServiceConfig(routingKey, jexlFilter);

        // Now build the entity
        Entity entity = new Entity();
        entity.plugin = new PagerDutyForwarder(pdClientFactory, pluginConfig, serviceConfig);
        // Register the service
        entity.alarmLifecycleListener = bundleContext.registerService(AlarmLifecycleListener.class, entity.plugin, null);
        LOG.info("Successfully started plugin for pid: {}", pid);
    }

    @Override
    public void deleted(String pid) {
        final Entity entity = entities.remove(pid);
        if (entity != null) {
            LOG.info("Stopping plugin for pid: {}", pid);
            if (entity.alarmLifecycleListener != null) {
                entity.alarmLifecycleListener.unregister();
            }
        }
    }
}
