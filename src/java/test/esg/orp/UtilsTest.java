package esg.orp;

import junit.framework.Assert;

import org.junit.Test;

public class UtilsTest {
    
    @Test
    public void testTransform() {
        
        String url = "http://esg-datanode.jpl.nasa.gov/thredds/dodsC/esg_dataroot/obs4MIPs/observations/atmos/husNobs/mon/grid/NASA-JPL/AIRS/v20110608/husNobs_AIRS_L3_RetStd-v5_200209-201105.nc.das?plev[0:1]";
        String _url = Utils.transformUrl(url);
        Assert.assertEquals("http://esg-datanode.jpl.nasa.gov/thredds/fileServer/esg_dataroot/obs4MIPs/observations/atmos/husNobs/mon/grid/NASA-JPL/AIRS/v20110608/husNobs_AIRS_L3_RetStd-v5_200209-201105.nc", _url);
        
    }

}
