
// rm *.class ; javac main.java  ; java main

// time javac test2.java -cp .:netcdfAll-4.2.jar
// time java -cp .:postgresql-9.1-901.jdbc4.jar:netcdfAll-4.2.jar  test2


package au.org.emii.ncfgenerator;

//import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream ;
import java.io.IOException;

import java.io.FileInputStream;
import java.io.BufferedInputStream;

import java.util.ArrayList; //io.BufferedInputStream;
import java.util.List; //io.BufferedInputStream;
//import java.util.ArrayDouble; //io.BufferedInputStream;

import java.util.HashMap; //io.BufferedInputStream;
import java.util.Map; //io.BufferedInputStream;

import java.util.Date; 

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

import java.util.Arrays;

import java.util.regex.Pattern ;
import java.util.regex.Matcher;


import java.util.Iterator;

/*
import au.org.emii.geoserver.extensions.filters.layer.data.FilterConfiguration;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.xml.sax.SAXException;
*/

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
// import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import java.util.AbstractMap.SimpleImmutableEntry;
// java.util.AbstractMap.SimpleImmutableEntry<K,V>;



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


interface IExprParser
{
	public IExpression parseExpression(String s, int pos); 
	

}


class ExprParser implements IExprParser
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


	public ExprParser() {
			// TODO don't generate this every time...
		df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	}

	// should pass this as a dependency
	final SimpleDateFormat df;

	public IExpression parseExpression(String s, int pos)
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

	private ExprProc parseProc(String s, int pos)
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

	private ExprSymbol parseSymbol( String s, int pos)
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


	private ExprTimestamp parseTimestamp( String s, int pos )
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

	private ExprInteger parseInt( String s, int pos )
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

	private ExprLiteral parseLiteral( String s, int pos )
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


/*
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


*/


/*

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
*/



