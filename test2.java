
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
	public int get_position() ; 
}

class ExprInteger implements IExpression
{
	public ExprInteger( int pos, int value)
	{
		pos = pos;
		value = value;
	}


	public int get_position() { return pos; } 

	int pos;
	int value; //
}

class ExprWhite implements IExpression
{
	public ExprWhite( int pos)
	{
		pos = pos;
	}

	public int get_position() { return pos; } 
	int pos;
}


class ExprIdentifier implements IExpression
{
	public ExprIdentifier( int pos, String symbol )
	{
		pos = pos;
		symbol = symbol;
	}

	public int get_position() { return pos; } 

	int		pos;
	String symbol;
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

	think that we might need an abstract ability to get the position.
	from an upcvlassed object...

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
		// may want to embed this specifically
/*
		ExprWhite white = parseWhite(s, pos);
		if( white != null) {
			System.out.println("whoot got white!" + white.pos );
			pos = white.pos;
		}
*/

		while(Character.isSpaceChar(s.charAt(pos))) {
			++pos;
		}


		// try integer
		ExprInteger expr = parseInt(s, pos);
		if(expr != null)
		{
			// append
			System.out.println("whoot got integer!" + expr );
			return expr;
		}

		ExprIdentifier expr2 = parseIdentifier(s, pos);
		if( expr2 != null)
		{
			System.out.println("got sexp !" + expr );
			return expr2;
		}
		return null;
	}


	// a tuple is just an unnamed s-expression, i don't think we even really need it.

	// why not have a parse string and just appropriate the generated classes
	// alternatively we could actually point at the symbol.
	// ('+' a b)

	ExprIdentifier parseIdentifier(String s, int pos)
	{
		String symbol = "";

		// assume we've done white 
		if(s.charAt(pos) != '(')
			return null;

		// should be whitespace check
		++pos;

		// pull out the symbol...
		if(Character.isLetter(s.charAt(pos))  ) {
			StringBuilder b = new StringBuilder();
			while(Character.isLetter(s.charAt(pos)) || s.charAt(pos) == '_') {
				b.append(s.charAt(pos));
				++pos;
			}
			//return new ExprIdentifier(pos, b.toString());

			System.out.println("got symbol !" + symbol );
			symbol = b.toString();
		}
		// we must have white 
		// no we just have to parse the expression.

		IExpression g = null;
		do {	
			g = parseExpression( s, pos); 
			if( g != null ) {
				System.out.println("got subexpr !" );
				pos = g.get_position();
			}
		} while( g != null);


		ExprIdentifier p = new ExprIdentifier ( pos, symbol );

		return p ;
	}
	
	// perhaps add a depth as well 

/*
	ExprWhite parseWhite(String s, int pos)
	{

		System.out.println(" parseWhite " + pos  );

		if(Character.isSpaceChar(s.charAt(pos))) {
			while(Character.isSpaceChar(s.charAt(pos))) {
				++pos;
			}
			return new ExprWhite(pos);
		}
		return null;
	}
*/

	ExprInteger parseInt( String s, int pos)
	{
		// we are committed
		if(Character.isDigit(s.charAt(pos))) {
			StringBuilder b = new StringBuilder();
			while( Character.isDigit(s.charAt(pos))) {
				b.append(s.charAt(pos));
				++pos;
			}
			int value = Integer.parseInt(b.toString());
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
		//String s = "777 and ( contains(geom, box( (0,0), ... ), less ( time , 1.1.2015 )";
		//String s = "(contains 123 (geom, box( (0,0), ... ), less ( time , 1.1.2015 )";
		String s = "(contains  123) ";


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
