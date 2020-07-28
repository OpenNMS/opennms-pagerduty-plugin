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

import java.io.Closeable;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.opennms.integration.api.v1.alarms.AlarmLifecycleListener;
import org.opennms.integration.api.v1.model.Alarm;
import org.opennms.integration.api.v1.model.DatabaseEvent;
import org.opennms.integration.api.v1.model.EventParameter;
import org.opennms.integration.api.v1.model.Severity;
import org.opennms.pagerduty.client.api.PDClient;
import org.opennms.pagerduty.client.api.PDClientFactory;
import org.opennms.pagerduty.client.api.PDEvent;
import org.opennms.pagerduty.client.api.PDEventAction;
import org.opennms.pagerduty.client.api.PDEventPayload;
import org.opennms.pagerduty.client.api.PDEventSeverity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class PagerDutyForwarder implements AlarmLifecycleListener, Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(PagerDutyForwarder.class);

    private final PDClient pdClient;
    private final PagerDutyPluginConfig pluginConfig;
    private final PagerDutyServiceConfig serviceConfig;

    private final JexlEngine jexl = new JexlBuilder().create();
    private final JexlExpression jexlFilterExpression;

    public PagerDutyForwarder(PDClientFactory pdClientFactory, PagerDutyPluginConfig pluginConfig, PagerDutyServiceConfig serviceConfig) {
        this.pluginConfig = Objects.requireNonNull(pluginConfig);
        this.serviceConfig = Objects.requireNonNull(serviceConfig);
        pdClient = pdClientFactory.getClient();

        if (serviceConfig.getJexlFilter() != null) {
            jexlFilterExpression = jexl.createExpression(serviceConfig.getJexlFilter());
        } else {
            jexlFilterExpression = null;
        }
    }

    private boolean shouldProcess(Alarm alarm) {
        if (jexlFilterExpression == null) {
            return true;
        }
        return testAlarmAgainstExpression(jexlFilterExpression, alarm);
    }

    @Override
    public void handleNewOrUpdatedAlarm(Alarm alarm) {
        if (!shouldProcess(alarm)) {
            return;
        }

        PDEvent pdEvent = toEvent(alarm);
        LOG.info("Sending event for alarm with reduction-key: {}", alarm.getReductionKey());
        pdClient.sendEvent(pdEvent).whenComplete((v,ex) -> {
           if (ex != null) {
               LOG.warn("Sending event for alarm with reduction-key: {} failed.", alarm.getReductionKey(), ex);
           } else {
               LOG.info("Event sent successfully for alarm with reduction-key: {}", alarm.getReductionKey());
           }
        });
    }

    @Override
    public void handleDeletedAlarm(int alarmId, String reductionKey) {
        // We don't have enough information here to apply the filter expression, so we just forward all deletes
        final PDEvent e = new PDEvent();
        e.setEventAction(PDEventAction.RESOLVE);
        e.setDedupKey(reductionKey);
        e.setRoutingKey(serviceConfig.getRoutingKey());
        LOG.info("Sending clear for deleted alarm with reduction-key: {}", reductionKey);
        pdClient.sendEvent(e).whenComplete((v,ex) -> {
            if (ex != null) {
                LOG.warn("Sending event for alarm with reduction-key: {} failed.", reductionKey, ex);
            } else {
                LOG.info("Event sent successfully for alarm with reduction-key: {}", reductionKey);
            }
        });
    }

    @Override
    public void handleAlarmSnapshot(List<Alarm> alarms) {
        // TODO: Add snapshot handling support
        // We should add some persistent state handling before we do this though
    }

    public PDEvent toEvent(Alarm alarm) {
        final PDEvent e = new PDEvent();
        e.setClient(pluginConfig.getClient());
        e.setClientUrl(String.format(pluginConfig.getAlarmDetailsUrlPattern(), alarm.getId()));
        e.setRoutingKey(serviceConfig.getRoutingKey());
        e.setDedupKey(alarm.getReductionKey());

        if (Severity.CLEARED.equals(alarm.getSeverity())) {
            e.setEventAction(PDEventAction.RESOLVE);
        } else if (alarm.isAcknowledged()) {
            e.setEventAction(PDEventAction.ACKNOWLEDGE);
        } else {
            e.setEventAction(PDEventAction.TRIGGER);
        }

        final PDEventPayload payload = new PDEventPayload();
        e.setPayload(payload);
        // Log message -> Summary
        payload.setSummary(alarm.getLogMessage());
        // Severity -> Severity
        payload.setSeverity(PDEventSeverity.fromOnmsSeverity(alarm.getSeverity()));
        // Use the node label as the source if available
        if (alarm.getNode() != null) {
            payload.setSource(alarm.getNode().getLabel());
        } else {
            payload.setSource("unknown");
        }
        if (!Strings.isNullOrEmpty(alarm.getManagedObjectType()) && !Strings.isNullOrEmpty(alarm.getManagedObjectInstance())) {
            // Use the MO type/instance if set
            payload.setComponent(String.format("%s - %s", alarm.getManagedObjectType(), alarm.getManagedObjectInstance()));
        }
        // Add all of the event parameters as custom details
        final DatabaseEvent dbEvent = alarm.getLastEvent();
        if (dbEvent != null) {
            payload.getCustomDetails().putAll(eparmsToMap(dbEvent.getParameters()));
        }

        return e;
    }

    protected static Map<String, Object> eparmsToMap(List<EventParameter> eparms) {
        final Map<String, Object> map = new LinkedHashMap<>();
        if (eparms == null) {
            return map;
        }
        eparms.forEach(p -> map.put(p.getName(), p.getValue()));
        return map;
    }

    @Override
    public void close() {
        try {
            pdClient.close();
        } catch (IOException e) {
            LOG.warn("Error while closing PagerDuty client. Resources may not be cleaned up properly.", e);
        }
    }

    public static boolean testAlarmAgainstExpression(JexlExpression expression, Alarm alarm) {
        JexlContext jc = new MapContext();
        jc.set("alarm", alarm);
        return (boolean)expression.evaluate(jc);
    }
}