/*
interface IVariableEncoderD3 extends IVariableEncoder
{
	// change name VarEncoder

	// name is provided by map lookup
	// theoretically this object could also preserve the index
	// if we give it a concept of the name, then it can also define the netcdf.

	public void addValue( int a, int b, int c, Object o ) ;
}


interface IVariableEncoderD1 extends IVariableEncoder
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




interface IValueEncoder
{
	// Change name to ValueEncoder Timestamp

	public void encode( Array A, int ima, Object value );

	public void prepare( Map<String, String> attributes ); 

	//public Class targetType();
	public DataType targetType();
}


// change name init() to prepare() 


class TimestampValueEncoder implements IValueEncoder
{

	TimestampValueEncoder()
	{
		// all the date attribute parsing slows the code a lot so calculate once at init . 
		this.epoch = 0;
		this.unit = null;
		this.fill = 1234;
	}

	// need a date unit...
	long epoch;  // in seconds 
	String unit; // seconds, days  
	float fill;

	public DataType targetType()
	{
		return DataType.FLOAT;//.class;
	}

	public void prepare(  Map<String, String> attributes ) 
	{ 
		// System.out.println( "****************** units " + attributes.get("units") ); 

		Matcher m = Pattern.compile("([a-zA-Z]*)[ ]*since[ ]*(.*)").matcher( attributes.get("units") );
		if(!m.find())
		{
			throw new RuntimeException( "couldn't parse attribute date");
		}
		unit = m.group(1);
		String epochString = m.group(2);
		try { 
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
			Date ts = df.parse(epochString); 
			epoch = (Long) ts.getTime() / 1000 ;
		} catch( Exception e )
		{
			throw new RuntimeException( "couldn't extract timestamp '" + epochString + "' " + e.getMessage()  );
		}

		fill = Float.valueOf( attributes.get( "_FillValue" )).floatValue();
	}  

	// should have an init() or prepare() function?
	public void encode( Array A, int ima,  Object value )
	{
		// System.out.println( "****************** unit is '" + unit + "'"); 

		// this needs to be changes
		if( value == null) {
			A.setFloat( ima, fill );
		}
		else if( value instanceof java.sql.Timestamp ) {
			long seconds =  ((java.sql.Timestamp)value).getTime() / 1000  ;
			long ret = seconds - epoch; 
			if( unit.equals("days"))	
				ret /= 86400; 
			else if( unit.equals("minutes"))	
				ret /= 1440; 
			else if ( unit.equals("seconds"))
				;
			else 
				throw new RuntimeException( "unrecognized time unit " + unit );

			A.setFloat( ima, (float) ret );
		}
		else {
			throw new RuntimeException( "Not a timestamp" );
		}
	}
}



class FloatValueEncoder implements IValueEncoder
{

	FloatValueEncoder()
	{
		this.fill = 1234; 
	}

	float fill; 

	// change name to targetType
	public DataType targetType()
	{
		return DataType.FLOAT;
	}


	public void prepare(  Map<String, String> attributes ) 
	{ 
		fill = Float.valueOf( attributes.get( "_FillValue" )).floatValue();

	}  

	public void encode( Array A, int ima, Object value )
	{
		if( value == null) {
			A.setFloat( ima, fill );
		}
		else if( value instanceof Float ) {
			A.setFloat( ima, (Float) value);
		}
		else if( value instanceof Double ) {
			A.setFloat( ima, (float)(double)(Double) value);
		}
		else if( value instanceof Long ) { // for testing - should remove
			A.setFloat( ima, (float)(long)(Long) value);
		}
		else {
			throw new RuntimeException( "Failed to coerce type '" + value.getClass() + "' to float" );
		}
	}
}


class ByteValueEncoder implements IValueEncoder
{
	ByteValueEncoder()
	{
		this.fill = 0x0;	
	}
	byte fill;  

	// assumption that the Object A is a float array
	public DataType targetType()
	{
		return DataType.BYTE;
	}

	public void prepare( Map<String, String> attributes ) 
	{ 
		// eg. no unsigned byte in java, so use integer and downcast
		fill = (byte) Integer.decode( attributes.get( "_FillValue" ) ).intValue();
	}  

	public void encode( Array A, int ima, Object value )
	{
		if( value == null) {
			A.setByte( ima, fill );
		}
		else if(value instanceof Byte)
		{
			A.setByte( ima, (Byte) value);
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



interface IAddValue
{
	// change name to put(), or append? and class to IBufferAddValue
	public void addValueToBuffer( Object value );
}

interface IVariableEncoder extends IAddValue
{
	// change name VarEncoder

	// name is provided by map lookup
	// theoretically this object could also preserve the index
	// if we give it a concept of the name, then it can also define the netcdf.

//	public void define();  // change name to start(); ?


	public void define( NetcdfFileWriteable writer ) ;
	public void finish( NetcdfFileWriteable writer) throws Exception ;

	public void addValueToBuffer( Object value );

	public String getName(); // change class name to IVariableEncoder and this to just getName()

	public void dump();
}


interface IDimension extends IAddValue
{
	public void define( NetcdfFileWriteable writer) ;

	public Dimension getDimension( ) ; // horrible to expose this...
										// can't the caller create the dimension?
	//
	public int getLength();

	public void addValueToBuffer( Object value );

	public String getName();

	public void dump();
}

// this will get rid of the horrible recursion as well...


// VERY IMPORTANT - we need to keep the dimension list separately, because they must be written
// before the variables.

class DimensionImpl implements IDimension
{
	// dimension determines sql ordering criteria.

	public DimensionImpl( String name )
	{
		this.name = name; // required to encode dimension
		this.size = 0;
	}

	final String name;
	int size;
	Dimension dimension;


	public Dimension getDimension( )  // bad naming
	{
		// throw if not defined...
		return dimension;
	}

	public int getLength()
	{
		return size;
	}

	public void define( NetcdfFileWriteable writer)
	{
		// shouldn't do all this at the same time...
		// uggh.
		// no children means it's a dimension... actually could still be a stand alone scalar, that's an array.

		System.out.println( "** before writing dimension " + name + " " + size );

		dimension = writer.addDimension( name, size );
		//return null;
	}

	public void addValueToBuffer( Object value )
	{
		++size;
	}

	public String getName() { return name ; }

	public void dump()
	{
		System.out.println( "** Dimension size " + size );
	}
}





/*
	The final netcdf document is actully a combination of everything
*/

