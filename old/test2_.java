
// rm *.class ; javac main.java  ; java main

// time javac test2.java -cp .:netcdfAll-4.2.jar
// time java -cp .:postgresql-9.1-901.jdbc4.jar:netcdfAll-4.2.jar  test2 

//import java.io.BufferedReader;
import java.io.IOException;

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
}


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


class EncoderD1 implements IEncoderD1
{
	// IMPORTANT - we could encode the variable name as an attribute and avoid having to pass it.

	public EncoderD1( NetcdfFileWriteable writer, String variableName, ArrayList<Dimension> dims, Map<String, Object> attributes, IEncodeValue encodeValue )
	{
		this.writer = writer;
		this.variableName = variableName; 
		this.dims = dims;
		this.attributes = attributes;
		this.encodeValue = encodeValue; // new EncodeFloatValue();
		this.A = null; 
		if( dims.size() != 1 ) {
			throw new RuntimeException( "Expected only 1 dimension" );
		}

	}

	final NetcdfFileWriteable writer; 
	final String variableName; 
	final ArrayList<Dimension> dims;
	final Map<String, Object> attributes; 
	final IEncodeValue encodeValue;

	Array	 A;

	public void define()
	{
		writer.addVariable(variableName, DataType.FLOAT, dims);
		for( Map.Entry< String, Object> entry : attributes.entrySet()) { 
			writer.addVariableAttribute( variableName, entry.getKey(), entry.getValue().toString() ); 
		}

		// this is typed on the value type and the dimension. which makes it very hard to pull apart.  
		// but
		if( encodeValue.targetType() == Float.class )
		{
			this.A = new ArrayFloat.D1( dims.get(0).getLength() );
		}
		else if ( encodeValue.targetType() == Byte.class )
		{
			this.A = new ArrayByte.D1( dims.get(0).getLength() );
		}
		else {
			throw new RuntimeException( "Unrecognized encoder type" );
		}
	}

	public void addValue( int a, Object value )
	{
		Index ima = A.getIndex();
		ima.set(a); 
		encodeValue.encode( A, ima, attributes, value ); 
	}

	public void finish() throws Exception
	{
		int [] origin = new int[1];
		writer.write(variableName, origin, A);

		// and write the attribute values ...
	}
}


class EncoderD3 implements IEncoderD3
{
	// abstraction that handles both data for type float, and definining the parameters 
	// try to keep details about the dimensions out of this, and instead just encode the dimension lengths.

	// do we want it to 

	// do we define the netcdf???
	public EncoderD3( NetcdfFileWriteable writer, String variableName, ArrayList<Dimension> dims, Map<String, Object> attributes, IEncodeValue encodeValue )
	{
		this.writer = writer;
		this.variableName = variableName; 
		this.dims = dims;
		this.attributes = attributes;
		this.encodeValue = encodeValue; 

		this.A = null; 
		if( dims.size() != 3 ) {
			throw new RuntimeException( "Expected only 1 dimension" );
		}
	}

	// column name should only be in the mapper. 
	final NetcdfFileWriteable writer; 
	final String variableName; 
	final ArrayList<Dimension> dims;
	final Map<String, Object> attributes; 
	final IEncodeValue encodeValue;

	//ArrayFloat.D3 A;
	Array	 A;

	public void define()
	{
		// assumes writer is in define mode
		writer.addVariable(variableName, DataType.FLOAT, dims);
		for( Map.Entry< String, Object> entry : attributes.entrySet()) { 
			writer.addVariableAttribute( variableName, entry.getKey(), entry.getValue().toString() ); 
		}
	// 	this.A = new ArrayFloat.D3( dims.get(0).getLength(), dims.get(1).getLength(), dims.get(2).getLength());
		// this is typed on the value type and the dimension. which makes it very hard to pull apart.  
		// but
		if( encodeValue.targetType() == Float.class )
		{
			this.A = new ArrayFloat.D3( dims.get(0).getLength(), dims.get(1).getLength(), dims.get(2).getLength());
		}
		else if ( encodeValue.targetType() == Byte.class )
		{
			this.A = new ArrayByte.D3( dims.get(0).getLength(), dims.get(1).getLength(), dims.get(2).getLength());
		}
		else {
			throw new RuntimeException( "Unrecognized encoder type" );
		}

	}
	public void addValue( int a, int b, int c, Object value )
	{
		Index ima = A.getIndex();
		ima.set(a, b, c); 
		encodeValue.encode( A, ima, attributes, value ); 
	}

	public void finish() throws Exception
	{
		// assumes writer is in data mode
		int [] origin = new int[3];
		writer.write(variableName, origin, A);
	}
}






interface IEncodingStrategy
{
	// it's both a decode and encode strategy . 

	public IEncoder getEncoder( String variableName, Class variableType ); 
}


class ConventionEncodingStrategy implements IEncodingStrategy
{
	// Convention over configuration, will delegate for configuration...

	public ConventionEncodingStrategy( NetcdfFileWriteable writer, ArrayList<Dimension> dims ) 
	{ 
		this.writer = writer;
		this.dims = dims;
		// we need to pass both the writer and dims.... 
		// which is p
		// well we can abstract the instantiation of this thing ...
	} 

