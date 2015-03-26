
package au.org.emii;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


import ucar.nc2.NetcdfFileWriteable; 

//import java.sql.*;


public class MyIT {

    @Before
    public void mergeIT() {
		System.out.println( "**** MYIT BEFORE " );
    }   


    @Test
    public void newListIT() throws Exception {

		System.out.println( "**** whoot INTEGRATION 2 " );
		// we need to catch exceptions ...
		assertTrue(123 == 123 );
		assertTrue(456 == 456 );

		NcfGenerator generator = new NcfGeneratorBuilder().create(	
			"anmn_nrs_ctd_profiles", 
			"select * from indexed_file",
		    "select file_id as instance_id, * from measurements",
			"DEPTH",
			" (lt TIME 2013-6-29T00:40:01Z ) " 
		);

		NetcdfFileWriteable writer = null;
		do {  
			writer = generator.get();	
		}
		while( writer != null );
    }   

}
