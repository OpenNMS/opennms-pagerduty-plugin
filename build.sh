#!/bin/bash
#
# Build and deploy pagerduty plugin locally
#
# ENV variables can be set here or in .bash_profile or .zshenv
# You need to get you routing key from the pagerduty site
#

#OPENNMS_HOME=$HOME/opennms/target/opennms-31.0.0-SNAPSHOT/
#PAGERDUTY_ROUTING_KEY="12345678901234567890123456789012"
OPENNMS_URL="http://localhost:8980/opennms"

KARFILE="opennms-pagerduty-plugin.kar"

if [ -z ${OPENNMS_HOME} ]; then
	echo "OPENNMS_HOME environment variable not set"
	exit 1
fi

if [ ! -f ${OPENNMS_HOME}/bin/opennms ]; then
	echo "File: ${OPENNMS_HOME}/bin/opennms not found." 
	echo "Make sure ${OPENNMS_HOME} points to your opennms target directory and opennms is built"
	exit 2
fi

ARTIFACT_ID="<artifactId>pagerduty-parent</artifactId>"
grep  "${ARTIFACT_ID}" pom.xml > /dev/null
if [ $? -ne 0 ] ; then
	echo "Can't find pagerduty artifactId in pom.xml, are you in the right directory"
	exit 3
fi

mvn clean install
MVNRET=$?
if [ $MVNRET -eq 127 ]; then
	echo "Check if maven is on your path"
	exit 4
fi

if [ $MVNRET -ne 0 ] ; then
	echo "Build failed"
	exit 5
fi



if [ ! -f ./assembly/kar/target/$KARFILE ] ; then
	echo "New kar file is missing: "
	exit 6
fi

rm -f ${OPENNMS_HOME}/deploy/$KARFILE
if [ -f ${OPENNMS_HOME}/deploy/$KARFILE ] ; then
	echo "Cant remove previous kar file"
	exit 7
fi

#This is the important bit
cp ./assembly/kar/target/$KARFILE ${OPENNMS_HOME}/deploy/
if [ ! -f ${OPENNMS_HOME}/deploy/$KARFILE ] ; then
	echo "Can't copy new file"
	exit 8
fi

echo "------Deploy directory -----"
ls -l ${OPENNMS_HOME}/deploy/
echo "----------------------------"

${OPENNMS_HOME}/bin/opennms status
RUNNING=$?
if [ $RUNNING -ne 0 ] ; then
	echo "Starting OpenNMS"
	${OPENNMS_HOME}/bin/opennms -t start
	echo "Actually we are waiting, push <Ctrl> C to cancel.."
	until $(curl --output /dev/null --silent --head --fail ${OPENNMS_URL}); do printf '.'; sleep 1; done
	echo "Sleeping a little bit more.."
	sleep 5
	${OPENNMS_HOME}/bin/opennms status
	
	if [ $? -ne 0 ] ; then
		echo "OpenNMS not up, cancelled waiting"
		exit 9
	fi
fi

if [ ! -d ${OPENNMS_HOME}/etc/featuresBoot.d ] ; then
	echo "${OPENNMS_HOME}/etc/featuresBoot.d directory not found"
	exit 10
fi
echo 'opennms-plugins-pagerduty wait-for-kar=opennms-pagerduty-plugin' > ${OPENNMS_HOME}/etc/featuresBoot.d/pagerduty.boot
if [ ! -f  ${OPENNMS_HOME}/etc/featuresBoot.d/pagerduty.boot ] ; then
	echo "${OPENNMS_HOME}/etc/featuresBoot.d/pagerduty.boot not created"
	echo "Is directory writable?"
	exit 11
fi

echo "------------------karaf commands--------------------------"
echo "feature:list | grep pager"
echo "feature:uninstall opennms-plugins-pagerduty"
echo "feature:install opennms-plugins-pagerduty"
echo "config:edit org.opennms.plugins.pagerduty"
echo "property-set client OpenNMS"
echo "property-set alarmDetailsUrlPattern '${OPENNMS_URL}/alarm/detail.htm?id=%d'"
echo "config:update"
echo ""
echo "config:edit --alias core --factory org.opennms.plugins.pagerduty.services"
echo "property-set routingKey \"${PAGERDUTY_ROUTING_KEY}\""
echo "config:update"
echo "------------------------------------------------------------"
echo ""
echo ""
echo "Run 'ssh -p 8101 admin@localhost' and config as above"
echo ""
echo ""



