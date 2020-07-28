# OpenNMS PagerDuty Plugin [![CircleCI](https://circleci.com/gh/OpenNMS/opennms-pagerduty-plugin.svg?style=svg)](https://circleci.com/gh/OpenNMS/opennms-pagerduty-plugin)

## Overview

This plugin allows you to create PagerDuty incidents that correspond to alarms in OpenNMS by integrating with the [Events API v2](https://developer.pagerduty.com/docs/events-api-v2/overview/).

OpenNMS alarms map naturally to incidents in PagerDuty:
* Events create incidents
* Events are deduplicated by the `dedup_key` (or `reduction key` in OpenNMS)
* Incidents can be acknowledged while someone is working on the problem
* Incidents are cleared when there is no longer a problem

The integration operates only in one direction currently (changes to alarms are pushed to PagerDuty), however future work could be done to achieve bi-directional communication by leveraing web hooks.

This plugin is compatible with OpenNMS Horizon 26.1.3 or higher.

## Usage

Build and install the plugin into your local Maven repository using:
```
mvn clean install
```

From the OpenNMS Karaf shell:
```
feature:repo-add mvn:org.opennms.plugins/pagerduty-karaf-features/1.0.0-SNAPSHOT/xml
feature:install opennms-plugins-pagerduty
```

Configure global options (affects all services for this instance):
```
config:edit org.opennms.plugins.pagerduty
property-set client OpenNMS
property-set alarmDetailsUrlPattern 'http://127.0.0.1:8980/opennms/alarm/detail.htm?id=%d'
config:update
```

Configure services:
```
config:edit --alias core --factory org.opennms.plugins.pagerduty.services
property-set routingKey "YOUR-ROUTING-KEY-HERE"
property-set jexlFilter 'alarm.reductionKey =~ ".*triggera.*"'
config:update
```

> Use the value of the "Integration Key" in the service integrations as the `routingKey`

Update automatically:
```
bundle:watch *
```
