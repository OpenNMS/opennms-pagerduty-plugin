/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.pagerduty.client.api;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.io.Resources;

public class PDLongEventTest {

    @Test
    public void canMarshalLongEventToJson() throws IOException, JSONException, ParseException {
        PDEvent event = new PDEvent();
        PDEventPayload payload = new PDEventPayload();
        payload.setSummary("One morning, when Gregor Samsa woke from troubled dreams, he found himself transformed in his bed into a horrible vermin. He lay on his armour-like back, and if he lifted his head a little he could see his brown belly, slightly domed and divided by arches into stiff sections. The bedding was hardly able to cover it and seemed ready to slide off any moment. His many legs, pitifully thin compared with the size of the rest of him, waved about helplessly as he looked. 'What's happened to me?' he thought. It wasn't a dream. His room, a proper human room although a little too small, lay peacefully between its four familiar walls. A collection of textile samples lay spread out on the table - Samsa was a travelling salesman - and above it there hung a picture that he had recently cut out of an illustrated magazine and housed in a nice, gilded frame. It showed a lady fitted out with a fur hat and fur boa who sat upright, raising a heavy fur muff that covered the whole of her lower arm towards the viewer. Gregor then turned to look out the window");
        SimpleDateFormat formatter = new SimpleDateFormat(PDEventPayload.DATE_FORMAT, Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = formatter.parse("2015-07-17T08:42:58.315+0000");
        payload.setTimestamp(date);
        payload.setSource("minotoriringtool:cloudvendor:central-region-dc-01:852559987:cluster/api-stats-prod-003");
        payload.setSeverity(PDEventSeverity.INFO);
        payload.setComponent("postgres");
        payload.setGroup("prod-datapipe");
        payload.setEventClass("deploy");
        payload.getCustomDetails().put("ping time", "1500ms");
        payload.getCustomDetails().put("load avg", 0.75f);
        event.setPayload(payload);

        event.setRoutingKey("samplekeyhere");
        event.setDedupKey("samplekeyhere");

        PDEventImage image = new PDEventImage();
        image.setSource("https://www.pagerduty.com/wp-content/uploads/2016/05/pagerduty-logo-green.png");
        image.setHref("https://example.com/");
        image.setAlt("Example text");
        event.getImages().add(image);

        event.setEventAction(PDEventAction.TRIGGER);
        event.setClient("Sample Monitoring Service");
        event.setClientUrl("https://monitoring.example.com");

        marshalAndCompare(event, "example-event-long.json");
    }

    private static void marshalAndCompare(Object o, String resourcePath) throws IOException, JSONException {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        final String actualJson = mapper.writer().writeValueAsString(o);
        final URL resourceUrl = Resources.getResource(resourcePath);
        final String expectedJson = Resources.toString(resourceUrl, StandardCharsets.UTF_8);
        System.err.println("Actual JSON: " + actualJson);
        System.err.println("Expected JSON: " + expectedJson);
        JSONAssert.assertEquals(expectedJson, actualJson, false);
    }
}
