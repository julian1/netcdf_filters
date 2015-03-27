
package au.org.emii.ncfgenerator;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


import java.io.InputStream ;

// use a stream here. 
import ucar.nc2.NetcdfFileWriteable; 

//import java.sql.*;


public class MyIT {

    @Before
    public void mergeIT() {
		System.out.println( "**** MYIT BEFORE " );

		// should configure connection? 
    }   


	 public void streamData( NcfGenerator generator ) throws Exception {
		NetcdfFileWriteable writer = null;
		do {  
			// should try and get lots...
			writer = generator.get();	
		}
		while( writer != null );
	}

    @Test
    public void test1_IT() throws Exception {

		System.out.println( "**** whoot INTEGRATION 1 **** " );
		// exception handling needs to be improved a lot...
		// assertTrue(123 == 123 );
		// assertTrue(456 == 456 );

		InputStream config = getClass().getResourceAsStream("/anmn_nrs_ctd_profiles.xml");
		NcfGenerator generator = new NcfGeneratorBuilder().create(	
			config,
			"anmn_nrs_ctd_profiles", 
			"select * from indexed_file",
		    "select file_id as instance_id, * from measurements",
			" (lt TIME 2013-6-29T00:40:01Z ) " 
		);

		streamData( generator ); 
		System.out.println( "finished test" );
    }   

	@Test
    public void test2_IT() throws Exception {

		System.out.println( "**** anmn timeseries ****" );
		// assertTrue(123 == 123 );
		// assertTrue(456 == 456 );
		InputStream config = getClass().getResourceAsStream("/anmn_timeseries.xml");
		NcfGenerator generator = new NcfGeneratorBuilder().create(	
			config,
			"anmn_ts", 
			"select * from timeseries",
		    "select ts_id as instance_id, * from measurement",
			 " (and (gt TIME 2013-6-28T00:35:01Z ) (lt TIME 2013-6-29T00:40:01Z )) "
			// " (lt TIME 2013-6-29T00:40:01Z ) "
		);

		streamData( generator ); 
		System.out.println( "finished test" );
    }   

	@Test
    public void test3_IT() throws Exception {

		System.out.println( "**** sst trajectory ****" );
		InputStream config = getClass().getResourceAsStream("/soop_sst_trajectory.xml");
		NcfGenerator generator = new NcfGeneratorBuilder().create(	
			config,
			"soop_sst", 
			"select * from indexed_file",
		    "select trajectory_id as instance_id, * from measurements",
			 " (and (gt TIME 2013-6-27T00:35:01Z ) (lt TIME 2013-6-29T00:40:01Z )) "
			// " (lt TIME 2013-6-29T00:40:01Z ) "
		);

		// can expect a count ...
		streamData( generator ); 
		System.out.println( "finished test" );
    }   

}



