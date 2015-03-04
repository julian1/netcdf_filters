

import java.sql.*;

import java.util.Properties; 

// psql -h 127.0.0.1 -U meteo -d postgres     works

/*
	jdbc3 doesn't work for non-validating factory

	export CLASSPATH=.:postgresql-9.1-901.jdbc4.jar
	java test3
*/

public class test3 {

    public static void main(String[] args)  throws Exception
	{

		String url = "jdbc:postgresql://127.0.0.1/postgres";
		Properties props = new Properties();
		props.setProperty("user","meteo");
		props.setProperty("password","meteo");
		props.setProperty("ssl","true");
		props.setProperty("sslfactory","org.postgresql.ssl.NonValidatingFactory");

		props.setProperty("driver","org.postgresql.Driver" );

	

		Connection conn = DriverManager.getConnection(url, props);
		if( conn != null ) {

			System.out.println( "got conn" );

		}

		// String url = "jdbc:postgresql://localhost/test?user=fred&password=secret&ssl=true";
		// Connection conn = DriverManager.getConnection(url);
	}
}


