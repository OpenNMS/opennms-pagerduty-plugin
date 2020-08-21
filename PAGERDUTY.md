# PagerDuty + OpenNMS Integration Benefits

OpenNMS is a scalable and highly configurable network management platform with comprehensive fault, performance, and traffic monitoring. 
Integrate OpenNMS with PagerDuty to

* Notify on-call responders based on alarms triggered in OpenNMS
* Customize which alarms are forwarded to PagerDuty using flexible expressions
* Synchronize incidents and acknowledgemenet across both OpenNMS and PagerDuty as they update

# How it Works

PagerDuty integration for OpenNMS is available as an [OpenNMS Integration API](https://github.com/OpenNMS/opennms-integration-api) plugin.
When loaded, the plugin listens for changes to alarms and forwards these to PagerDuty using the [Events API v2](https://developer.pagerduty.com/docs/events-api-v2/overview/).
The plugin also supports handling webhooks issued by changes to the alerts in PagerDuty, which can be used for bi-direction synchronization.

# Requirements

* OpenNMS Horizon 26.1.3+ or OpenNMS Meridian 2020.1.0+

# Support

If you need help with this integration, please use the [OpenNMS Discourse Group](https://opennms.discourse.group/) or our [OpenNMS Chat](https://chat.opennms.com/).

# Integration Walkthrough
## In PagerDuty

### Integrating With a PagerDuty Service
1. From the **Configuration** menu, select **Services**.
2. There are two ways to add an integration to a service:
   * **If you are adding your integration to an existing service**: Click the **name** of the service you want to add the integration to. Then, select the **Integrations** tab and click the **New Integration** button.
   * **If you are creating a new service for your integration**: Please read our documentation in section [Configuring Services and Integrations](https://support.pagerduty.com/docs/services-and-integrations#section-configuring-services-and-integrations) and follow the steps outlined in the [Create a New Service](https://support.pagerduty.com/docs/services-and-integrations#section-create-a-new-service) section, selecting **OpenNMS*** as the **Integration Type** in step 4. Continue with the **In OpenNMS**  section (below) once you have finished these steps.
3. Enter an **Integration Name** in the format `monitoring-tool-service-name` (e.g.,  OpenNMS-Core-Routers) and select **OpenNMS**  from the Integration Type menu.
4. Click the **Add Integration** button to save your new integration. You will be redirected to the Integrations tab for your service.
5. An **Integration Key** will be generated on this screen. Keep this key saved in a safe place, as it will be used when you configure the integration with **OpenNMS**  in the next section.
![](https://pdpartner.s3.amazonaws.com/ig-template-copy-integration-key.png)

## In OpenNMS

Download the plugin's .kar file into your OpenNMS deploy directory i.e.,:
```
sudo wget https://github.com/OpenNMS/opennms-pagerduty-plugin/releases/download/v0.1.0/opennms-pagerduty-plugin.kar -P /opt/opennms/deploy/
```

Configure the plugin to be installed when OpenNMS starts:
```
echo 'opennms-plugins-pagerduty wait-for-kar=opennms-pagerduty-plugin' | sudo tee /opt/opennms/etc/featuresBoot.d/pagerduty.boot
```

Access the [Karaf shell](https://opennms.discourse.group/t/karaf-cli-cheat-sheet/149) and install the feature manually to avoid having to restart:
```
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
property-set jexlFilter 'alarm.reductionKey =~ ".*trigger.*"'
config:update
```

> Use the value of the "Integration Key" in the service integrations as the `routingKey`

See [OpenNMS PagerDuty Plugin](https://github.com/OpenNMS/opennms-pagerduty-plugin) for more details on customizing the integration.

# How to Uninstall

To uninstall the integration, remove the plugin archive:
```
rm -f /opt/opennms/deploy/opennms-pagerduty-plugin.kar
```

Remove the feature from the system startup:
```
rm -f /opt/opennms/etc/featuresBoot.d/pagerduty.boot
```

Remove the plugin configuration files:
```
rm -f /opt/opennms/etc/org.opennms.plugins.pagerduty*
```

Restart OpenNMS.

