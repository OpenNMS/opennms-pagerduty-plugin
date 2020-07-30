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
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * TODO: The object properties are incomplete - see https://developer.pagerduty.com/docs/webhooks/v2-overview/
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility=JsonAutoDetect.Visibility.NONE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Incident {

    /**
     * The incident's id, which can be used to find it in the REST API.
     */
    @JsonProperty("id")
    private String id;

    /**
     * The incident's id, which can be used to find it in the REST API.
     */
    @JsonProperty("alerts")
    private List<Alert> alerts = new LinkedList<>();

    /**
     * The number of the incident. This is unique across the account.
     */
    @JsonProperty("incident_number")
    private Integer incident_number;

    /**
     * A succinct description of the nature, symptoms, cause, or effect of the incident.
     */
    @JsonProperty("title")
    private String title;

    /**
     * The date/time the incident was first triggered.
     */
    @JsonProperty("created_at")
    private Date created_at;

    /**
     * The current status of the incident. One of triggered, acknowledged, or resolved
     */
    @JsonProperty("status")
    private String status;

    /**
     * The incident's de-duplication key.
     */
    @JsonProperty("incident_key")
    private String incident_key;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Alert> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<Alert> alerts) {
        this.alerts = alerts;
    }

    public Integer getIncident_number() {
        return incident_number;
    }

    public void setIncident_number(Integer incident_number) {
        this.incident_number = incident_number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIncident_key() {
        return incident_key;
    }

    public void setIncident_key(String incident_key) {
        this.incident_key = incident_key;
    }
}
