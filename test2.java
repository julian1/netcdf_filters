
// rm *.class ; javac main.java  ; java main

//import java.io.BufferedReader; 
import java.io.FileReader; 
import java.io.InputStream ; 

import java.io.FileInputStream;
import java.io.BufferedInputStream; 

import java.util.StringTokenizer;

// string tokenizer isn't going to work because may not be strings. 

// do everything twice? 
// or leave the value there...

interface IExpression
{

}

class ExprInteger implements IExpression
{
	int value; //
}

// actually why not pass a 
// string s, integer pos, boxed type....  

class Context
{

	public Context( String s, int pos) 
	{
		int pos2 = parseExpression( pos ); 
	
	}

	int parseExpression( int pos )
	{
		// try integer
		ExprInteger expr = null;
		int pos2 = parseInt( pos, expr); 
		if( pos2 != pos)
		{
			// append

		}
		return pos2;
	}

	int parseInt( int pos, ExprInteger expr )
	{
		// if it succeeds then we return the value
		// it maybe that we want to not use Integer but instead an expression . ExprInteger or IntegerExpression 
		// or TupleExpression

		expr = new ExprInteger( ); 
		return pos + 10;
	}
}

// or we tokenize the

// Why not try to do it recursively
// to return a tuple means we lose the typing if we use an integer 



public class test2 {

    public static void main(String[] args) 
	{
		String s = "and ( contains(geom, box( (0,0), ... ), less ( time , 1.1.2015 )"; 


		Context c = new Context( s, 0 );

		int pos = 0;

		if(s.charAt(pos) >= '0' &&  s.charAt(pos) <= '9') {  
			while(s.charAt(pos) >= '0' &&  s.charAt(pos) <= '9') {
				++pos;
			}	
		}
		else {

			System.out.println( "not an integer !"  ); // Display the string.
		}

/*
		 StringTokenizer st = new StringTokenizer( s);
		 while (st.hasMoreTokens()) {
			String x = st.nextToken();
			 System.out.println( x );
		 }
*/

    }
}