class VariableEncoder implements IVariableEncoder
{


		// IVariableEncoder temp = new VariableEncoder ( "TEMP", idimensions, floatEncoder, floatAttributes ) ;

	//public VariableEncoder( String variableName, ArrayList< IVariableEncoder>  children )

	//public EncoderD1( NetcdfFileWriteable writer, String variableName, ArrayList<Dimension> dims, Map<String, Object> attributes, IValueEncoder encodeValue )
	public VariableEncoder( String variableName, ArrayList< IDimension> dimensions, IValueEncoder encodeValue, Map<String, String> attributes )
	{
		this.variableName = variableName;
		this.encodeValue = encodeValue;
		this.attributes = attributes;
		this.dimensions = dimensions;

		this.buffer = new ArrayList<Object>( );

//		this.isDefined = false;
//		this.dimension = null;
	}

	final String variableName;
	final IValueEncoder			encodeValue;
	final Map<String, String>	attributes;
	final ArrayList<IDimension>	dimensions; // change name childDimensions
	final ArrayList<Object>		buffer;

//	boolean isDefined;
//	Dimension dimension;



	/*	we can also record the table, or index of table here if we want
			to incorporate into the strategy.
		eg. we can compre with xml to decide what to do.
	*/
	public void addValueToBuffer( Object value )
	{
		// perhaps delegate to strategy...
		buffer.add( value );
	}

	public void define( NetcdfFileWriteable writer )
	{
		// write dims and attributes

		// make sure children are defined already
		List<Dimension> d = new ArrayList<Dimension>();
		for( IDimension dimension: dimensions)
		{
			d.add( dimension.getDimension() );
		}

		writer.addVariable(variableName, encodeValue.targetType(), d );

		for( Map.Entry< String, String> entry : attributes.entrySet()) {
			writer.addVariableAttribute( variableName, entry.getKey(), entry.getValue()/*.toString()*/ );
		}
	}

	// we're going to need to pass in our instantiated array
	// or we use a modulo to produce the value ???

	public void writeValues( ArrayList<IDimension> dims, int dimIndex, int acc, Array A  )
	{
		// ok, actually we only need to compute the Index that we will use...
		// this is always going to generate a linear sequence...

		// so what is the actual usefulness...

		if( dimIndex < dims.size() )
		{
			Dimension dim = dims.get( dimIndex ).getDimension();
			for( int i = 0; i < dim.getLength(); i++ )
			{
				writeValues( dims, dimIndex + 1, acc + i, A );
			}
		}
		else
		{
			// System.out.println( "dimIndex " + "  acc " + acc  + "  buffer " + buffer.get( acc ) );
			// public void encode( Array A, int ima, Map<String, Object> attributes, Object value );

			encodeValue.encode( A, acc, buffer.get( acc ) );

			// A.setFloat( acc, (float) 99999. );
		}

	}

	private static int[] toIntArray( List<Integer> list)
	{
		// List.toArray() only supports Boxed Integers...
		int[] ret = new int[list.size()];
		for(int i = 0;i < ret.length;i++)
			ret[i] = list.get(i);
		return ret;
	}