	final NetcdfFileWriteable writer ; 
	final ArrayList<Dimension> dims; 

	// we might need to return a list, if a single column type can map 
	// to multiple variables - eg. the_geometry and LATITUTDE AND LONGTITUDE...
	public IEncoder getEncoder( String columnName, Class columnType )
	{
		// class is an attempt to try to guess heuristically, how to encode. 
		// we can populate. 

		// delegate to configuration...
		// otherwise apply convention rules
		// it's the db column name and type
		// System.out.println ( "name '" + columnName + "'  type '" + columnType + "'"  );

		// think that we need to fill this in with SAX...
		// then we query for what we want.

		IEncoder encoder = null; 

		if( columnName.equals("TIME"))
		{
			// in main table, but is a dimension.

			System.out.println ( "WHOOT ** got time " );

			// should lookup the dimension by name
			// we've set it to take all the dimensions
			ArrayList<Dimension> d = new ArrayList<Dimension>();
			d.add( dims.get( 0) );

			Map<String, Object> attributes = new HashMap<String, Object>();
			attributes.put( "units", "days since 1950-01-01 00:00:00 UTC" );
			attributes.put( "_FillValue", (float) 999999. ); 


//			encoder = new EncoderTimestampD1( writer, columnName, d , attributes);


			IEncodeValue encodeValue = new EncodeTimestampValue(); 

			encoder = new EncoderD1( writer, columnName, d, attributes, encodeValue);
		}

		// put attributes in the encoder?  no

		else if( columnName.equals("LATITUDE"))
		{
			// need to look these up, by name
			ArrayList<Dimension> d = new ArrayList<Dimension>();
			d.add( dims.get( 1) );

			Map<String, Object> attributes = new HashMap<String, Object>();
			attributes.put( "_FillValue", (float) 999999. ); 

			IEncodeValue encodeValue = new EncodeFloatValue();

			encoder = new EncoderD1( writer, columnName, d, attributes, encodeValue);
		}
		else if( columnName.equals("LONGITUDE"))
		{
			ArrayList<Dimension> d = new ArrayList<Dimension>();
			d.add( dims.get( 2) );

			Map<String, Object> attributes = new HashMap<String, Object>();
			attributes.put( "_FillValue", (float) 999999. ); 

			IEncodeValue encodeValue = new EncodeFloatValue();

			encoder = new EncoderD1( writer, columnName, d, attributes, encodeValue);

		}
		else if( columnName.equals("LATITUDE_quality_control"))
		{
			ArrayList<Dimension> d = new ArrayList<Dimension>();
			d.add( dims.get( 1) );
	
			Map<String, Object> attributes = new HashMap<String, Object>();
			attributes.put( "_FillValue", (byte) 0xff ); 

	
			IEncodeValue encodeValue = new EncodeByteValue();
			encoder = new EncoderD1( writer, columnName, d, attributes, encodeValue);

		//	encoder = new encoderByteD1( writer, columnName, d, attributes ); //(byte)0xff );
		}
		else if( columnName.equals("LONGITUDE_quality_control"))
		{
			ArrayList<Dimension> d = new ArrayList<Dimension>();
			d.add( dims.get( 2) );

			Map<String, Object> attributes = new HashMap<String, Object>();
			attributes.put( "_FillValue", (byte) 0xff ); 

			IEncodeValue encodeValue = new EncodeByteValue();

			encoder = new EncoderD1( writer, columnName, d, attributes, encodeValue);

			// encoder = new EncoderByteD1( writer, columnName, d, attributes );
		}



		else if( Pattern.compile(".*quality_control$" ).matcher( columnName) .matches()) 
		{
			Map<String, Object> attributes = new HashMap<String, Object>();
			attributes.put( "_FillValue", (byte) 0xff ); 
	
			IEncodeValue encodeValue = new EncodeByteValue();
			encoder = new EncoderD3( writer, columnName, dims , attributes, encodeValue );

		}
		else if( Pattern.compile("^[A-Z]+.*" ).matcher( columnName).matches()) 
		{
			Map<String, Object> attributes = new HashMap<String, Object>();
			attributes.put( "_FillValue", (float) 999999. ); 

			System.out.println( "normal var - " + columnName );
			if( columnType.equals(Float.class)
				||	columnType.equals(Double.class)
			) {

				IEncodeValue encodeValue = new EncodeFloatValue();
				encoder = new EncoderD3( writer, columnName, dims , attributes, encodeValue );
			}
			// other...
		}
/*
		if ( encoder == null ){
			encoder = new EncoderIgnore(); 
		}
*/
		return encoder;
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


	public void doDefinitionStuff( 
		Connection conn,
	
		String table,	

		Map<String, IEncoder> encoders, 
		Map<String, IEncoderD3> encodersD3, 
		Map<String, IEncoderD1> encodersD1 ,

		IEncodingStrategy encodingStrategy  
		 ) throws Exception
	{
		String query = "SELECT * FROM " + table + " limit 0"; 
		PreparedStatement stmt = conn.prepareStatement( query ); 
		ResultSet rs = stmt.executeQuery();

		ResultSetMetaData m = rs.getMetaData();
		int numColumns = m.getColumnCount();

		// Establish conversions according to convention
		for ( int i = 1 ; i <= numColumns ; i++ ) {
			String columnName = m.getColumnName(i); 
			// issue is that we're going to be calling it multiple times over????
			Class clazz = Class.forName(m.getColumnClassName(i));
			IEncoder encoder = encodingStrategy.getEncoder( columnName, clazz ); 
			if( encoder != null ) { 
				encoders.put( columnName, encoder );

				if( encoder instanceof IEncoderD3)
					encodersD3.put( columnName, (IEncoderD3) encoder ); 

				else if ( encoder instanceof IEncoderD1)
					encodersD1.put( columnName, (IEncoderD1)encoder ); 

				else {
					throw new RuntimeException( "Unknown Encoder type for column '" + columnName + "'" );
				}				
			}
		}

		rs.close();
	}


	public NetcdfFileWriteable get() throws Exception
	{
		// code organized so we only iterate over the recordset returned by the query once

		featureInstances.next();

		long ts_id = (long) featureInstances.getObject(1); 

		System.out.println( "whoot get(), ts_id is " + ts_id );

		String selection = translate.process( selection_expr); // we ought to be caching the specific query ??? 
					
		// doing independent queries for COUNT, then values, 
		// should be faster than creating a scrollable cursor, iterating and rewinding.
		// we can query the count independently of querying all the values...
		
		// we're going to have to close all this stuff,
		int count = 0;
		{
			// We know how many values we are going to deal with, so let's encode rather than use an unlimited dimension 
			String query = "SELECT count(1) FROM anmn_ts.measurement where " + selection +  " and ts_id = " + Long.toString( ts_id); 
			PreparedStatement stmt = conn.prepareStatement( query ); 
			// stmt.setFetchSize(1000);
			ResultSet rs = stmt.executeQuery();
			rs.next(); 
			count = (int)(long) rs.getObject(1); 
		}

		/*
			need a strategy as to where to create these files
		*/

		NetcdfFileWriteable writer = createWritable.create();

		// we have to encode these values as well.

		// All, time series need this to be explicitly setup...
		// add dimensions
		Dimension timeDim = writer.addDimension("TIME", count); // writer.addUnlimitedDimension("time");
		Dimension latDim = writer.addDimension("LATITUDE", 1);
		Dimension lonDim = writer.addDimension("LONGITUDE", 1);

		// time unlimited ...  // need time, // define Variable
		ArrayList<Dimension> dims = new ArrayList<Dimension>();
		dims.add(timeDim);
		dims.add(latDim);
		dims.add(lonDim);

		// this needs to be abstracted ...

		// we shouldn't be instantiating this thing here...
		IEncodingStrategy encodingStrategy = new ConventionEncodingStrategy( writer, dims ); 

		Map<String, IEncoder> encoders = new HashMap<String, IEncoder>();
		Map<String, IEncoderD3> encodersD3 = new HashMap<String, IEncoderD3>();
		Map<String, IEncoderD1> encodersD1 = new HashMap<String, IEncoderD1>();


		doDefinitionStuff( conn, "anmn_ts.timeseries", encoders, encodersD3, encodersD1 , encodingStrategy  ); 
		doDefinitionStuff( conn, "anmn_ts.measurement", encoders, encodersD3, encodersD1 , encodingStrategy  ); 
	

		// define
		for ( IEncoder encoder: encoders.values()) {
			encoder.define();
		}

		// finish netcdf definition
		writer.create();


		// measurement data	
		if( true )
		{
			// sql stuff
			// need to encode the additional parameter...
			String query = "SELECT * FROM anmn_ts.measurement where " + selection +  " and ts_id = " + Long.toString( ts_id) + " order by \"TIME\" "; 
			PreparedStatement stmt = conn.prepareStatement( query ); 
			stmt.setFetchSize(1000);
			ResultSet rs = stmt.executeQuery();

			// now we loop the main attributes 
			ResultSetMetaData m = rs.getMetaData();
			int numColumns = m.getColumnCount();
			// encode values t,lat,lon are always indexes - so we should be able to delegate to the thing...
			int time = 0;
			while ( rs.next() ) {  
				for( int lat = 0; lat < latDim.getLength(); ++lat )
				for( int lon = 0; lon < lonDim.getLength(); ++lon ) {
					// 3d values
					for ( int i = 1 ; i <= numColumns ; i++ ) {
						IEncoderD3 encoder = encodersD3.get(m.getColumnName(i)); 
						if( encoder != null) 
							encoder.addValue( time, lat, lon, rs.getObject( i));
					}
				}

				// 1d values
				for ( int i = 1 ; i <= numColumns ; i++ ) {
					IEncoderD1 encoder = encodersD1.get(m.getColumnName(i)); 
					if( encoder != null) 
						encoder.addValue( time, rs.getObject( i));
				}
				++time;
			}
		}

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




		// close
		writer.close();
	
		return writer; 
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



