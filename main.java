
// rm *.class ; javac main.java  ; java main

//import java.io.BufferedReader; 
import java.io.FileReader; 
import java.io.InputStream ; 

import java.io.FileInputStream;
import java.io.BufferedInputStream; 

interface IX
{

}

class X implements IX
{

}

public class main {

    public static void main(String[] args) 
	{
		IX j = new X();
        System.out.println( "Hi there!"  ); // Display the string.

		try
		{
			//FileReader r = new FileReader("file.txt");  for text.

			// FileInputStream and BufferedInput stream for working with bytes
			InputStream input = new BufferedInputStream(new FileInputStream( "main.java" ));
			// input = new FileInputStream( "main.java" );

			int chunk = 100;
			byte[] result = new byte[ chunk ];
			int n = 0;  
			do 
			{
				//int n = input.read( result, 100 , bytesRemaining); 
				n = input.read( result, 0, chunk ); 
				System.out.println( "bytes read " + n ); // Display the string.
			} while( n >= 0); 


			input.close();
		}
		catch( Exception e )
		{
			//System.out.println( "exeption caught '" + e.getMessage() + "'" ); 
			e.printStackTrace();
		} 

    }
}
