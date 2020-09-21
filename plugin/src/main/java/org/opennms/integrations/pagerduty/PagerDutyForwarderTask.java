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

    private final Duration delay;

    /**
     * The reductionKey for the underlying alarm.
     */
    private final String reductionKey;

    /**
     * The PagerDuty event to notify about.
     */
    private final PDEvent pdEvent;

    public PagerDutyForwarderTask(Instant fireAfter, String reductionKey, PDEvent pdEvent) {
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
        return delay.
    }

    @Override
    public int compareTo(Delayed o) {
        return Objects.compare(getDelay(TimeUnit.SECONDS),
                o.getDelay(TimeUnit.SECONDS),
                Comparator.naturalOrder());
    }
}
