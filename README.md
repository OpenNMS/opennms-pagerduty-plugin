# OpenNMS PagerDuty Plugin [![CircleCI](https://circleci.com/gh/OpenNMS/opennms-pagerduty-plugin.svg?style=svg)](https://circleci.com/gh/OpenNMS/opennms-pagerduty-plugin)

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

Update automatically:
```
bundle:watch *
```
