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

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import org.opennms.pagerduty.client.api.PDEvent;

/**
 * Represents a PagerDuty notification to be sent at some future time, if the underlying alarm has not yet been
 * resolved.
 */
public class PagerDutyForwarderTask implements Delayed {
    /**
     * The time after which this task may fire.
     */
    private final Instant fireAfter;

    /**
     * The reductionKey for the underlying alarm.
     */
    private final String reductionKey;

    /**
     * The PagerDuty event to notify about.
     */
    private final PDEvent pdEvent;

    public PagerDutyForwarderTask(Instant fireAfter, String reductionKey, PDEvent pdEvent) {
        this.fireAfter = fireAfter;
        this.reductionKey = reductionKey;
        this.pdEvent = pdEvent;
    }

    public String getReductionKey() {
        return reductionKey;
    }

    public PDEvent getPdEvent() {
        return pdEvent;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        Instant now = Instant.now();
        Duration remaining = Duration.between(now, fireAfter);
        return unit.convert(remaining.toNanos(), TimeUnit.NANOSECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        return Objects.compare(getDelay(TimeUnit.NANOSECONDS),
                o.getDelay(TimeUnit.NANOSECONDS),
                Comparator.naturalOrder());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PagerDutyForwarderTask that = (PagerDutyForwarderTask) o;
        return Objects.equals(fireAfter, that.fireAfter) &&
                Objects.equals(reductionKey, that.reductionKey) &&
                Objects.equals(pdEvent, that.pdEvent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fireAfter, reductionKey, pdEvent);
    }

    @Override
    public String toString() {
        return "PagerDutyForwarderTask{" +
                "fireAfter=" + fireAfter +
                ", reductionKey='" + reductionKey + '\'' +
                ", pdEvent=" + pdEvent +
                '}';
    }
}
