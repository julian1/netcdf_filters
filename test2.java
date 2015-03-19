
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
import java.util.List; //io.BufferedInputStream;
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

import java.util.Arrays;

import java.util.regex.Pattern ; 
import java.util.regex.Matcher; 



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
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
	// Change name to ValueEncoderTimestamp

	public void encode( Array A, int ima, Map<String, Object> attributes, Object value ); 

	//public Class targetType(); 
	public DataType targetType(); 
}





class EncodeTimestampValue implements IEncodeValue
{
	public DataType targetType()
	{
		return DataType.FLOAT;//.class;
	}

	public void encode( Array A, int ima, Map<String, Object> attributes, Object value )
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
	public DataType targetType()
	{
		return DataType.FLOAT;//.class;
	}

	public void encode( Array A, int ima, Map<String, Object> attributes, Object value )
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
	public DataType targetType()
	{
		return DataType.BYTE;//.class;
	}

	public void encode( Array A, int ima, Map<String, Object> attributes, Object value )
	{
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



interface IAddValue
{
	public void addValueToBuffer( Object value ); 
}

interface IEncoder extends IAddValue
{
	// change name VarEncoder

	// name is provided by map lookup
	// theoretically this object could also preserve the index
	// if we give it a concept of the name, then it can also define the netcdf.

//	public void define();  // change name to start(); ?


	public void define( NetcdfFileWriteable writer ) ; 
	public void finish( NetcdfFileWriteable writer) throws Exception ; 

	public void addValueToBuffer( Object value ); 

	public String getVariableName(); // change class name to IVariableEncoder and this to just getName()

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

class MyDimension implements IDimension 
{
	// dimension determines sql ordering criteria.

	public MyDimension( String name ) 
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

class MyEncoder implements IEncoder
{


		// IEncoder temp = new MyEncoder ( "TEMP", idimensions, floatEncoder, floatAttributes ) ; 

	//public MyEncoder( String variableName, ArrayList< IEncoder>  children )

	//public EncoderD1( NetcdfFileWriteable writer, String variableName, ArrayList<Dimension> dims, Map<String, Object> attributes, IEncodeValue encodeValue )
	public MyEncoder( String variableName, ArrayList< IDimension> dimensions, IEncodeValue encodeValue, Map<String, Object> attributes )
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
	final IEncodeValue			encodeValue; 
	final Map<String, Object>	attributes; 
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

		for( Map.Entry< String, Object> entry : attributes.entrySet()) { 
			writer.addVariableAttribute( variableName, entry.getKey(), entry.getValue().toString() ); 
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
			encodeValue.encode( A, acc, attributes, buffer.get( acc ) ); 

			// A.setFloat( acc, (float) 99999. ); 
		}
		
	}


	static int[] toIntArray( List<Integer> list)
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




class DecodeXmlConfiguration
{
	public DecodeXmlConfiguration () { } 

	private static String XML = "<?xml version=\"1.0\"?>\n" +
		"    <dimension>\n" +
		"        <name>TIME</name>\n" +
		"        <name2>TIME</name2>\n" +
		"    </dimension>\n" ;


	// it actually has to be a bottom out node...
	// ahh no, we can probably....
	// select the child node by name ... explicitly...
	// unless we want to simply be able to return the 

	// 
/*
	public Map<String, Object> parseChildren( Node node )
	{
		if( node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName() == "dimension" ) 
		{ // do dimen
		}
		return null;
	} 
	public Object parseVal( Node node) 
	{
		if( node.getNodeType() = Node.TEXT_NODE )
		{ }
	}

	public void parseKeyVal( Node node) 
	{
		// we use this to get a list of key val objects to instantiate our class...
		// if it ends in s we could pull a list 
		// so we have to return a tuple.
		String key = node.getNodeName();  // required...
	}
	public Object parseValue( Node node ) 
	{
		if( node.getNodeType() == Node.TEXT_NODE)
		{
			return  
		}
	}
		if( node.getNodeType() == Node.ELEMENT_NODE) 
			System.out.println( "node " + node.getNodeName() );
		else if( node.getNodeType() == Node.TEXT_NODE) 
			System.out.println( "text " + node.getNodeValue() );


*/

	public String parseSimpleStringValue( Node node ) 
	{
		// simple key value pair eg.   <name>value</name>	
		String val = null;
		NodeList lst = node.getChildNodes(); 
		for( int i = 0; i < lst.getLength(); ++i )
		{
			Node child = lst.item( i);
			if( child.getNodeType() == Node.TEXT_NODE)
			{
				val = child.getNodeValue();
			}
		}
		return val;	
	}

	public Object parseComplexValue( Node node ) 
	{
		if( node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName() == "dimension" ) 
		{
			System.out.println( "whoot got dimension"  );

			Map< String, Object> pairs  = new HashMap< String, Object>();
			parseKeyValues( node, pairs ) ;

			// instantiate
						
		}
		// throw
		return null;
	}

	public void parseKeyValue( Node node, Map< String, Object> pairs ) 
	{
		// we do the children...

		String key = node.getNodeName();
		Object val = parseSimpleStringValue( node ) ;

		if( val == null ) {


		}
		System.out.println( "got pair " + key + " " + val );
		pairs.put( key, val );
	}

	public void parseKeyValues( Node node, Map< String, Object> pairs ) 
	{
		NodeList lst = node.getChildNodes(); 
		for( int i = 0; i < lst.getLength(); ++i )
		{
			Node child = lst.item( i);
			if( child.getNodeType() == Node.ELEMENT_NODE )
				parseKeyValue( child, pairs ) ;
		}
	}
/*
	public void walk( Node node, int depth ) 
	{
		for( int i = 0; i < depth; ++i)
			System.out.print( "   " );


		if( node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName() == "dimension" ) 
		{
			System.out.println( "whoot got dimension"  );

			Map< String, Object> pairs  = new HashMap< String, Object>();
			parseKeyValues( node, pairs ) ;
		}

		else {
			NodeList lst = node.getChildNodes(); 
			for( int i = 0; i < lst.getLength(); ++i )
			{
				Node child = lst .item( i); 
				walk( child, depth + 1 );
			}
		}
	}
*/

	public void test() throws Exception 
	{	
		InputStream stream = new ByteArrayInputStream(XML.getBytes(StandardCharsets.UTF_8));
		Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
		Node node =	document.getFirstChild(); 

		parseComplexValue( node ) ;
	
//		walk( node, 0 );
	}
   
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

	// hang on they share the same name...
	// so we could put it in a list...

	public void populateValues(  
		Map< String, IDimension> dimensions, 
		Map< String, IEncoder> encoders, 
		String query  

		)  throws Exception
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
		
		// pre-map the encoders by index according to the column name 
		ArrayList< IAddValue> [] processing = (ArrayList< IAddValue> []) new ArrayList [numColumns + 1]; 
		// ArrayList< IAddValue> [] processing = (ArrayList< IAddValue> []) java.lang.reflect.Array.newInstance( ArrayList.class, numColumns + 1) ; 

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


	public NetcdfFileWriteable get() throws Exception
	{
		// code organized so we only iterate over the recordset returned by the query once

		featureInstances.next();

		long ts_id = (long) featureInstances.getObject(1); 


		System.out.println( "whoot get(), ts_id is " + ts_id );

		String selection = translate.process( selection_expr); // we ought to be caching the specific query ??? 
					
		// we'll add them all to a List 
//		IEncoder e = new MyEncoder ( "LATITUDE", null ) ; 




		Map< String, IDimension> dimensions = new HashMap< String, IDimension> ();


		IDimension time_ = new MyDimension( "TIME" );
		dimensions.put( time_.getName(), time_ );



		Map< String, IEncoder> encoders = new HashMap< String, IEncoder> ();


		IEncodeValue floatEncoder = new EncodeFloatValue();
		IEncodeValue byteEncoder = new EncodeByteValue();
		IEncodeValue timestampEncoder = new EncodeTimestampValue();


		Map<String, Object> timestampAttributes = new HashMap<String, Object>();
		timestampAttributes.put( "units", "days since 1950-01-01 00:00:00 UTC" );
		timestampAttributes.put( "_FillValue", (float) 999999. ); 

		Map<String, Object> floatAttributes = new HashMap<String, Object>();
		floatAttributes.put( "_FillValue", (float) 999999. ); 

		Map<String, Object> byteAttributes = new HashMap<String, Object>();
		byteAttributes.put( "_FillValue", (byte) 0xff ); 


		// the dimensions 

		// time dimension and time variable 
		

	/*	IEncoder lat = new MyEncoder ( "LATITUDE", null, floatEncoder, floatAttributes); 
		IEncoder lon = new MyEncoder ( "LONGITUDE", null , floatEncoder, floatAttributes); 
		IEncoder time = new MyEncoder ( "TIME", null, timestampEncoder, timestampAttributes) ; 
	*/
		// where on earth are the attributes coming from ? 

		// VERY IMPORTANT - dimensions are the same as sql ordering criteria. order by TIME. they decide the encode order.


		ArrayList< IDimension> idimensions = new ArrayList<IDimension>(); 
		idimensions.add( time_ );


		// OK
//		IEncoder u [] = { /*lat, lon,*/ time };   // we should use a list to make this simpler

		//IEncoder temp = new MyEncoder ( "TEMP", new ArrayList< IEncoder>( Arrays.asList( u )), floatEncoder, floatAttributes ) ; 
		IEncoder temp = new MyEncoder ( "TEMP", idimensions, floatEncoder, floatAttributes ) ; 

		IEncoder time_qc = new MyEncoder ( "TIME_quality_control", idimensions, byteEncoder, byteAttributes ) ; 
		
		//encoders.put( lat.getVariableName(), lat ) ; 
		//encoders.put( lon.getVariableName(), lon ) ; 
		//encoders.put( time.getVariableName(), time ) ; 
		encoders.put( temp.getVariableName(), temp ) ; 
		encoders.put( time_qc.getVariableName(), time_qc ) ; 

		/*
			For timeseries - we may only need a 
		*/

		// we could have a common interface for addValue...
		// in order to avoid passing encoders and dimensions.

		// yes and avoid having the two loops...
		// we also ought to map values by index... rather than doing the complicated name lookup


		populateValues( dimensions, encoders, "SELECT * FROM anmn_ts.timeseries where id = " + Long.toString( ts_id) );
		populateValues( dimensions, encoders, "SELECT * FROM anmn_ts.measurement where " + selection +  " and ts_id = " + Long.toString( ts_id) + " order by \"TIME\" "  );


		/* IMPORTANT Issue - ordering criteria...
		*/

/*		for ( IEncoder encoder: encoders.values())
		{
			encoder.dump();
		}
*/
		// now we loop the encoders and try to define...
		// will need to pass a dimensions need if it's already defined
		// 


		NetcdfFileWriteable writer = createWritable.create();


		for ( IDimension dimension: dimensions.values())
		{
			dimension.define(writer);
		}


		for ( IEncoder encoder: encoders.values())
		{
			encoder.define( writer );
		}


		// finish netcdf definition
		writer.create();


		for ( IEncoder encoder: encoders.values())
		{
			// change name writeValues
			encoder.finish( writer );
		}


		// close
		writer.close();
	

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

		DecodeXmlConfiguration x = new DecodeXmlConfiguration(); 
		x.test();



/*

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
*/
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



