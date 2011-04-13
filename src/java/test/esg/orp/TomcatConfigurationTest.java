/*******************************************************************************
 * Copyright (c) 2011 Earth System Grid Federation
 * ALL RIGHTS RESERVED. 
 * U.S. Government sponsorship acknowledged.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of the <ORGANIZATION> nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package esg.orp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Properties;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TomcatConfigurationTest {
    private static File tmpServerFile;
    private static File catalinaHome;
    
    @BeforeClass
    public static void setupOnce() throws Exception {
        //create a tmp and dummy CATALINA_HOME
        catalinaHome = File.createTempFile(
                TomcatConfigurationTest.class.getName(), "");
        assertTrue(catalinaHome.delete());
        assertTrue(catalinaHome.mkdir());
        catalinaHome.deleteOnExit();
        
        File confDir = new File(catalinaHome, "conf");
        assertTrue(confDir.mkdir());
        confDir.deleteOnExit();
        
        //get the test server.xml file
        tmpServerFile = copyToDir("esg/orp/resources/server.xml", confDir);
        assertNotNull(tmpServerFile);
        assertTrue(tmpServerFile.isFile());
        tmpServerFile.deleteOnExit();
    }
    
    private static File copyToDir(String filename, File parent) throws IOException {
        File file = null;
        //get the input stream from the file (this should work even if it's in a jar
        InputStream in = TomcatConfigurationTest.class.getClassLoader().getResourceAsStream(filename);
        
        if (in != null) {
            //use the filename for the temp file
            if (parent != null)
                file = new File(parent, new File(filename).getName());
            else
                file = new File(new File(filename).getName());
            
            //prepare the channels
            FileChannel chnOut = new FileOutputStream(file).getChannel();
            ReadableByteChannel chnIn = Channels.newChannel(in);
            
            //copy input stream into file
            chnOut.transferFrom(chnIn, 0, Long.MAX_VALUE);
            
            //Copy done!
            chnIn.close();
            chnOut.close();
            
        } else {
            throw new FileNotFoundException("Could not find " + filename);
        }
        
        return file;
    }
    
    @Before
    public void setup() {
        //set catalina home properly
        System.setProperty("CATALINA_HOME", catalinaHome.getAbsolutePath());
    }
    
    
    @Test
    public void testNoFile() throws Exception {
        System.setProperty("CATALINA_HOME", "/path/to/nowhere/kansas");
        
        Properties props = TomcatConfiguration.getProperties();
        assertNull(props);
        
        //set back tomcat init flag, to re-trigger initialization 
        Field field = TomcatConfiguration.class.getDeclaredField("init");
        field.setAccessible(true);
        field.set(null, Boolean.FALSE);
        
    }
    
    @Test
    public void testGetProperties() {
        Properties props = TomcatConfiguration.getProperties();
        assertNotNull(props);
        
        //aser we get the corect properties
        String[] keys = new String[] { "port", "truststoreFile", "truststorePass",
                "keystoreFile", "keystorePass", "keyAlias" };
        String[] values = new String[] { "8443", "trustore.ts", "trustorepass",
                "keystore.ks", "keystorePass", "alias" };
        for (int i = 0; i < values.length; i++) {
            assertTrue(props.containsKey(keys[i]));
            assertEquals(values[i], props.getProperty(keys[i]));
        }
    }


}
