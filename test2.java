
// rm *.class ; javac main.java  ; java main

//import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream ;

import java.io.FileInputStream;
import java.io.BufferedInputStream;

import java.util.ArrayList; //io.BufferedInputStream;

//import java.util.StringTokenizer;

// string tokenizer isn't going to work because may not be strings.

// do everything twice?
// or leave the value there...


interface Visitor
{
	public void visit(  ExprInteger expr );
	public void visit(  ExprIdentifier expr );
}



interface IExpression
{
	public int get_position() ; 
	public void accept( Visitor v ) ; 
}

class ExprInteger implements IExpression
{
	public ExprInteger( int pos_, int value_)
	{
		pos = pos_;
		value = value_;
	}


	public int get_position() { return pos; } 

	public void accept( Visitor v )  { v.visit( this); }  

	final int pos;
	final int value; //
}

/*
class ExprWhite implements IExpression
{
	public ExprWhite( int pos_)
	{
		pos = pos_;
	}
	public int get_position() { return pos; } 
	public void visit( Visitor v )  { }  
	int pos;
}
*/


class ExprIdentifier implements IExpression
{
	public ExprIdentifier( int pos_, String symbol_, ArrayList<IExpression> children_  )
	{
		pos = pos_;
		symbol = symbol_;
		children = children_;
	}

	public int get_position() { return pos; } 
	public void accept( Visitor v )  { v.visit( this); }  

	final int		pos;
	final String symbol;
	final ArrayList<IExpression> children;
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
		// advance over whitespace
		while(Character.isSpaceChar(s.charAt(pos))) {
			++pos;
		}

		// try integer
		ExprInteger expr = parseInt(s, pos);
		if(expr != null)
		{
			// append
			System.out.println("got " + expr );
			return expr;
		}

		ExprIdentifier expr2 = parseIdentifier(s, pos);
		if(expr2 != null)
		{
			System.out.println("got " + expr2 );
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
		++pos;

		// advance whitespace
		while(Character.isSpaceChar(s.charAt(pos))) {
			++pos;
		}

		// pull out the symbol...
		if(Character.isLetter(s.charAt(pos)) || s.charAt(pos) == '_' ) {
			StringBuilder b = new StringBuilder();
			while(Character.isLetter(s.charAt(pos)) 
				|| Character.isDigit(s.charAt(pos)) 
				|| s.charAt(pos) == '_') {
				b.append(s.charAt(pos));
				++pos;
			}
			//return new ExprIdentifier(pos, b.toString());
			//System.out.println("got symbol !" + symbol );
			//System.out.println("- pos now " + pos);
			symbol = b.toString();
		}
		// we must have white 
		// no we just have to parse the expression.

		ArrayList<IExpression> children = new ArrayList<IExpression>();
		IExpression child = null;
		do {	
			// System.out.println("- pos before parsing expr " + pos);
			child = parseExpression( s, pos); 
			if( child != null ) {
				children.add( child);
				//System.out.println("childot subexpr !" );
				pos = child.get_position();
				//System.out.println("- pos now" + pos);
			}
		} while(child != null);

		// advance whitespace
		while(Character.isSpaceChar(s.charAt(pos))) {
			++pos;
		}

		if(s.charAt(pos) != ')')
			return null;
		++pos;

		return new ExprIdentifier ( pos, symbol, children );
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
			while(Character.isDigit(s.charAt(pos))) {
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



class PrettyPrinter implements Visitor
{
	// think our naming is incorrect

	public void visit(  ExprInteger expr )
	{
		System.out.print( "Integer " + expr.value );
	}

	public void visit( ExprIdentifier expr )
	{
		System.out.print( "(" + expr.symbol + " " );

		for( IExpression child : expr.children ) {
			child.accept(this);
			System.out.print( ", ");
		}
		System.out.println( ")" );
	}
}



public class test2 {

    public static void main(String[] args)
	{
		//String s = "777 and ( contains(geom, box( (0,0), ... ), less ( time , 1.1.2015 )";
		//String s = "(contains 123 (geom, box( (0,0), ... ), less ( time , 1.1.2015 )";
		//String s = "(contains  (uuu 123 789) 456) ";
		//String s = "(contains (f 456) 789 888) ";
		String s = "(contains 123 (f 456 789) (f2 999) 1000)";

		Context c = new Context();
		IExpression expr = c.parseExpression( s, 0);

		if( expr != null) {
			System.out.println( "got an expression" );

			PrettyPrinter pp = new PrettyPrinter() ;
			expr.accept( pp);
		}
	}
}
