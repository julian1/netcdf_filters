
/*
	export CLASSPATH=.:netcdfAll-4.2.jar
	javac test4.java 
	java test4

	This worked too

	unset CLASSPATH
	javac test4.java  -cp .:netcdfAll-4.2.jar
	java -classpath '.:netcdfAll-4.2.jar' test4 

*/

import ucar.nc2.NetcdfFileWriteable; 
import ucar.nc2.Dimension; 
//import ucar.nc2.DataType.DOUBLE; 
//import ucar.nc2.*; 

import ucar.ma2.DataType; 
import ucar.ma2.Array;

import java.io.IOException;

import java.util.ArrayList; 

public class test4 
{
    public static void main(String[] args)  throws Exception
	{

   String filename = "testWrite.nc";
 NetcdfFileWriteable ncfile = NetcdfFileWriteable.createNew(filename, false);

   // add dimensions
 Dimension latDim = ncfile.addDimension("lat", 5);
   Dimension lonDim = ncfile.addDimension("lon", 6);

   // define Variable
   ArrayList dims = new ArrayList();

   dims.add( latDim);
   dims.add( lonDim);
 ncfile.addVariable("temperature", DataType.DOUBLE, dims);
 ncfile.addVariableAttribute("temperature", "units", "K");

   // add a 1D attribute of length 3
 Array data = Array.factory( int.class, new int [] {3}, new int[] {1,2,3});
 ncfile.addVariableAttribute("temperature", "scale", data);

   // add a string-valued variable: char svar(80)
   Dimension svar_len = ncfile.addDimension("svar_len", 80);
   dims = new ArrayList();
   dims.add( svar_len);
 ncfile.addVariable("svar", DataType.CHAR, dims);

   // string array: char names(3, 80)
   Dimension names = ncfile.addDimension("names", 3);
   ArrayList dima = new ArrayList();
   dima.add(names);
   dima.add(svar_len);
 ncfile.addVariable("names", DataType.CHAR, dima);

   // how about a scalar variable?
 ncfile.addVariable("scalar", DataType.DOUBLE, new ArrayList());

   // add global attributes
	ncfile.addGlobalAttribute("yo", "face");
   ncfile.addGlobalAttribute("versionD", new Double(1.2));
   ncfile.addGlobalAttribute("versionF", new Float(1.2));
   ncfile.addGlobalAttribute("versionI", new Integer(1));
   ncfile.addGlobalAttribute("versionS", new Short((short)2));
   ncfile.addGlobalAttribute("versionB", new Byte((byte)3));

   // create the file
   try {
 ncfile.create();

	 ncfile.close();

   } catch (IOException e) {
  System.err.println("ERROR creating file "+ncfile.getLocation()+"\n"+e);
   }

/**/
	}

}

