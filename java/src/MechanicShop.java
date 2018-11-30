/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class MechanicShop{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public MechanicShop(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + MechanicShop.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		MechanicShop esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new MechanicShop (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. AddCustomer");
				System.out.println("2. AddMechanic");
				System.out.println("3. AddCar");
				System.out.println("4. InsertServiceRequest");
				System.out.println("5. CloseServiceRequest");
				System.out.println("6. ListCustomersWithBillLessThan100");
				System.out.println("7. ListCustomersWithMoreThan20Cars");
				System.out.println("8. ListCarsBefore1995With50000Milles");
				System.out.println("9. ListKCarsWithTheMostServices");
				System.out.println("10. ListCustomersInDescendingOrderOfTheirTotalBill");
				System.out.println("11. < EXIT");
				
				/*
				 * FOLLOW THE SPECIFICATION IN THE PROJECT DESCRIPTION
				 */
				switch (readChoice()){
					case 1: AddCustomer(esql); break;
					case 2: AddMechanic(esql); break;
					case 3: AddCar(esql); break;
					case 4: InsertServiceRequest(esql); break;
					case 5: CloseServiceRequest(esql); break;
					case 6: ListCustomersWithBillLessThan100(esql); break;
					case 7: ListCustomersWithMoreThan20Cars(esql); break;
					case 8: ListCarsBefore1995With50000Milles(esql); break;
					case 9: ListKCarsWithTheMostServices(esql); break;
					case 10: ListCustomersInDescendingOrderOfTheirTotalBill(esql); break;
					case 11: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye!");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice
	
	public static void AddCustomer(MechanicShop esql){//1	
		String fname, lname, phone = "", address;
		int id; //Customer PRIMARY KEY

		boolean correctFormat = false;

		try{
			//auto increment Customer id
			String query = "SELECT id FROM Customer ORDER BY id DESC LIMIT 1";
			List<List<String>> custID = esql.executeQueryAndReturnResult(query);
			id = Integer.parseInt(custID.get(0).get(0)) + 1;
			//Get customer info
			System.out.print("Customer's first name: ");
			fname = in.readLine();
			System.out.print("Customer's last name: ");
			lname = in.readLine();
			System.out.print("Customer's phone number (xxx)xxx-xxxx: ");
			while(!correctFormat){
				phone = in.readLine();
				//TODO Check if chars entered are numeric
				if(phone.length() != 13 || phone.charAt(0) != '(' || phone.charAt(4) != ')' || phone.charAt(8) != '-'){
					System.out.print("Enter the customer's phone number in the format (xxx)xxx-xxxx: ");
				}
				else{
					correctFormat = true;
				}
			}
			System.out.print("Customer's address: ");
			address = in.readLine();
			//Store info in DB
			query = "INSERT INTO Customer(id, fname, lname, phone, address) VALUES (" + id + ", '" + fname + "', '" + lname + "', '" + phone + "', '" + address + "')";
			esql.executeUpdate(query);			
		}catch(Exception e){
			System.err.println(e.getMessage());
		}
		
	}
	
	public static void AddMechanic(MechanicShop esql){//2
		String fname, lname, experience = "-1";
		int id; //Mechanic PRIMARY KEY

		//To check if a proper value for 'experience' has been given
		boolean correctDomain = false;
		int checkYears;

		try{
			//auto increment Mechanic id
			String query = "SELECT id FROM Mechanic ORDER BY id DESC LIMIT 1";
			List<List<String>> mechID = esql.executeQueryAndReturnResult(query);
			id = Integer.parseInt(mechID.get(0).get(0)) + 1;
			//Get mechanic info
			System.out.print("Mechanic's first name: ");
			fname = in.readLine();
			System.out.print("Mechanic's last name: ");
			lname = in.readLine();
			System.out.print("Mechanic's years of experience: ");
			//Check if experience is in its declared domain
			while(!correctDomain){
				try{
					experience = in.readLine();
					checkYears = Integer.parseInt(experience);
				}catch(NumberFormatException e){
					System.out.println("Value entered must be an integer");
					System.out.print("Mechanic's years of experience: ");
					continue;
				}
				if(checkYears < 0 || checkYears >= 100){
					System.out.print("Enter a value greater than or equal to 0 or less than 100: ");
				}
				else{
					correctDomain = true;
				}
			}
			//Store info in DB
			query = "INSERT INTO Mechanic(id, fname, lname, experience) VALUES (" + id + ", '" + fname + "', '" + lname + "', '" + experience + "')";
			esql.executeUpdate(query);
			//
			query = "SELECT * FROM Mechanic WHERE id = 250";
			esql.executeQueryAndPrintResult(query);

			//
		}catch(Exception e){
			System.err.println(e.getMessage());
		}
	}
	
	public static void AddCar(MechanicShop esql){//3
		//VIN is Car PRIMARY KEY
		String make, model, year = "-1", vin;

		//To check if a proper value for 'year' has been given
		boolean correctDomain = false;
		int checkYear;

		try{
			//Get car info
			System.out.print("Car's VIN: ");
			vin = in.readLine();
			System.out.print("Car's make: ");
			make = in.readLine();
			System.out.print("Car's model: ");
			model = in.readLine();
			System.out.print("Car's year: ");
			//Check if year is in its declared domain
			while(!correctDomain){
				try{
					year = in.readLine();
					checkYear = Integer.parseInt(year);
				}catch(NumberFormatException e){
					System.out.println("Value entered must be an integer");
					System.out.print("Car's year: ");
					continue;
				}
				if(checkYear < 1970){
					System.out.print("Car's year must be greater than or equal to 1970: ");
				}
				else{
					correctDomain = true;
				}
			}
			//Store info in DB
			String query = "INSERT INTO Car(vin, make, model, year) VALUES (" + vin + ", '" + make + "', '" + model + "', '" + year + "')";
			esql.executeUpdate(query);			
		}catch(Exception e){
			System.err.println(e.getMessage());
		}
	}
	
	public static void InsertServiceRequest(MechanicShop esql){//4
		boolean found = true; //Assume we will find a customer with a matching last name

		try{
			System.out.print("Enter customer's last name: ");
			String lname = in.readLine();
			//ILIKE is a case-insensitive LIKE
			String query = "SELECT fname, phone, address FROM Customer WHERE lname = '" + lname + "'";
			List<List<String>> potentialCustomers = esql.executeQueryAndReturnResult(query);
			if(potentialCustomers.size() == 0){
				found = false;
				System.out.println("Sorry, we couldn't find any customers with that last name");
			}
			//If more than one customer with same last name
			else if(potentialCustomers.size() > 1){
				//Print out all the different customers with the same last name
				for(int i = 0; i < potentialCustomers.size(); ++i){
					System.out.println(Integer.toString(i + 1) + ". First Name: " + potentialCustomers.get(i).get(0) + ", Phone Number: " + potentialCustomers.get(i).get(1) + ", Address: " + potentialCustomers.get(i).get(2));
				}
				System.out.println("Choose which customer initiated the service request");
				String choice = in.readLine();//TODO Input error checking
			}
			else if(potentialCustomers.size() == 1){
				System.out.print("TODO");
			}
		}catch(Exception e){
			System.err.println(e.getMessage());
		}
	}
	
	public static void CloseServiceRequest(MechanicShop esql) throws Exception{//5
		
	}
	
	public static void ListCustomersWithBillLessThan100(MechanicShop esql){//6
		
	}
	
	public static void ListCustomersWithMoreThan20Cars(MechanicShop esql){//7
		
	}
	
	public static void ListCarsBefore1995With50000Milles(MechanicShop esql){//8
		
	}
	
	public static void ListKCarsWithTheMostServices(MechanicShop esql){//9
		//
		
	}
	
	public static void ListCustomersInDescendingOrderOfTheirTotalBill(MechanicShop esql){//9
		//
		
	}
	
}
