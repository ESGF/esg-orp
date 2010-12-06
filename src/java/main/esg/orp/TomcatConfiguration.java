package esg.orp;

import java.io.File;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TomcatConfiguration {
    private static final Log LOG = LogFactory.getLog(TomcatConfiguration.class);
    private static final String DEF_LOCATION = "/usr/local/tomcat";
    private static final Properties properties = new Properties();
    private static boolean init = false;

    /**
     * @return loaded properties or null if failed.
     */
    private static void load() {
        String tomcat = System.getenv("CATALINA_HOME");
        if (tomcat == null) tomcat = System.getProperty("CATALINA_HOME");

        if (tomcat == null) {
            LOG.warn("CATALINA_HOME wasn't set attemping default location: "
                    + DEF_LOCATION);
            // use datanode default installation location
            tomcat = DEF_LOCATION;
        }
        File serverXML = new File(tomcat, "conf/server.xml");
        if (!serverXML.canRead() || !serverXML.isFile()) {
            LOG.error(serverXML.getAbsolutePath() + " not found");
            return;
        }

        // we got the file now parse what we need
        try {
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().parse(serverXML);
            NodeList connectors = doc.getElementsByTagName("Connector");

            int connCount = connectors.getLength();
            for (int i = 0; i < connCount; i++) {
                Properties connProps = parseProperties(connectors.item(i));
                // we might save everything, but for the moment only the secure
                // connectors are interesting
                if ("true".equals(connProps.getProperty("secure", ""))) {
                    if (properties.isEmpty()) {
                        properties.putAll(connProps);
                    } else {
                        LOG.fatal("More than one secured connector. Property loading failed");
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(
                    "Could not parse xml file: " + serverXML.getAbsolutePath(),
                    e);
        }

        return;
    }

    public static Properties getProperties() {
        if (!init) {
            load();
            init = true;
        }
        if (properties.isEmpty()) return null;
        else return properties;
    }

    private static Properties parseProperties(Node item) {
        Properties props = new Properties();
        for (int i = 0; i < item.getAttributes().getLength(); i++) {
            props.put(item.getAttributes().item(i).getNodeName(), item
                    .getAttributes().item(i).getNodeValue());
        }
        return props;
    }

}
