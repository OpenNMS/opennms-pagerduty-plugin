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

package org.opennms.pagerduty.webhook;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility=JsonAutoDetect.Visibility.NONE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Message {

    /**
     * Uniquely identifies this outgoing webhook message; can be used for idempotency when processing the messages.
     */
    @JsonProperty("id")
    private String id;

    /**
     * The webhook event type
     */
    @JsonProperty("event")
    @JsonSerialize(using = EventTypeSerializer.class)
    @JsonDeserialize(using = EventTypeDeserializer.class)
    private EventType event;

    /**
     * The date/time when the incident changed state.
     */
    @JsonProperty("created_on")
    private Date created_on;

    /**
     * The incident details at the time of the state change.
     */
    @JsonProperty("incident")
    private Incident incident;

    /**
     * The webhook configuration which resulted in this message.
     */
    @JsonProperty("webhook")
    private Object webhook;

    /**
     * Log entries that correspond to the action this Webhook is reporting.
     * There will be only one log_entry type object in the array for incident.trigger, incident.acknowledge and
     * incident.resolve type webhooks. For incident.escalate webhooks where there were more than one target,
     * there will be one entry in this array for each escalation target.
     */
    @JsonProperty("log_entries")
    private List<Object> log_entries;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public EventType getEvent() {
        return event;
    }

    public void setEvent(EventType event) {
        this.event = event;
    }

    public Date getCreated_on() {
        return created_on;
    }

    public void setCreated_on(Date created_on) {
        this.created_on = created_on;
    }

    public Incident getIncident() {
        return incident;
    }

    public void setIncident(Incident incident) {
        this.incident = incident;
    }

    public Object getWebhook() {
        return webhook;
    }

    public void setWebhook(Object webhook) {
        this.webhook = webhook;
    }

    public List<Object> getLog_entries() {
        return log_entries;
    }

    public void setLog_entries(List<Object> log_entries) {
        this.log_entries = log_entries;
    }
}
