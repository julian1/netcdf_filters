
// rm *.class ; javac main.java  ; java main

//import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream ;

import java.io.FileInputStream;
import java.io.BufferedInputStream;

import java.util.ArrayList; //io.BufferedInputStream;


import java.sql.*;

import java.util.Properties;
import java.lang.RuntimeException;

import java.text.SimpleDateFormat;
/*import java.util.List;
import java.util.Set;
import java.util.ArrayList;
*/
//import java.util.Date;



//import java.util.StringTokenizer;

// string tokenizer isn't going to work because may not be strings.

// do everything twice?
// or leave the value there...


interface Visitor
{
	public void visit( ExprInteger expr );
	public void visit( ExprProc expr );
	public void visit( ExprLiteral expr );
	public void visit( ExprSymbol expr );
	public void visit( ExprTimestamp expr );
}

interface IExpression
{
	public int getPosition() ;
	public void accept( Visitor v ) ;
}

class ExprSymbol implements IExpression
{
	public ExprSymbol( int pos, String value)
	{
		this.pos = pos;
		this.value = value;
	}

	public int getPosition() { return pos; }
	public void accept( Visitor v )  { v.visit( this); }

	final int pos;
	final String value; //
}

class ExprInteger implements IExpression
{
	public ExprInteger( int pos, int value)
	{
		this.pos = pos;
		this.value = value;
	}

	public int getPosition() { return pos; }
	public void accept( Visitor v )  { v.visit( this); }

	final int pos;
	final int value; //
}

class ExprTimestamp implements IExpression
{
	public ExprTimestamp( int pos, Timestamp value)
	{
		this.pos = pos;
		this.value = value;
	}

	public int getPosition() { return pos; }
	public void accept( Visitor v )  { v.visit( this); }

	final int pos;
	final Timestamp value; //
}