	public void finish( NetcdfFileWriteable writer) throws Exception
	{
		// change name to writeValues ?
		// now we have to set up a loop ... over all the dimensions...
		// which means we have to assemble the dimensions again.
		System.out.println( "finish " + variableName );

		ArrayList< Integer> shape = new ArrayList< Integer>() ;
		for( IDimension dimension : dimensions ) {
			shape.add( dimension.getLength() );
		}

		Array A = Array.factory( encodeValue.targetType(), toIntArray(shape ) );

		encodeValue.prepare( attributes );

		writeValues( dimensions,  0, 0 , A );

		// int [] origin = new int[1];
		// writer.write(variableName, origin, A);
		writer.write(variableName, A);
/**/
	}

	public void dump()
	{
		System.out.println( "WHOOT ENCODEER - " + variableName + " buffer size " + buffer.size() );
	}

	public String getName()
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


class NodeWrapper implements Iterable<Node> {

    private Node node;
    private List<Node> nodes;
    private NodeList nodeList;

    public NodeWrapper(Node node) {
        this.node = node;
    }

    public Iterator<Node> iterator() {
        if (nodes == null) {
            buildNodes();
        }

        return nodes.iterator();
    }

    private void buildNodes() {
        nodes = new ArrayList<Node>(getListLength());
        for (int i = 0; i < getListLength(); i++) {
            nodes.add(nodeList.item(i));
        }
    }

    private int getListLength() {
        return getNodeList().getLength();
    }

    private NodeList getNodeList() {
        if (nodeList == null) {
            setNodeList();
        }
        return nodeList;
    }

    private void setNodeList() {
        if (node.hasChildNodes()) {
            nodeList = node.getChildNodes();
        }
        else {
            nodeList = new NullNodeList();
        }
    }

    private class NullNodeList implements NodeList {

        public Node item(int index) {
            return null;
        }

        public int getLength() {
            return 0;
        }
    }
}


// More data than a class 

class NcfDefinition
{
	NcfDefinition(
		String schema,
		String virtualDataTable,
		String virtualInstanceTable,
		Map< String, IDimension> dimensions,
		Map< String, IVariableEncoder> encoders
	) {
		this.schema = schema;
		this.virtualDataTable = virtualDataTable;
		this.virtualInstanceTable = virtualInstanceTable;
		this.dimensions = dimensions;
		this.encoders = encoders;
	}

	final String schema;
	final String virtualDataTable;
	final String virtualInstanceTable;
	final Map< String, IDimension> dimensions;
	final Map< String, IVariableEncoder> encoders;
}





class NcfDefinitionXMLParser
{
	private boolean isNodeName( Node node, String name )
	{
		return node.getNodeType() == Node.ELEMENT_NODE
			&& node.getNodeName().equals( name );
	}

	private String nodeVal( Node node  )
	{
		Node child = node.getFirstChild();
		if( child != null && child.getNodeType() == Node.TEXT_NODE )
			return child.getNodeValue();

		return "";
	}


	private Map< String, String> parseKeyVals( Node node )
	{
		// have another version that does this for attributes
		Map< String, String> m = new HashMap< String, String>();
		for( Node child : new NodeWrapper(node) )
			if( child.getNodeType() == Node.ELEMENT_NODE )
				m.put( child.getNodeName(), nodeVal( child ) );

		// add any xml attributes
		NamedNodeMap attrs = node.getAttributes();
		for( int i = 0; i < attrs.getLength(); ++i )
		{
			Node child = attrs.item( i) ;
			m.put( child.getNodeName(), nodeVal( child ) );
		}

		return m;
	}

	private IDimension parseDimension( Node node)
	{
		if( isNodeName( node, "dimension")) {
			Map< String, String> m = parseKeyVals( node );
			return new DimensionImpl( m.get( "name" ) );
		}
		return null;
	}

	// having a simple parse key-vals function, means we can do it with attributes as alternative syntax.

	private Map< String, IDimension> parseDimensions( Node node )
	{
		if( isNodeName( node, "dimensions"))
		{
			Map< String, IDimension> dimensions = new HashMap< String, IDimension> () ;
			for( Node child : new NodeWrapper(node) ) {
				IDimension dimension = parseDimension( child );
				if( dimension != null)
					dimensions.put( dimension.getName(), dimension );
			}
			return dimensions;
		}
		return null;
	}


