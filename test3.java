

import java.sql.*;

import java.util.Properties; 

// psql -h 127.0.0.1 -U meteo -d postgres     works

/*
	jdbc3 doesn't work for non-validating factory

	export CLASSPATH=.:postgresql-9.1-901.jdbc4.jar
	java test3
*/

public class test3 {

    public static void fuck2 ( Connection conn )  throws Exception
	{

		PreparedStatement stmt = conn.prepareStatement( "SELECT * FROM anmn_ts.measurement limit ?");

		stmt.setInt(1, new Integer( 1));//e.getValue().intValue());

		ResultSet rs = 	stmt.executeQuery();

		dumpResults ( rs );
	}	

    public static void dumpResults ( ResultSet rs )  throws Exception
	{
		while ( rs.next() ) {
			int numColumns = rs.getMetaData().getColumnCount();
			for ( int i = 1 ; i <= numColumns ; i++ ) {
			   // Column numbers start at 1.
			   // Also there are many methods on the result set to return
			   //  the column as a particular type. Refer to the Sun documentation
			   //  for the list of valid conversions.
			   System.out.println( "COLUMN " + i + " = " + rs.getObject(i) );
			}
		}
	}	

    public static void fuck ( Connection conn )  throws Exception
	{
		Statement stmt = conn.createStatement(); 
		ResultSet rs = stmt.executeQuery( "SELECT * FROM anmn_ts.measurement limit 1" );

		// close stmt, resultset and conn	
		dumpResults ( rs );
	}


    public static void main(String[] args)  throws Exception
	{

		//String url = "jdbc:postgresql://127.0.0.1/postgres";
		String url = "jdbc:postgresql://dbprod.emii.org.au/harvest";
		Properties props = new Properties();
		props.setProperty("user","jfca");
		props.setProperty("password","fredfred");
		props.setProperty("ssl","true");
		props.setProperty("sslfactory","org.postgresql.ssl.NonValidatingFactory");

		props.setProperty("driver","org.postgresql.Driver" );


		Connection conn = DriverManager.getConnection(url, props);
		if( conn != null ) {

			System.out.println( "got conn" );
			
			fuck2( conn );

		}

	}
}

//			Statement stmt = conn.createStatement(); 
		//	ResultSet rs = stmt.executeQuery( "SELECT * FROM anmn_ts.measurement limit 1" );
		// String url = "jdbc:postgresql://localhost/test?user=fred&password=secret&ssl=true";
		// Connection conn = DriverManager.getConnection(url);



