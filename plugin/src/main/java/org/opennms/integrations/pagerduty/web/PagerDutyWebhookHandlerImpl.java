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

package org.opennms.integrations.pagerduty.web;

import java.util.Objects;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.opennms.integration.api.v1.dao.AlarmDao;
import org.opennms.integration.api.v1.events.EventForwarder;
import org.opennms.integration.api.v1.model.Alarm;
import org.opennms.integration.api.v1.model.InMemoryEvent;
import org.opennms.integration.api.v1.model.immutables.ImmutableEventParameter;
import org.opennms.integration.api.v1.model.immutables.ImmutableInMemoryEvent;
import org.opennms.pagerduty.webhook.Alert;
import org.opennms.pagerduty.webhook.EventType;
import org.opennms.pagerduty.webhook.Message;
import org.opennms.pagerduty.webhook.Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PagerDutyWebhookHandlerImpl implements PagerDutyWebhookHandler {
    private static final Logger LOG = LoggerFactory.getLogger(PagerDutyWebhookHandlerImpl.class);

    private static final String ACKNOWLEDGE_EVENT_UEI = "uei.opennms.org/ackd/acknowledge";

    final ObjectMapper mapper = new ObjectMapper();

    private EventForwarder eventForwarder;
    private AlarmDao alarmDao;

    public PagerDutyWebhookHandlerImpl(AlarmDao alarmDao, EventForwarder eventForwarder) {
        this.alarmDao = Objects.requireNonNull(alarmDao);
        this.eventForwarder = Objects.requireNonNull(eventForwarder);
    }

    @Override
    public Response ping() {
        return Response.ok("pong").build();
    }

    @Override
    public Response handleWebhook(String body) {
        LOG.debug("Got payload: {}", body);

        final Payload payload;
        try {
            payload = mapper.readValue(body, Payload.class);
        } catch (JsonProcessingException e) {
            LOG.error("Error parsing webhook payload: {}", body, e);
            return Response.serverError().entity("Failed to parse payload.").build();
        }

        for (Message message : payload.getMessages()) {
            if (EventType.ACKNOWLEDGE.equals(message.getEvent())) {
                for (Alert alert : message.getIncident().getAlerts()) {
                    String reductionKey = alert.getAlertKey();
                    // FIXME: Not efficient - should add reduction key lookup to DAO
                    Optional<Alarm> matchingAlarm = alarmDao.getAlarms().stream()
                            .filter(a -> a.getReductionKey().equals(reductionKey))
                            .findFirst();
                    if (!matchingAlarm.isPresent()) {
                        LOG.info("No matching alarm found for reduction key: {}", reductionKey);
                        continue;
                    }

                    InMemoryEvent ackEvent = ImmutableInMemoryEvent.newBuilder()
                            .setUei(ACKNOWLEDGE_EVENT_UEI)
                            .setSource(PagerDutyWebhookHandlerImpl.class.getCanonicalName())
                            .addParameter(ImmutableEventParameter.newInstance("ackType", "alarm"))
                            // FIXME: Derive user from payload
                            .addParameter(ImmutableEventParameter.newInstance("ackUser", "PagerDutyPlugin"))
                            .addParameter(ImmutableEventParameter.newInstance("refId", Integer.toString(matchingAlarm.get().getId())))
                            .build();
                    LOG.info("Sending ack event: {}", ackEvent);
                    eventForwarder.sendAsync(ackEvent);
                }
            }
        }

        return Response.ok().build();
    }
}
