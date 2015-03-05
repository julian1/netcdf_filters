
/*

	export CLASSPATH=.:netcdfAll-4.2.jar
	javac test4.java 
	java test4
*/

import ucar.nc2.NetcdfFileWriteable; 

public class test4 
{
    public static void main(String[] args)  throws Exception
	{
		String filename = "testWrite.nc";
		NetcdfFileWriteable ncfile = NetcdfFileWriteable.createNew(filename, false);

	}

}