	private IValueEncoder parseEncoder( Node node)
	{
		if( isNodeName( node, "encoder"))
		{
			String val = nodeVal( node );
			if( val.equals( "float")) {
				return new FloatValueEncoder();
			}
			else if( val.equals( "byte")) {
				return new ByteValueEncoder();
			}
			else if( val.equals( "time")) {
				return new TimestampValueEncoder();
			}
			else
			{
				throw new RuntimeException( "Unrecognized value type encoder" );
			}
		}
		return null;
	}


	private SimpleImmutableEntry<String, String> parseAttribute( Node node )
	{
		// we ought to be able to coerce the object type immediately here.
		// no, if it's a timevalue then  we want it left as a string

		// No i think it's better to leave interpreting this until we're right at the point of encoding using
		// the specific encoder type...
		// we can cache a fill value if we really required.

		if( isNodeName( node, "attribute"))
		{
			Map< String, String> m = parseKeyVals( node );
			String key = m.get("name");
			String val = m.get("value");
			return new SimpleImmutableEntry< String, String>( key, val );
		}
		return null;
	}

/*
	- we really need the simplified looping construct
	- which should be easy to do.
*/
	private Map<String, String> parseAttributes( Node node )
	{
		if( isNodeName( node, "attributes"))
		{
			Map<String, String> m = new HashMap<String, String> ();
			for( Node child : new NodeWrapper(node) ) {
				SimpleImmutableEntry< String, String> pair = parseAttribute( child);
				if( pair != null)
					m.put( pair.getKey(), pair.getValue());
			}
			return m;
		}
		return null;
	}


	private IDimension parseDimensionRef ( Node node, Map< String, IDimension> dimensionsContext )
	{
		if( isNodeName( node, "dimension")) {
			Map< String, String> m = parseKeyVals( node );
			// System.out.println( "found dimension ref " + m.get( "name" ) );
			return dimensionsContext.get( m.get( "name" ));
		}
		return null;
	}


	private Map< String, IDimension> parseDimensionsRef( Node node, Map< String, IDimension> dimensionsContext )
	{
		if( isNodeName( node, "dimensions"))
		{
			Map< String, IDimension> dimensions = new HashMap< String, IDimension> () ;
			for( Node child : new NodeWrapper(node) ) {
				IDimension dimension = parseDimensionRef( child, dimensionsContext);
				if( dimension != null)
					dimensions.put( dimension.getName(), dimension );
			}
			return dimensions;
		}
		return null;
	}


	// think we may want a more general context ...


	private IVariableEncoder parseVariableEncoder( Node node, Map< String, IDimension> dimensionsContext  )
	{
		String name = null;
		Map< String, IDimension> dimensions = null;  // this is wrong. we should be looking it up by name.
		IValueEncoder encodeValue = null;
		Map< String, String> attributes = null;

		if( isNodeName( node, "variable"))
		{
			for( Node child : new NodeWrapper(node) )
			{
				// this is very neat. may want to do this explicitly rather than using the map...
				if( isNodeName( child, "name" ))
					name = nodeVal( child);
				if( dimensions == null)
					dimensions = parseDimensionsRef( child, dimensionsContext );
				if( encodeValue == null)
					encodeValue = parseEncoder( child ) ;
				if( attributes == null)
					attributes = parseAttributes( child );
			}

			if( dimensions == null )
			{
				dimensions = new HashMap< String, IDimension> ();
			}

			if( name != null
				&& encodeValue != null
				&& attributes != null )
			{
				System.out.println( "whoot creating encoder " + name  );

				return new VariableEncoder ( name , new ArrayList<IDimension>(dimensions.values()), encodeValue , attributes ) ;
			}
			else {
				throw new RuntimeException("missing something  " );
				// return null;
			}
		}
		return null;
	}


