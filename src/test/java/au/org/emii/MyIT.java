
package au.org.emii.ncfgenerator;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


import java.io.InputStream ;

import ucar.nc2.NetcdfFileWriteable; 

//import java.sql.*;


public class MyIT {

    @Before
    public void mergeIT() {

		// setup db connection? 
    }   


	private void streamData( NcdfEncoder generator ) throws Exception {
		NetcdfFileWriteable writer = null;
		do {  
			// should try and get lots...
			writer = generator.get();	
		}
		while( writer != null );
	}

	// we should expose the db... 

    @Test
    public void anmn_nrs_ctd_profiles_IT() throws Exception {

		System.out.println( "**** anmn_nrs_ctd_profiles **** " );
		// exception handling needs to be improved a lot...
		// assertTrue(123 == 123 );
		// assertTrue(456 == 456 );

		InputStream config = getClass().getResourceAsStream("/anmn_nrs_ctd_profiles.xml");
		NcdfEncoder generator = new NcdfEncoderBuilder().create(	
			config,
			" (lt TIME 2013-6-29T00:40:01Z ) " 
		);

		streamData( generator ); 
		System.out.println( "finished test" );
    }   

	@Test
    public void anmn_timeseries_IT() throws Exception {

		System.out.println( "**** anmn timeseries ****" );
		// assertTrue(123 == 123 );
		// assertTrue(456 == 456 );
		InputStream config = getClass().getResourceAsStream("/anmn_timeseries.xml");
		NcdfEncoder generator = new NcdfEncoderBuilder().create(	
			config,
			 " (and (gt TIME 2013-6-28T00:35:01Z ) (lt TIME 2013-6-29T00:40:01Z )) "
			// " (lt TIME 2013-6-29T00:40:01Z ) "
		);

		streamData( generator ); 
		System.out.println( "finished test" );
    }   

	@Test
    public void soop_sst_trajectory_IT() throws Exception {

		System.out.println( "**** sst trajectory ****" );
		InputStream config = getClass().getResourceAsStream("/soop_sst_trajectory.xml");
		NcdfEncoder generator = new NcdfEncoderBuilder().create(	
			config,
			 " (and (gt TIME 2013-6-27T00:35:01Z ) (lt TIME 2013-6-29T00:40:01Z )) "
			// " (lt TIME 2013-6-29T00:40:01Z ) "
		);

		// can expect a count ...
		streamData( generator ); 
		System.out.println( "finished test" );
    }   

}



