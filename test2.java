
// rm *.class ; javac main.java  ; java main

// time javac test2.java -cp .:netcdfAll-4.2.jar
// time java -cp .:postgresql-9.1-901.jdbc4.jar:netcdfAll-4.2.jar  test2 

//import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream ;
import java.io.IOException;

import java.io.FileInputStream;
import java.io.BufferedInputStream;

import java.util.ArrayList; //io.BufferedInputStream;
//import java.util.ArrayDouble; //io.BufferedInputStream;

import java.util.HashMap; //io.BufferedInputStream;
import java.util.Map; //io.BufferedInputStream;


import java.sql.*;

import java.util.Properties;
import java.lang.RuntimeException;

import java.text.SimpleDateFormat;
/*import java.util.List;
import java.util.Set;
import java.util.ArrayList;
*/
//import java.util.Date;

import ucar.nc2.NetcdfFileWriteable; 
import ucar.nc2.Dimension; 
//import ucar.nc2.DataType.DOUBLE; 
//import ucar.nc2.*; 

import ucar.ma2.DataType; 
import ucar.ma2.Array;

import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayByte;
//import ucar.ma2.ArrayString;

import ucar.ma2.Index;



import java.util.regex.Pattern ; 
import java.util.regex.Matcher; 


//import java.util.StringTokenizer;

// string tokenizer isn't going to work because may not be strings.

// do everything twice?
// or leave the value there...


interface IVisitor
{
	public void visit( ExprInteger expr );
	public void visit( ExprProc expr );
	public void visit( ExprLiteral expr );
	public void visit( ExprSymbol expr );
	public void visit( ExprTimestamp expr );
}

// change name to AST . 
// can use 

interface IExpression
{
	public int getPosition() ;
	public void accept( IVisitor v ) ;
}

class ExprSymbol implements IExpression
{
	public ExprSymbol( int pos, String value)
	{
		this.pos = pos;
		this.value = value;
	}

	public int getPosition() { return pos; }
	public void accept( IVisitor v )  { v.visit( this); }

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
	public void accept( IVisitor v )  { v.visit( this); }

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
	public void accept( IVisitor v )  { v.visit( this); }

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
	public void accept( IVisitor v )  { v.visit( this); }
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
	public void visit( IVisitor v )  { }
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
	public void accept( IVisitor v )  { v.visit( this); }

	final int		pos;
	final String symbol;
	final ArrayList<IExpression> children;
}




// actually why not pass a
// string s, integer pos, boxed type....

// change name exprParser

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

	// should pass this as a dependency
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



class PrettyPrinterVisitor implements IVisitor
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



class PostgresGenerationVisitor implements IVisitor
{
	// rename PGDialectSelectionGenerator

	// we don't know the bloody index offset of the parameter.
	// we might have

	// should take the stream on the constructor
	// actually just a string builder...

	StringBuilder b;
	SimpleDateFormat df; 

	// parameters...

	// ok, 