	private Map< String, IVariableEncoder> parseVariableEncoders( Node node, Map< String, IDimension> dimensionsContext  )
	{
		if( isNodeName( node, "variables"))
		{
			Map< String, IVariableEncoder> m = new HashMap < String, IVariableEncoder>();
			for( Node child : new NodeWrapper(node)) {
				IVariableEncoder e = parseVariableEncoder( child, dimensionsContext  );
				if( e != null )
					m.put( e.getName(), e );
			}
			return m;
		}
		return null;
	}

	private Map< String, String> parseSource( Node node )
	{
		if( isNodeName( node, "source")) /// data or dataSource
		{
			Map< String, String> source = parseKeyVals( node);

			System.out.println( "*** GOT CONFIG " +  source.size()   );
			for( Map.Entry< String, String> entry : source.entrySet()) {
				System.out.println( "*** " + entry.getKey() + " " + entry.getValue() );

			}
			return source;
		}
		return null;

	}

	NcfDefinition parseDefinition( Node node )
	{
		// think we need a context?
		if( isNodeName( node, "definition"))
		{
			Map< String, String> source = null;
			Map< String, IDimension> dimensions = null;
			Map< String, IVariableEncoder> encoders = null;

			// pick out dimensions
			for( Node child : new NodeWrapper(node)) {

				if( source == null )
					source = parseSource( child );

				if( dimensions == null )
					dimensions = parseDimensions( child );

				if( encoders == null )
					encoders = parseVariableEncoders( child, dimensions );
			}

			String schema = source.get( "schema" );
			String virtualDataTable = source.get( "virtualDataTable" );
			String virtualInstanceTable =source.get( "virtualInstanceTable" );

			return new NcfDefinition( schema, virtualDataTable, virtualInstanceTable, dimensions, encoders );
		}
		return null;
	}
}




	// type of the attribute comes from the encoder type target type

	// IMPORTANT - the way to handle this. is to take it as isSimpleNodeValue pair...
	// eg. <name>value<name>
	// more complicated stuff can be parsed explicitly.

/*
	private static String XML = "<?xml version=\"1.0\"?>\n" +
		"<top>\n" +
		"  <dimensions>\n" +
		"    <dimension name=\"TIME\"/>\n" +
		"  </dimensions>\n" +
		"  <myencoder  >\n" +
		"    <name>TEMP</name>\n" +
		"    <encoder>floatEncoder</encoder>\n" +
		"    <dimensions>\n" +
		"      <dimension name=\"TIME\"/>\n" +
		"    </dimensions>\n" +
		"    <attributes>\n" +
		"      <attribute name=\"_FillValue\" value=\"99999.\"/>\n" +
		"    </attributes>\n" +
		"  </myencoder>\n" +
		"</top>\n"
	;
*/

	// it actually has to be a bottom out node...
	// ahh no, we can probably....
	// select the child node by name ... explicitly...
	// unless we want to simply be able to return the

	// we may need property setters - to completely integrate into geoserver
	// but we'll try to do this with constructor


	// TJ's stuff is built on the assumption that everything is a simple object

	// why don't we build a setter  object  that's what the key pair is

	// so we should pass the map in...


	/*
		- things like table name are also going to be in here.
		So i think that we need a context.

	*/
/*
	public NcfDefinition  test() throws Exception
	{
		InputStream stream = new ByteArrayInputStream(XML.getBytes(StandardCharsets.UTF_8));
		Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
		Node node =	document.getFirstChild();

		return new NcfDefinitionXMLParser().parseDefinition( node );
	}
*/




// we need the query or the selection to be exposed, so we can formulate
// like a fold, with an init and transform
// we should definitely pass a writable here ...
// rather than instantiate it

// hang on they share the same name...
// so we could put it in a list...

/*
	virtual projections...
	select * from ( select 1 as x) as table1
*/




/*
	ok, lets try profile.
*/

class NcfGenerator
{
	final IExprParser exprParser;				// change name to expressionParser or SelectionParser
	final IDialectTranslate translate ;		// will also load up the parameters?
	final Connection conn;
	final ICreateWritable createWritable; // generate a writiable
	final NcfDefinition definition ;
	final String filterExpr;

