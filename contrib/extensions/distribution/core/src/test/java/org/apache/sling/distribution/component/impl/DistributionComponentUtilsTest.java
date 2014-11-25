package org.apache.sling.distribution.component.impl;

import org.apache.sling.distribution.agent.DistributionAgent;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class DistributionComponentUtilsTest {

    @Test
    public void testEmptySettingsTransform() {
        Map<String, Object> settings = new HashMap<String, Object>();

        DistributionComponentUtils utils = new DistributionComponentUtils();
        Map<String, Map<String, Object>> result = utils.transformToOsgi(settings);

        assertEquals(0, result.size());
    }

    @Test
    public void testFirstLevelTransform() {
        Map<String, Object> settings = new HashMap<String, Object>();
        settings.put("kind", "agent");
        settings.put("name", "publish");
        settings.put("type", "simple");

        settings.put("packageExporter", new HashMap<String, Object>());

        DistributionComponentUtils utils = new DistributionComponentUtils();
        Map<String, Map<String, Object>> result = utils.transformToOsgi(settings);

        assertEquals(1, result.size());
        String resultKey = "agent|simple|publish";
        assertTrue(result.containsKey(resultKey));
        assertEquals(4, result.get(resultKey).size());
        assertTrue(result.get(resultKey).containsKey("name"));
        assertTrue(result.get(resultKey).containsKey("type"));
        assertTrue(result.get(resultKey).containsKey("packageExporter.target"));
        assertEquals("(parent.name=publish)", result.get(resultKey).get("packageExporter.target"));
    }

    @Test
    public void testTwoLevelTransform() {
        Map<String, Object> settings = new HashMap<String, Object>();
        settings.put("kind", "agent");
        settings.put("name", "publish");
        settings.put("type", "simple");
        settings.put("packageExporter", new HashMap<String, Object>());
        ((Map) settings.get("packageExporter")).put("kind", "exporter");
        ((Map) settings.get("packageExporter")).put("type", "remote");

        DistributionComponentUtils utils = new DistributionComponentUtils();
        Map<String, Map<String, Object>> result = utils.transformToOsgi(settings);

        assertEquals(2, result.size());

        {
            String resultKey = "agent|simple|publish";
            assertTrue(result.containsKey(resultKey));
            assertEquals(4, result.get(resultKey).size());
            assertTrue(result.get(resultKey).containsKey("kind"));
            assertTrue(result.get(resultKey).containsKey("name"));
            assertTrue(result.get(resultKey).containsKey("type"));
            assertTrue(result.get(resultKey).containsKey("packageExporter.target"));
            assertEquals("(parent.name=publish)", result.get(resultKey).get("packageExporter.target"));
        }

        {
            String resultKey = "exporter|remote|publish/packageExporter";
            assertTrue(result.containsKey(resultKey));
            assertEquals(4, result.get(resultKey).size());
        }
    }

    @Test
    public void testMultipleTargetTransform() {
        Map<String, Object> settings = new HashMap<String, Object>();
        settings.put("kind", "agent");
        settings.put("name", "publish");
        settings.put("type", "simple");
        settings.put("triggers", new HashMap<String, Object>());
        ((Map) settings.get("triggers")).put("type", "list");
        ((Map) settings.get("triggers")).put("kind", "trigger");


        Map trigger1Properties = new HashMap<String, Object>();
        trigger1Properties.put("type", "event");
        ((Map) settings.get("triggers")).put("trigger1", trigger1Properties);


        Map trigger2Properties = new HashMap<String, Object>();
        trigger2Properties.put("type", "event");
        ((Map) settings.get("triggers")).put("trigger2", trigger2Properties);



        DistributionComponentUtils utils = new DistributionComponentUtils();
        Map<String, Map<String, Object>> result = utils.transformToOsgi(settings);

        assertEquals(3, result.size());
        {
            String resultKey = "agent|simple|publish";
            assertTrue(result.containsKey(resultKey));
            assertEquals(4, result.get(resultKey).size());
            assertTrue(result.get(resultKey).containsKey("name"));
            assertTrue(result.get(resultKey).containsKey("type"));
            assertTrue(result.get(resultKey).containsKey("triggers.target"));
            assertEquals("(parent.name=publish)", result.get(resultKey).get("triggers.target"));
        }
    }
}
