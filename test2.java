
// rm *.class ; javac main.java  ; java main

//import java.io.BufferedReader; 
import java.io.FileReader; 
import java.io.InputStream ; 

import java.io.FileInputStream;
import java.io.BufferedInputStream; 

//import java.util.StringTokenizer;

// string tokenizer isn't going to work because may not be strings. 

// do everything twice? 
// or leave the value there...

interface IExpression
{

}

class ExprInteger implements IExpression
{
	public ExprInteger( int value)
	{
		value = value;
	}
	int value; //
}

// actually why not pass a 
// string s, integer pos, boxed type....  

class Context
{

/*	public Context( String s, int pos) 
	{
		int pos2 = parseExpression( s, pos ); 
	}
*/

	// | Int
	// | f '(' expr_list ')'   f 
	// | expr_list = 
	//			expr 
	// | '(' expr, expr ')'   tuple 

	int parseExpression( String s, int pos )
	{
		// actually should try to gobble up
		

		// try whitespace
		int pos2  = 0;


		// try integer
		ExprInteger expr = null;
		pos2 = parseInt( s, pos, expr); 
		if( pos2 != pos)
		{
			// append
			System.out.println("whoot got integer!" + expr ); 
		}
		
		return pos2;
	}


	int parseWhite( String s, int pos)
	{
		while(Character.isSpaceChar(s.charAt(pos))) {
			++pos;
		}	
		return pos;
	}

	int parseInt( String s, int pos, ExprInteger expr )
	{
		// we are committed
		if(Character.isDigit(s.charAt(pos))) {  
			StringBuilder buf = new StringBuilder();
			while( Character.isDigit(s.charAt(pos))) {
				buf.append(s.charAt(pos));
				++pos;
			}	
			int value = Integer.parseInt(buf.toString());
			System.out.println( "whoot integer "  + Integer.toString( value )  ); 
			expr = new ExprInteger( value); 
		}
		return pos;
	}
}

// or we tokenize the
// Why not try to do it recursively
// to return a tuple means we lose the typing if we use an integer 
// if it succeeds then we return the value
// it maybe that we want to not use Integer but instead an expression . ExprInteger or IntegerExpression 
// or TupleExpression



public class test2 {

    public static void main(String[] args) 
	{
		String s = "777 and ( contains(geom, box( (0,0), ... ), less ( time , 1.1.2015 )"; 


		Context c = new Context();// s, 0 );
		c.parseExpression( s, 0);

/*
		int pos = 0;

		if(s.charAt(pos) >= '0' &&  s.charAt(pos) <= '9') {  
			while(s.charAt(pos) >= '0' &&  s.charAt(pos) <= '9') {
				++pos;
			}	
		}
		else {
			System.out.println( "not an integer !"  ); // Display the string.
		}
*/

/*
		 StringTokenizer st = new StringTokenizer( s);
		 while (st.hasMoreTokens()) {
			String x = st.nextToken();
			 System.out.println( x );
		 }
*/

    }
}