	final int fetchSize;
	IExpression selection_expr;
	ResultSet featureInstancesRS;

	public NcfGenerator(
		IExprParser exprParser,
		IDialectTranslate translate,
		Connection conn,
		ICreateWritable createWritable,
		NcfDefinition definition,
		String filterExpr
	) {
		this.exprParser = exprParser;
		this.translate = translate; // sqlEncode.. dialect... specialization
		this.conn = conn;
		this.createWritable = createWritable;
		this.definition = definition;
		this.filterExpr = filterExpr;

		fetchSize = 1000;
		featureInstancesRS = null;
		selection_expr = null;
	}

	public void init() throws Exception
	{
		selection_expr = exprParser.parseExpression( filterExpr, 0);
		// bad, should return expr or throw
		if(selection_expr == null) {
			throw new RuntimeException( "failed to parse expression" );
		}

		System.out.println( "setting search_path to " + definition.schema );

		PreparedStatement s = conn.prepareStatement("set search_path='" + definition.schema + "'");
		// PreparedStatement s = conn.prepareStatement("set search_path='" + schema + "',public");
		// PreparedStatement s = conn.prepareStatement("set search_path=" + schema + ",public");
		s.execute();
		s.close();

		String selection = translate.process( selection_expr);

		String query = "SELECT distinct data.instance_id  FROM (" + definition.virtualDataTable + ") as data where " + selection + ";" ;
		System.out.println( "first query " + query  );

		PreparedStatement stmt = conn.prepareStatement( query );
		stmt.setFetchSize(fetchSize);

		// try ...
		// change name featureInstancesRSToProcess ?
		featureInstancesRS = stmt.executeQuery();
		System.out.println( "done determining feature instances " );
		// should determine our target types here
	}

	public void populateValues(
		Map< String, IDimension> dimensions,
		Map< String, IVariableEncoder> encoders,
		String query
		)  throws Exception
	{
		System.out.println( "query " + query  );

		// sql stuff
		PreparedStatement stmt = conn.prepareStatement( query );
		stmt.setFetchSize(fetchSize);
		ResultSet rs = stmt.executeQuery();

		// now we loop the main attributes
		ResultSetMetaData m = rs.getMetaData();
		int numColumns = m.getColumnCount();

		// pre-map the encoders by index according to the column name
		ArrayList< IAddValue> [] processing = (ArrayList< IAddValue> []) new ArrayList [numColumns + 1];

		for ( int i = 1 ; i <= numColumns ; i++ ) {
			// System.out.println( "column name "+ m.getColumnName(i) );
			processing[i] = new ArrayList< IAddValue> ();

			IDimension dimension = dimensions.get( m.getColumnName(i));
			if( dimension != null)
				processing[i].add( dimension );

			IAddValue encoder = encoders.get(m.getColumnName(i));
			if( encoder != null)
				processing[i].add( encoder );
		}

		// process result set rows
		while ( rs.next() ) {
			for ( int i = 1 ; i <= numColumns ; i++ ) {
				for( IAddValue p : processing[ i] ) {
					p.addValueToBuffer( rs.getObject( i));
				}
			}
		}
	}

	// we could make the TIME explicit in the query.
	// as the data.dimension      and data.instance_id

	// How do we represent the dimension order?

	// probably better to lookit up in the input.xml

	// for testing


