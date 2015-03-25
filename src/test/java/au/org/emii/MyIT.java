
package au.org.emii;


import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;



import java.sql.*;


public class MyIT {

/*
    private static Connection getConn() throws Exception
	{
		String url = "jdbc:postgresql://test-geoserver/harvest";
		Properties props = new Properties();
		props.setProperty("user","meteo");
		props.setProperty("password","meteo");

		props.setProperty("ssl","true");
		props.setProperty("sslfactory","org.postgresql.ssl.NonValidatingFactory");
		props.setProperty("driver","org.postgresql.Driver" );

		Connection conn = DriverManager.getConnection(url, props);
		if(conn == null) {
			throw new RuntimeException( "Could not get connection" );
		}
		return conn;

	}
*/

    public static void main(String[] args) throws Exception
	{


	}


    @Before
    public void mergeIT() {
		System.out.println( "**** whoot" );
    }   


    @Test
    public void newListIT() {
		System.out.println( "**** whoot" );


		assertTrue(123 == 123 );
		assertTrue(457 == 456 );

/*
		DecodeXmlConfiguration x = new DecodeXmlConfiguration(); 

		// ok, think we want a pair for encoders and dimensions pair. 
		Description description = x.test();

		// change name exprParser
		Parser parser = new Parser();

		IDialectTranslate translate = new PostgresDialectTranslate();

		Connection conn = getConn();

		ICreateWritable createWritable = new CreateWritable();  

		// avoiding ordering clauses that will prevent immediate stream response
		// we're going to need to sanitize this 	
		// note that we can wrap in double quotes 

		// change name virtualTable
		String instanceTable = "(select * from anmn_nrs_ctd_profiles.indexed_file )";
		String dataTable = "(select file_id as instance_id, * from anmn_nrs_ctd_profiles.measurements)";

		// Get rid of this and look it up as the dimension, 
		String dimensionVar = "DEPTH";

		//String filterExpr = " (and (gt TIME 2013-6-28T00:35:01Z ) (lt TIME 2013-6-29T00:40:01Z )) "; 
		String filterExpr = " (lt TIME 2013-6-29T00:40:01Z ) "; 

		Timeseries1 timeseries = new Timeseries1( 
			parser, translate, conn, createWritable, description, instanceTable, dataTable, dimensionVar, filterExpr );

		timeseries.init();	

		NetcdfFileWriteable writer = null;
		do {  
			writer = timeseries.get();	
		}
		while( writer != null );
*/
    }   

}
