
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
	public void visit(  ExprProc expr );
	public void visit(  ExprLiteral expr );
}

interface IExpression
{
	public int getPosition() ;
	public void accept( Visitor v ) ;
}

class ExprInteger implements IExpression
{
	public ExprInteger( int pos_, int value_)
	{
		pos = pos_;
		value = value_;
	}

	public int getPosition() { return pos; }
	public void accept( Visitor v )  { v.visit( this); }

	final int pos;
	final int value; //
}

class ExprLiteral implements IExpression
{
	public ExprLiteral( int pos_, String value_)
	{
		pos = pos_;
		value = value_;
	}

	public int getPosition() { return pos; }
	public void accept( Visitor v )  { v.visit( this); }
	final int pos;
	final String value; //
}


/*
class ExprWhite implements IExpression
{
	public ExprWhite( int pos_)
	{
		pos = pos_;
	}
	public int getPosition() { return pos; }
	public void visit( Visitor v )  { }
	int pos;
}
*/


class ExprProc implements IExpression
{
	public ExprProc( int pos_, String symbol_, ArrayList<IExpression> children_  )
	{
		pos = pos_;
		symbol = symbol_;
		children = children_;
	}

	public int getPosition() { return pos; }
	public void accept( Visitor v )  { v.visit( this); }

	final int		pos;
	final String symbol;
	final ArrayList<IExpression> children;
}




// actually why not pass a
// string s, integer pos, boxed type....

class Parser
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

	// TODO we should check that we have matched the string entirely 
	// with whitespace at the end... parseEOF or similar? 

	IExpression parseExpression(String s, int pos)
	{
		// advance whitespace
		while(Character.isSpaceChar(s.charAt(pos))) {
			++pos;
		}
		// integer
		IExpression expr = parseInt(s, pos);
		if(expr != null)
			return expr;

		// literal
		expr = parseLiteral(s, pos);
		if(expr != null)
			return expr;

		// proc
		expr = parseProc(s, pos);
		if(expr != null)
			return expr;

		return null;
	}


	// a tuple is just an unnamed s-expression, i don't think we even really need it.

	// why not have a parse string and just appropriate the generated classes
	// alternatively we could actually point at the symbol.
	// ('+' a b)

	ExprProc parseProc(String s, int pos)
	{
		String symbol = null;

		if(s.charAt(pos) != '(')
			return null;
		++pos;

		// advance whitespace
		while(Character.isSpaceChar(s.charAt(pos))) {
			++pos;
		}

		// symbol
		if(Character.isLetter(s.charAt(pos)) || s.charAt(pos) == '_' ) {
			StringBuilder b = new StringBuilder();
			while(Character.isLetter(s.charAt(pos))
				|| Character.isDigit(s.charAt(pos))
				|| s.charAt(pos) == '_') {
				b.append(s.charAt(pos));
				++pos;
			}
			symbol = b.toString();
		}

		// children
		ArrayList<IExpression> children = new ArrayList<IExpression>();
		IExpression child = null;
		do {
			child = parseExpression( s, pos);
			if( child != null ) {
				children.add( child);
				pos = child.getPosition();
			}
		} while(child != null);

		// advance whitespace
		while(Character.isSpaceChar(s.charAt(pos))) {
			++pos;
		}

		if(s.charAt(pos) != ')')
			return null;
		++pos;

		return new ExprProc ( pos, symbol, children );
	}


	ExprInteger parseInt( String s, int pos)
	{
		if(Character.isDigit(s.charAt(pos))) {
			// we are committed
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

	ExprLiteral parseLiteral( String s, int pos)
	{
		// don't worry about escaping for now
		if(s.charAt(pos) != '\'')
			return null;
		++pos;

		StringBuilder b = new StringBuilder();
		// should test s length as well...
		while(s.charAt(pos) != '\'') {
			b.append(s.charAt(pos));
			++pos;
		}

		if(s.charAt(pos) != '\'')
			return null;
		++pos;

		return new ExprLiteral( pos, b.toString() );
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
		System.out.print( "Integer:" + expr.value );
	}

	public void visit(  ExprLiteral expr )
	{
		System.out.print( "Literal:" + expr.value );
	}

	public void visit( ExprProc expr )
	{
		System.out.print( "(" + expr.symbol + " " );
		for( IExpression child : expr.children ) {
			child.accept(this);
			System.out.print( " ");
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
		//String s = "(contains 123 (f 456 789) (f2 999) 1000 1001)";
		String s = "(equals 'platform_number' 456) ";

		Parser c = new Parser();
		IExpression expr = c.parseExpression( s, 0);

		if( expr != null) {
			System.out.println( "got an expression" );
			PrettyPrinter pp = new PrettyPrinter() ;
			expr.accept( pp);
		}
		else {
			System.out.println( "expression parse failed" );

		}
	}
}



