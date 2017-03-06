package matching;
import java.sql.*;
import java.util.Properties;

import org.xbib.jdbc.csv.CsvDriver;

import similarity.matching.AnnotationComparison;

public class DemoDriver
{
	private Statement stmt;
	private String file;
	public DemoDriver(String folder, String file) throws SQLException, ClassNotFoundException
	{
		// Load the driver.
	    Class.forName("org.xbib.jdbc.csv.CsvDriver");

	    // Create a connection. The first command line parameter is
	    // the directory containing the .csv files.
	    // A single connection is thread-safe for use by several threads.
	    Properties props = new Properties();
	    props.put("suppressHeaders", "true");
	    props.put("headerline", "E1\tE2\tSIM");
	    props.put("separator", "\t");
	    props.put("charset", "UTF-8");
	    Connection conn = DriverManager.getConnection("jdbc:xbib:csv:" + folder, props);// +"?separator=\t?suppressHeaders=True"); //?headerline=E1\tE2\tSIM

	    // Create a Statement object to execute the query with.
	    // A Statement is not thread-safe.
	    stmt = conn.createStatement();
	    this.file = file;
	}
	
	public double getSimilarity(AnnotationComparison comp) throws SQLException
	{
		String a = (String) comp.getConceptA();
		String b = (String) comp.getConceptB();
		//String query = "SELECT SIM FROM " + file + " WHERE (E1=\"" + a + "\" AND E2=\"" + b + "\") OR (E1=\"" + b + "\" AND E2=\"" + a + "\")";
		String query = "SELECT SIM FROM " + file + " WHERE E1 Like 'http://dbpedia.org/resource/Igor_Milić/dump0'";// WHERE (E1='" + a + "')";
		System.out.println(query);
		ResultSet results = stmt.executeQuery(query);
		return results.getDouble(1);
	}
  public static void main(String[] args) throws Exception
  {
    // Load the driver.
    Class.forName("org.xbib.jdbc.csv.CsvDriver");

    // Create a connection. The first command line parameter is
    // the directory containing the .csv files.
    // A single connection is thread-safe for use by several threads.
    Properties props = new Properties();
    props.put("suppressHeaders", "true");
    props.put("headerline", "E1\tE2\tSIM");
    props.put("separator", "\t");
    Connection conn = DriverManager.getConnection("jdbc:xbib:csv:" + args[0], props);// +"?separator=\t?suppressHeaders=True"); //?headerline=E1\tE2\tSIM

    // Create a Statement object to execute the query with.
    // A Statement is not thread-safe.
    Statement stmt = conn.createStatement();

    // Select the ID and NAME columns from sample.csv
    ResultSet results = stmt.executeQuery("SELECT E1 FROM results_0-1");

    // Dump out the results to a CSV file with the same format
    // using CsvJdbc helper function
    boolean append = true;
    //CsvDriver.writeToCsv(results, System.out, append);

    // Clean up
    conn.close();
    
    DemoDriver dr = new DemoDriver(args[0], "results_0-1");
    AnnotationComparison ac = new AnnotationComparison("http://dbpedia.org/resource/Igor_Milić/dump0", "http://dbpedia.org/resource/Igor_Milić/dump1");
    System.out.println(dr.getSimilarity(ac));
  }
}