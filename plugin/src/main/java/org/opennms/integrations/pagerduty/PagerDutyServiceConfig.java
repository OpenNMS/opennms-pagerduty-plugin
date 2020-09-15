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

import java.util.Objects;

public class PagerDutyServiceConfig {
    private final String pid;
    private final String routingKey;
    private final String jexlFilter;

    public PagerDutyServiceConfig(String pid, String routingKey, String jexlFilter) {
        this.pid = Objects.requireNonNull(pid, "pid is required");
        this.routingKey = Objects.requireNonNull(routingKey, "routingKey is required");
        this.jexlFilter = jexlFilter;
    }

    public String getPid() {
        return pid;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public String getJexlFilter() {
        return jexlFilter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PagerDutyServiceConfig that = (PagerDutyServiceConfig) o;
        return Objects.equals(pid, that.pid) &&
                Objects.equals(routingKey, that.routingKey) &&
                Objects.equals(jexlFilter, that.jexlFilter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pid, routingKey, jexlFilter);
    }

    @Override
    public String toString() {
        return "PagerDutyServiceConfig{" +
                "pid='" + pid + '\'' +
                ", routingKey='" + routingKey + '\'' +
                ", jexlFilter='" + jexlFilter + '\'' +
                '}';
    }
}