	public PostgresGenerationVisitor( StringBuilder b )
	{
		this.b = b;
		this.df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
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
		else if(symbol.equals("lt")) {
			emitInfixSqlExpr( "<", expr );
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



interface IDialectTranslate 
{
	public String process( IExpression expr ) ;
}


class PostgresDialectTranslate implements IDialectTranslate 
{
	// we have to have something to instantiate the specific visitor
	public PostgresDialectTranslate( ) 
	{
		; // this.visitor = visitor; 
	}


	public String process( IExpression expr ) 
	{
		// Should make it Postgres specific ?...
		StringBuilder b = new StringBuilder();
		PostgresGenerationVisitor visitor = new PostgresGenerationVisitor( b);
		expr.accept( visitor );

		// System.out.println( "expression is " + b.toString() );
		return b.toString();
	}
}


// we need raw identifiers which is the way cql does it.





/*
	jdbc3 doesn't work for non-validating factory
	export CLASSPATH=.:postgresql-9.1-901.jdbc4.jar
	java test3
*/
	/*
		collaborators
			- 
			- selector (filter) subsetter
			- conn
			j- limit
			- netcdf encoder, file... 
			- streaming output / whatever output control we need (should just push encoded netcdf to outputer ) 
	*/
	/*
		two methods
			- init()      initial query
			- nextFile()  encode file by file.
		but we'll need to be able to get another connectionjjjjjjjjjjjjjjjjj
	*/



class Timeseries2
{
	Parser parser;				
	IDialectTranslate translate ;		
	Connection conn;

	public Timeseries2( Parser parser, IDialectTranslate translate, Connection conn ) {
		this.parser = parser;
		this.translate = translate; // sqlEncode.. dialect... specialization
		this.conn = conn;
	}

	public void run() throws Exception
	{
		String s = " (and (gt TIME 2013-6-28T00:35:01Z ) (lt TIME 2013-6-29T00:40:01Z )) "; 
		IExpression expr = parser.parseExpression( s, 0);
		if(expr == null) {
			throw new RuntimeException( "failed to parse expression" );
		}
		String selection = translate.process( expr );
		String query = "SELECT * FROM anmn_ts.measurement m join anmn_ts.timeseries ts on m.ts_id = ts.id where " + selection + " order by ts_id, \"TIME\""; 
		System.out.println( "first query " + query  );

		PreparedStatement stmt = conn.prepareStatement( query );
		stmt.setFetchSize(1000);
		ResultSet rs = 	stmt.executeQuery();

		System.out.println( "got some data " );
		int count = 0;
		while ( rs.next() ) {  
			++count;
		}
		System.out.println( "count " + count );
	}
}


class Timeseries3
{
	// ok, we need to start encoding this as a netcdf ...
	// to be streaming this will generate a netcdf with a pull model....

	Parser parser;				
	IDialectTranslate translate ;		
	Connection conn;

	public Timeseries3( Parser parser, IDialectTranslate translate, Connection conn ) {
		this.parser = parser;
		this.translate = translate; // sqlEncode.. dialect... specialization
		this.conn = conn;
	}

	public void run() throws Exception
	{
		String filter = " (and (gt TIME 2013-6-28T00:35:01Z ) (lt TIME 2013-6-29T00:40:01Z )) "; 
		IExpression expr = parser.parseExpression( filter, 0);
		if(expr == null) {
			throw new RuntimeException( "failed to parse expression" );
		}
		// join anmn_ts.timeseries ts on m.ts_id = ts.id 

		// we should really take the easiest case and just start encoding ...

		String selection = translate.process( expr );
		String query = "SELECT * FROM anmn_ts.measurement m where " + selection + " order by ts_id, \"TIME\""; 
		System.out.println( "first query " + query  );

		PreparedStatement stmt = conn.prepareStatement( query );
		stmt.setFetchSize(1000);
		ResultSet rs = 	stmt.executeQuery();

		System.out.println( "got some data " );
		int count = 0;
		while ( rs.next() ) {  
			++count;
		}
		System.out.println( "count " + count );

	}
}





class MyType
{
	// fill in with some default values, then over-ride with explicit configuration

	// rather than doing clazz testing. 
	// could we use a visitor for this stuff

	// should be stored in a map...

	// schema name, table name
	public MyType(String columnName,  Class  targetType, Object fillValue )
	{
		this.columnName = columnName; 
		this.targetType = targetType; 
		this.fillValue = fillValue; 
	}


	public final String columnName; 

	public final Class  targetType; // to encode in FLOAT actually as Java.
	// rather than using the sql type
	// Object	data;

	public final Object fillValue;
}




interface IEncoder
{
	// change name VarEncoder

	// name is provided by map lookup
	// theoretically this object could also preserve the index
	// if we give it a concept of the name, then it can also define the netcdf.

	public void define();  // change name to start(); ?
	public void finish( ) throws Exception ; 


	public void addValueToBuffer( Object value ); 

	public String getVariableName();

	public void dump();
}

/*
interface IEncoderD3 extends IEncoder
{
	// change name VarEncoder

	// name is provided by map lookup
	// theoretically this object could also preserve the index
	// if we give it a concept of the name, then it can also define the netcdf.

	public void addValue( int a, int b, int c, Object o ) ; 
}


interface IEncoderD1 extends IEncoder
{
	public void addValue( int a, Object object );  // over 1 dimension 
}
*/


// do we have different types of encoder based on the number of dimensions it handles
// or do we have different methods 

// IMPORTANT - I THINK THIS IS CORRECT...
// And we'll need to shuffle them into the right type
// although we want t
// 3d encoder, 1d encoder, scalar encoder
// with different types, we can do the calling at different times. 

// so specialize the encoder interface... for Encoder1d, Encoder3d, EncoderScalar 


// And very important 
// we can do the same thing on the timeseries table....




interface IEncodeValue
{

	public void encode( Array A, Index ima, Map<String, Object> attributes, Object value ); 

	public Class targetType(); 
}



class EncodeTimestampValue implements IEncodeValue
{
	public Class targetType()
	{
		return Float.class;
	}

	public void encode( Array A, Index ima, Map<String, Object> attributes, Object value )
	{
		// this needs to be changes
		if( attributes.get("units").equals( "days since 1950-01-01 00:00:00 UTC" ))
		{
			if( value == null) {
				A.setFloat( ima, (float) attributes.get( "_FillValue" ));
			}
			else if( value instanceof java.sql.Timestamp ) {
				A.setFloat( ima, (float) 0. );
			} 
			else {
				throw new RuntimeException( "Not a timestamp" );
			}
		}
		else {
			// only limited case
			throw new RuntimeException( "Bad date unit" );
		}
	}
}



class EncodeFloatValue implements IEncodeValue
{
	// change name to targetType 
	public Class targetType()
	{
		return Float.class;
	}

	public void encode( Array A, Index ima, Map<String, Object> attributes, Object value )
	{
		if( value == null) {
			A.setFloat( ima, (float) attributes.get( "_FillValue" ));
		}
		else if( value instanceof Float ) {
			A.setFloat( ima, (float) value);
		} 
		else if( value instanceof Double ) {
			A.setFloat( ima, (float)(double) value);
		} 
		else {
			throw new RuntimeException( "Failed to coerce type to float" );
		}
	}
}


class EncodeByteValue implements IEncodeValue
{
	// value encoder
	// abstract concept of dimension...

	// assumption that the Object A is a float array
	public Class targetType()
	{
		return Byte.class;
	}

	public void encode( Array A, Index ima, Map<String, Object> attributes, Object value )
	{
		// ArrayByte A = (ArrayByte) A_;
		// Array A = (Array) A_;

		if( value == null) {
			A.setByte( ima, (byte) attributes.get( "_FillValue" ));
		}
		else if(value instanceof Byte)
		{
			A.setByte( ima, (byte) value);
		}
		else if(value instanceof String && ((String)value).length() == 1) {
			// coerce string of length 1 to byte
			String s = (String) value; 
			Byte ch = s.getBytes()[0];
			A.setByte(ima, ch);
		} 
		else {
			throw new RuntimeException( "Failed to convert type to byte");
		}
	}
}

/*
	The final netcdf document is actully a combination of everything 
*/

class MyEncoder implements IEncoder
{
	public MyEncoder( String variableName, ArrayList< IEncoder>  children )
	{
		this.variableName = variableName; 
		this.children = children; 
		
		this.buffer = new ArrayList<Object>( );	
	}

	final String variableName; 
	final ArrayList<Object>		buffer;
	final ArrayList<IEncoder>	children;

	/*
		we can also record the table, or index of table here if we want
			to incorporate into the strategy.
		eg. we can compre with xml to decide what to do.
	*/
	public void addValueToBuffer( Object value )
	{
		// perhaps delegate to strategy...
		buffer.add( value );
	}

	public void define() { }
	public void finish( ) throws Exception { }

	public void dump()
	{ 
		System.out.println( "WHOOT ENCODEER - " + variableName + " buffer size " + buffer.size() );
	}


	public String getVariableName()
	{
		return variableName;
	}
}



interface ICreateWritable 
{
	public NetcdfFileWriteable create( )  throws IOException ; 
}


class CreateWritable implements  ICreateWritable
{
	// NetcdfFileWriteable is not an abstraction over a stream!. instead it insists on being a file...

	public NetcdfFileWriteable create() throws IOException 
	{
		System.out.println( "creating writer" );
		// netcdf stuff
		String filename = "testWrite.nc";

		return NetcdfFileWriteable.createNew(filename, false);
	}

	// method to request as a byte stream and return?
	// public getByteStream () { } 
}





class Timeseries1
{
	Parser parser;				// change name to expressionParser or SelectionParser
	IDialectTranslate translate ;		// will also load up the parameters?
	Connection conn;

	ICreateWritable createWritable; // generate a writiable 
	// Encoder
	// Order criterion (actually a projection bit) 

	//  state required for streaming..
	// should we cache the result of the translation or the translator??
	IExpression selection_expr;
	// id's of feature instances we will need
	ResultSet featureInstances;


	public Timeseries1( Parser parser, IDialectTranslate translate, Connection conn, ICreateWritable createWritable ) {
		// we need to inject the selector ...
		// 
		this.parser = parser;
		this.translate = translate; // sqlEncode.. dialect... specialization
		this.conn = conn;
		this.createWritable = createWritable;
	
		featureInstances = null;
		selection_expr = null;
	}

	// init, get, close



	public void init() throws Exception
	{
		// avoiding ordering clauses that will prevent immediate stream response


		// set up the featureInstances that we will need to process 
		String s = " (and (gt TIME 2013-6-28T00:35:01Z ) (lt TIME 2013-6-29T00:40:01Z )) "; 

		selection_expr = parser.parseExpression( s, 0);
		// bad, should return expr or throw
		if(selection_expr == null) {
			throw new RuntimeException( "failed to parse expression" );
		}
		String selection = translate.process( selection_expr);
		String query = "SELECT distinct ts_id  FROM anmn_ts.measurement where " + selection ; 
		System.out.println( "first query " + query  );

		PreparedStatement stmt = conn.prepareStatement( query );
		stmt.setFetchSize(1000);

		// try ...
		// change name featureInstancesToProcess ?
		featureInstances = stmt.executeQuery();

		System.out.println( "done determining feature instances " );

		// should determine our target types here
	}


	// we need the query or the selection to be exposed, so we can formulate
	// like a fold, with an init and transform
	// we should definitely pass a writable here ...
	// rather than instantiate it

	public void populateValues(  Map< String, IEncoder> encoders, String query  )  throws Exception
	{
		// sql stuff
		// need to encode the additional parameter...
		//String query = "SELECT * FROM anmn_ts.measurement where " + selection +  " and ts_id = " + Long.toString( ts_id) + " order by \"TIME\" "; 
		// String query = "SELECT * FROM anmn_ts.timeseries where id = " + Long.toString( ts_id); 
		PreparedStatement stmt = conn.prepareStatement( query ); 
		stmt.setFetchSize(1000);
		ResultSet rs = stmt.executeQuery();

		// now we loop the main attributes 
		ResultSetMetaData m = rs.getMetaData();
		int numColumns = m.getColumnCount();
		// encode values t,lat,lon are always indexes - so we should be able to delegate to the thing...
		int time = 0;
		while ( rs.next() ) {  
			for ( int i = 1 ; i <= numColumns ; i++ ) {
				// System.out.println( "column name " + m.getColumnName(i) );

				IEncoder encoder = encoders.get( m.getColumnName(i)); 
				if( encoder != null) 
					encoder.addValueToBuffer( rs.getObject( i));
			}
		}
	} 


	public NetcdfFileWriteable get() throws Exception
	{
		// code organized so we only iterate over the recordset returned by the query once

		featureInstances.next();

		long ts_id = (long) featureInstances.getObject(1); 

		System.out.println( "whoot get(), ts_id is " + ts_id );

		String selection = translate.process( selection_expr); // we ought to be caching the specific query ??? 
					
		// we'll add them all to a List 
//		IEncoder e = new MyEncoder ( "LATITUDE", null ) ; 

		Map< String, IEncoder> encoders = new HashMap< String, IEncoder> ();


		IEncoder lat = new MyEncoder ( "LATITUDE", null ); 
		IEncoder time = new MyEncoder ( "TIME", null ) ; 

		encoders.put( lat.getVariableName(), lat ) ; 
		encoders.put( time.getVariableName(), time ) ; 

		/*
			For timeseries - we may only need a 

		*/

		populateValues( encoders, "SELECT * FROM anmn_ts.timeseries where id = " + Long.toString( ts_id) );
		populateValues( encoders, "SELECT * FROM anmn_ts.measurement where " + selection +  " and ts_id = " + Long.toString( ts_id) + " order by \"TIME\" "  );

		/* IMPORTANT Issue - ordering criteria...
		*/

		for ( IEncoder encoder: encoders.values())
		{

			encoder.dump ();
		}



/*
		// timeseries data	
		{
			// sql stuff
			// need to encode the additional parameter...
			String query = "SELECT * FROM anmn_ts.timeseries where id = " + Long.toString( ts_id); 
			PreparedStatement stmt = conn.prepareStatement( query ); 
			stmt.setFetchSize(1000);
			ResultSet rs = stmt.executeQuery();

			// now we loop the main attributes 
			ResultSetMetaData m = rs.getMetaData();
			int numColumns = m.getColumnCount();

			rs.next(); 
			// 1d values
			for ( int i = 1 ; i <= numColumns ; i++ ) {
				IEncoderD1 encoder = encodersD1.get(m.getColumnName(i)); 
				if( encoder != null ) 
					encoder.addValue(0, rs.getObject(i));
			}
		}

		// write values to netcdf
		for ( IEncoder encoder: encoders.values()) {
			encoder.finish();
		}

		System.out.println( "done writing data" );

*/

/*
		// close
		writer.close();
	
		return writer; 
*/
		return null;
	}


}









public class test2 {

	// if we're going to need more than one connection. No it's actually not clear that this is the case. 
	// we can handle multiple record sets on the same connection.
	// we need to expose the actual Driver manager.

    public static Connection getConn() throws Exception
	{
		//String url = "jdbc:postgresql://127.0.0.1/postgres";

		// psql -h test-geoserver -U meteo -d harvest

		String url = "jdbc:postgresql://test-geoserver/harvest";
		Properties props = new Properties();
		props.setProperty("user","meteo");
		props.setProperty("password","meteo");
/*
		String url = "jdbc:postgresql://dbprod.emii.org.au/harvest";
		Properties props = new Properties();
		props.setProperty("user","jfca");
		props.setProperty("password","fredfred");
*/

		props.setProperty("ssl","true");
		props.setProperty("sslfactory","org.postgresql.ssl.NonValidatingFactory");
		props.setProperty("driver","org.postgresql.Driver" );

		Connection conn = DriverManager.getConnection(url, props);
		/*if(conn == null) {
			throw new RuntimeException( "Could not get connection" );
		}*/
		return conn;
	}


    public static void main(String[] args) throws Exception
	{
		Parser parser = new Parser();

		IDialectTranslate translate = new  PostgresDialectTranslate();

		Connection conn = getConn();

		ICreateWritable createWritable = new CreateWritable();  
	
		Timeseries1 timeseries = new Timeseries1( parser, translate, conn, createWritable ); 
		// Timeseries timeseries = new Timeseries( parser, translate, conn ); 

		timeseries.init();	

		NetcdfFileWriteable writer = null;
		do {  
			writer = timeseries.get();	
		}
		while( writer != null );

	}

		//String s = "777 and ( contains(geom, box( (0,0), ... ), less ( time , 1.1.2015 )";
		//String s = "(contains 123 (geom, box( (0,0), ... ), less ( time , 1.1.2015 )";
		//String s = "(contains  (uuu 123 789) 456) ";
		//String s = "(contains (f 456) 789 888) ";
		//String s = "(contains 123 (f 456 789) (f2 999) 1000 1001)";
		// String s = "(and (equals instrument 'SEABIRD SBE37SM + P') (equals instrument 'SEABIRD SBE37SM + P'))";
		// String s = "(equals instrument 2012-01-01T00:00:00Z)";
		//	select *  from anmn_ts.measurement where "TIME" = 2013-03-24T21:35:01Z limit 2; 
//		String s = "(and (and (gt TIME 2013-6-28T00:35:01Z ) (lt TIME 2013-6-28T01:35:01Z )) (equals ts_id 6341))"; 
//		String s = "(equals ts_id 6341)"; 
		// select *  from anmn_ts.measurement where "TIME" > '2013-6-28T21:35:01Z' and ts_id = 6341 ;

}



