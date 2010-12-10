package esg.orp;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Encapsulates Tomcat properties. For the time being we extract only the
 * properties we require.
 * The current implementation parses the server.xml file to avoid library
 * dependencies. This might change in the future.
 *
 */
public class TomcatConfiguration {
    private static final Log LOG = LogFactory.getLog(TomcatConfiguration.class);
    private static final String DEF_LOCATION = "/usr/local/tomcat";
    private static final Properties properties = new Properties();
    private static boolean init = false;

    /**
     * Tries to load tomcat properties from $CATALINA_HOME/conf/server.xml.
     * @return loaded properties or null if failed.
     */
    private static void load() {
        //look first for java property as this is intentionally set
        String tomcat = System.getProperty("CATALINA_HOME");
        //if not found check for the environment property
        if (tomcat == null) tomcat = System.getenv("CATALINA_HOME");

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
                        if (LOG.isDebugEnabled()) {
                            StringWriter sw = new StringWriter();
                            properties.list(new PrintWriter(sw));
                            LOG.debug("Proties loaded:");
                            LOG.debug(sw.toString());
                        }
                    } else {
                        LOG.fatal("More than one secured connector. Property loading failed");
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Could not parse xml file: " 
                    + serverXML.getAbsolutePath(),e);
        }

        if (properties.isEmpty()) LOG.error("No tomcat properties were loaded.");
    }

    /**
     * @return the properties found or null if no properties could be found.
     */
    public static Properties getProperties() {
        if (!init) {
            load();
            init = true;
        }
        if (properties.isEmpty()) return null;
        else return properties;
    }

    /**
     * @param item node in server.xml
     * @return properties extracted from current node (attribute=value pairs)
     */
    private static Properties parseProperties(Node item) {
        Properties props = new Properties();
        for (int i = 0; i < item.getAttributes().getLength(); i++) {
            props.put(item.getAttributes().item(i).getNodeName(), item
                    .getAttributes().item(i).getNodeValue());
        }
        return props;
    }

}