class ExprLiteral implements IExpression
{
	public ExprLiteral( int pos, String value)
	{
		this.pos = pos;
		this.value = value;
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
	public ExprProc( int pos, String symbol, ArrayList<IExpression> children  )
	{
		this.pos = pos;
		this.symbol = symbol;
		this.children = children;
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

	// the input source is actually constant. while the pos needs to be held
	// on the stack
	// potentially we should keep the buffer state around...

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


	public Parser() {
			// TODO don't generate this every time...
		df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	}

	SimpleDateFormat df;

	IExpression parseExpression(String s, int pos)
	{
		// advance whitespace
		while(Character.isSpaceChar(s.charAt(pos))) {
			++pos;
		}
		// timestamp
		IExpression expr = parseTimestamp(s, pos);
		if(expr != null) {
			System.out.println( "parsed Timestamp" );
			return expr;
		}

		// integer
		expr = parseInt(s, pos);
		if(expr != null)
			return expr;
		// literal
		expr = parseLiteral(s, pos);
		if(expr != null)
			return expr;
		expr = parseSymbol( s, pos );
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

	ExprSymbol parseSymbol( String s, int pos)
	{
		// atom....
		// symbol
		int pos2 = pos;
		if(Character.isLetter(s.charAt(pos2)) || s.charAt(pos2) == '_' ) {
			while(Character.isLetter(s.charAt(pos2))
				|| Character.isDigit(s.charAt(pos2))
				|| s.charAt(pos2) == '_') {
				++pos2;
			}
			return new ExprSymbol( pos2, s.substring(pos,pos2));
		}
		return null;
	}


	ExprTimestamp parseTimestamp( String s, int pos )
	{
		// eg. if it looks like a date
		int pos2 = pos;
		while(Character.isDigit(s.charAt(pos2))
			|| s.charAt(pos2) == '-'
			|| s.charAt(pos2) == ':'
			|| s.charAt(pos2) == 'Z'
			|| s.charAt(pos2) == 'T'
		) ++pos2;

		if(pos != pos2) {
			try {
				String x = s.substring( pos, pos2);
				Timestamp d = new java.sql.Timestamp(df.parse(x).getTime());
				return new ExprTimestamp( pos2, d);
			} catch( Exception e ) {
			}
		}
		return null;
	}

	ExprInteger parseInt( String s, int pos )
	{
		int pos2 = pos;
		while(Character.isDigit(s.charAt(pos2))) 
			++pos2;

		if( pos != pos2) {
			int value = Integer.parseInt(s.substring(pos, pos2));
			return new ExprInteger(pos2, value);
		}
		return null;
	}

	ExprLiteral parseLiteral( String s, int pos )
	{
		// TODO pos2
		int pos2 = pos;
		// ignore escaping for the moment
		if(s.charAt(pos2) != '\'')
			return null;
		++pos2;

		while(s.charAt(pos2) != '\'')
			++pos2;

		++pos2;
		return new ExprLiteral(pos, s.substring( pos + 1, pos2 - 1));
	}

/*
	ExprLiteral parseLiteral( String s, int pos)
	{
		"2012-01-01T00:00:00Z";
	}
*/

}



// or we tokenize the
// Why not try to do it recursively
// to return a tuple means we lose the typing if we use an integer
// if it succeeds then we return the value
// it maybe that we want to not use Integer but instead an expression . ExprInteger or IntegerExpression
// or TupleExpression



class PrettyPrinterVisitor implements Visitor
{
	// should take the stream on the constructor

	// think our naming is incorrect

	public void visit( ExprSymbol expr )
	{
		System.out.print( "Symbol:" + expr.value );
	}

	public void visit(  ExprInteger expr )
	{
		System.out.print( "Integer:" + expr.value );
	}

	public void visit( ExprTimestamp expr )
	{
		System.out.print( "Timestamp:" + expr.value );
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



class SelectionGenerationVisitor implements Visitor
{
	// rename PGDialectSelectionGenerator

	// we don't know the bloody index offset of the parameter.
	// we might have

	// should take the stream on the constructor
	// actually just a string builder...

	StringBuilder b;

	// parameters...

	// ok, 

	public SelectionGenerationVisitor( StringBuilder b )
	{
		this.b = b;
	}

	// think our naming is incorrect
	public void visit( ExprInteger expr )
	{
		// This should actually emit a '?' and load the value into the sql parameter list
		// to avoid sql injection
		b.append( expr.value );
	}

	public void visit( ExprTimestamp expr )
	{
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		b.append( "'" + df.format(expr.value ) + "'" );
	}

	public void visit( ExprLiteral expr )
	{
		b.append("'"+ expr.value + "'");
	}

	public void visit( ExprSymbol expr )
	{
		// must quote field to enforce full case handling
		b.append('\"' + expr.value + '\"' );
	}

	public void visit( ExprProc expr )
	{
		String symbol = expr.symbol;

		if(symbol.equals("equals")) {
			emitInfixSqlExpr( "=", expr );
		}
		else if(symbol.equals("gt")) {
			emitInfixSqlExpr( ">", expr );
		}
		else if(symbol.equals("geq")) {
			emitInfixSqlExpr( ">=", expr );
		}
		else if( symbol.equals("and")
			|| symbol.equals("or")
			) {
			emitInfixSqlExpr( symbol, expr );
		}
		else {
			throw new RuntimeException( "Unrecognized proc expression symbol '" + symbol + "'" );
		}
	}

	public void emitInfixSqlExpr( String op, ExprProc expr )
	{
		// if expansion is done in order we may be ok,....
		b.append('(');
		expr.children.get(0).accept(this);
		b.append(' ');
		b.append(op);
		b.append(' ');
		expr.children.get(1).accept(this);
		b.append(')');
	}
}


// we need raw identifiers which is the way cql does it.





/*
	jdbc3 doesn't work for non-validating factory
	export CLASSPATH=.:postgresql-9.1-901.jdbc4.jar
	java test3
*/

class Test3 {

	Connection conn;

	public Test3( Connection conn )
	{
		this.conn = conn;
	}

    public void doQuery2 ( String query  )  throws Exception
	{
		//PreparedStatement stmt = conn.prepareStatement( "SELECT * FROM anmn_ts.measurement limit ?");
		PreparedStatement stmt = conn.prepareStatement( query );
		//stmt.setInt(1, new Integer( 1));
		//stmt.setInt(1, 1 );
		ResultSet rs = 	stmt.executeQuery();
		dumpResults ( rs );
	}

	// we need to query the timeseries and then the measurements.
	// this wants to be a separate class to output ...

	// eg. encode row. 

    public void dumpResults ( ResultSet rs )  throws Exception
	{
		ResultSetMetaData m = rs.getMetaData();
		int numColumns = m.getColumnCount();

		for ( int i = 1 ; i <= numColumns ; i++ ) {
			System.out.println( "" + i + " " + m.getColumnClassName( i ) + ", " + m.getColumnName(i ) );
		}

		while ( rs.next() ) {
			for ( int i = 1 ; i <= numColumns ; i++ ) {
			   // Column numbers start at 1.
			   System.out.println( "" + i + " = " + rs.getObject(i) );
			}
		}
	}

}


class Timeseries
{
	/*
		collaborators
			- 
			- selector (filter) subsetter
			- conn
			- limit
			- netcdf encoder 
			- streaming output / whatever output control we need (should just push encoded netcdf to outputer ) 
	*/

	public Timeseries( ) {
		// we need to inject the selector ...
		// 

	}
}



public class test2 {

    public static Connection getConn() throws Exception
	{
		//String url = "jdbc:postgresql://127.0.0.1/postgres";

		String url = "jdbc:postgresql://dbprod.emii.org.au/harvest";
		Properties props = new Properties();
		props.setProperty("user","jfca");
		props.setProperty("password","fredfred");
		props.setProperty("ssl","true");
		props.setProperty("sslfactory","org.postgresql.ssl.NonValidatingFactory");
		props.setProperty("driver","org.postgresql.Driver" );

		Connection conn = DriverManager.getConnection(url, props);
		if(conn == null) {
			throw new RuntimeException( "Could not get connection" );
		}
		return conn;
	}


    public static void main(String[] args) throws Exception
	{


		//String s = "777 and ( contains(geom, box( (0,0), ... ), less ( time , 1.1.2015 )";
		//String s = "(contains 123 (geom, box( (0,0), ... ), less ( time , 1.1.2015 )";
		//String s = "(contains  (uuu 123 789) 456) ";
		//String s = "(contains (f 456) 789 888) ";

		//String s = "(contains 123 (f 456 789) (f2 999) 1000 1001)";
		// String s = "(and (equals instrument 'SEABIRD SBE37SM + P') (equals instrument 'SEABIRD SBE37SM + P'))";

		// String s = "(equals instrument 2012-01-01T00:00:00Z)";

		//	select *  from anmn_ts.measurement where "TIME" = 2013-03-24T21:35:01Z limit 2; 

		String s = "(and (gt TIME 2013-6-28T21:35:01Z ) (equals ts_id 6341))"; 

		// select *  from anmn_ts.measurement where "TIME" > '2013-6-28T21:35:01Z' and ts_id = 6341 ;

		Parser c = new Parser();
		IExpression expr = c.parseExpression( s, 0);

		if( expr == null) {
			throw new RuntimeException( "failed to parse expression" );
		}

		System.out.println( "got an expression" );


		// Should make it Postgres specific ?...
		StringBuilder b = new StringBuilder();
		SelectionGenerationVisitor v = new SelectionGenerationVisitor( b);
		expr.accept(v);

		System.out.println( "expression is " + b.toString() );



		Connection conn = getConn();

		Test3 t = new Test3( conn );

		String query = "SELECT * FROM anmn_ts.measurement where " + b.toString() + " limit 2";
		// String query = "SELECT * FROM anmn_ts.timeseries where " + b.toString();

		System.out.println( "query " + query  );

		t.doQuery2( query  );

	}
}