	public NetcdfFileWriteable get() throws Exception
	{
		if( featureInstancesRS.next()) {

			// we ay have an Integer or Long or anything ?
			//long instance_id = (long)(Integer) featureInstancesRS.getObject(1);

			// munge
			long instance_id = -1234;
			Object o = featureInstancesRS.getObject(1);
			Class clazz = o.getClass();
			if( clazz.equals( Integer.class )) {
				instance_id = (long)(Integer)o;
			}
			else if( clazz.equals( Long.class )) {
				instance_id = (long)(Long)o;
			} else {
				throw new RuntimeException( "Can't convert intance_id type to integer" );
			}

	//		long instance_id = (long)(Long) featureInstancesRS.getObject(1);

			System.out.println( "whoot get(), instance_id is " + instance_id );

			String selection = translate.process( selection_expr); // we ought to be caching the specific query ???

			populateValues( definition.dimensions, definition.encoders, "SELECT * FROM (" + definition.virtualInstanceTable + ") as instance where instance.id = " + Long.toString( instance_id) );


			// is the order clause in sql part of projection or selection ?

			// eg. concat "," $ map (\x -> x.getName) dimensions.values ...
			String dimensionVar = "";
			for( IDimension dimension : definition.dimensions.values() )
			{
				if( ! dimensionVar.equals("")){
					dimensionVar += ",";
				}
				dimensionVar += "\"" + dimension.getName() + "\"" ;
			}

			populateValues( definition.dimensions, definition.encoders, "SELECT * FROM (" + definition.virtualDataTable + ") as data where " + selection +  " and data.instance_id = " + Long.toString( instance_id) + " order by " + dimensionVar  );

			NetcdfFileWriteable writer = createWritable.create();




			for ( IDimension dimension: definition.dimensions.values()) {
				dimension.define(writer);
			}

			for ( IVariableEncoder encoder: definition.encoders.values()) {
				encoder.define( writer );
			}
			// finish netcdf definition
			writer.create();

			for ( IVariableEncoder encoder: definition.encoders.values()) {
				// change name writeValues
				encoder.finish( writer );
			}
			// write the file
			writer.close();

			// TODO must close other record sets.
			// and if early termination.
	//		return null;

			// TODO we should be returning a filestream here...
			// the caller doesn't care that it's a netcdf

			return writer;
		}
		else {

			// close close close !!!@!

			return null;
		}
	}
}


// two interfaces a builder to generate, and then a class to use.

class NcfGeneratorBuilder
{
	// default instance creation

	// the assembly of all this,
	// need to distinguish the user data from inbuild data.

	public NcfGeneratorBuilder()
	{

	}

	// not sure if we should do this here...

	// certainly configuration should be pulled out... and put in resources.

    public static Connection getConn() throws Exception
	{
		//String url = "jdbc:postgresql://127.0.0.1/postgres";

		// psql -h test-geoserver -U meteo -d harvest

		String url = "jdbc:postgresql://test-geoserver/harvest";
		Properties props = new Properties();
		props.setProperty("user","meteo");
		props.setProperty("password","meteo");

//		props.setProperty("search_path","soop_sst,public");
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

	public NcfGenerator create ( InputStream config, String filterExpr) throws Exception
	{
		// not sure if definition decoding should be done here...
		NcfDefinition definition = null;
		try {
			// new ByteArrayInputStream(XML.getBytes(StandardCharsets.UTF_8));
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(config);
			Node node =	document.getFirstChild();
			definition = new NcfDefinitionXMLParser().parseDefinition( node );

		} finally {
			config.close();
		}

		// change name exprParser
		IExprParser parser = new ExprParser();
		IDialectTranslate translate = new  PostgresDialectTranslate();
		Connection conn = getConn();
		ICreateWritable createWritable = new CreateWritable();

		// avoiding ordering clauses that will prevent immediate stream response
		// we're going to need to sanitize this

		NcfGenerator generator = new NcfGenerator( parser, translate, conn, createWritable, definition, filterExpr );

		generator.init();	 // change name initGenerator..., distinct action from assembling the dependencies of the class.

		return generator ;
	}
}


