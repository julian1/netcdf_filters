
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
	public ExprInteger( int pos, int value)
	{
		pos = pos;
		value = value;
	}
	int pos;
	int value; //
}

class ExprWhite implements IExpression
{
	public ExprWhite( int pos)
	{
		pos = pos;
	}
	int pos;
}


class ExprIdentifier implements IExpression
{
	public ExprIdentifier( int pos)
	{
		pos = pos;
	}
	int pos;
}




// actually why not pass a 
// string s, integer pos, boxed type....  

class Context
{

/*	public Context( String s, int pos) 
	{
		int pos2 = parseExpression( s, pos ); 
	}

	So i think we have to parse the thing, and store the pos in the
	returned result.
*/

	// | Int
	// | f '(' expr_list ')'   f 
	// | expr_list = 
	//			expr 
	// | '(' expr, expr ')'   tuple 

	IExpression parseExpression(String s, int pos)
	{
		// actually should try to glob up
		
		// try whitespace
		// try integer
		ExprInteger expr = parseInt(s, pos); 
		if(expr != null)
		{
			// append
			//System.out.println("whoot got integer!" + expr ); 
			//pos = expr.pos;
			return expr;
		}

		ExprIdentifier expr2 = parseIdentifier(s, pos); 
		if( expr2 != null)
		{


		} 
		return null;
	}



	ExprIdentifier parseIdentifier( String s, int pos)
	{
		if(Character.isLetter(s.charAt(pos))) {
//			while(Character.isSpaceChar(s.charAt(pos))) {
//				++pos;
			}	

		}
		return null;


		return null;
	}


	ExprWhite parseWhite( String s, int pos)
	{
		if(Character.isSpaceChar(s.charAt(pos))) {
			while(Character.isSpaceChar(s.charAt(pos))) {
				++pos;
			}	
			return new ExprWhite(pos);
		}
		return null;
	}

	ExprInteger parseInt( String s, int pos)
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
			return new ExprInteger( pos, value); 
		}
		return null;
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
