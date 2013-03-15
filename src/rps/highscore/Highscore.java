package rps.highscore;

import java.sql.*;

/**
 * The highscore-class is the interface between java and mysql.
 *
 */
public class Highscore {
	
    private static final String PW = "TVf5s6DT";
    private static final String DB = "gdi1_project";
    private static final String USER = "group105_w";
    private static final String TABLE = "rps_highscore";
    private static final String URL = "sql219.your-server.de";
	private Connection conn;
    
	/**
	 * Constructor, which loads the mysql-driver and builds the connection to the server
	 */
    public Highscore() {
    	
    	try //load the mysql-driver
    	{ 
    	    Class.forName("com.mysql.jdbc.Driver"); 
    	} 
    	catch(ClassNotFoundException cnfe) 
    	{ 
    	    System.out.println("Treiber kann nicht geladen werden: "+cnfe.getMessage()); 
    	}
    	
    	try //build a connection to the server
    	{ 
    	    conn = DriverManager.getConnection("jdbc:mysql://" + URL + "/" + DB, USER, PW); 
    	} 
    	catch(SQLException sqle) 
    	{ 
    	    System.err.println("Verbindung ist fehlgeschlagen: " + sqle.getMessage()); 
    	}
    	
    }
    
    /**
     * Deconstructor, which closes the mysql-connection.
     */
    protected void finalize(){
    	
    	if (conn != null)
    	{
    	    try 
    	    { 
    	        conn.close(); 
    	    }
    	    catch(SQLException sqle) 
    	    { 
    	        System.err.println(sqle.getMessage()); 
    	    }
    	}
    	
    }
    
    /**
     * Inserts a new entry in the online highscores.
     * 
     * @param score The score 
     * @return true, if it worked 
     * @throws SQLException 
     */
    public boolean insertHighscore(Score score) throws SQLException{
    	
    	StringBuffer query = new StringBuffer();
    	
    	query.append("INSERT INTO ").append(TABLE);
    	query.append(" (nick, score, ai) VALUES (?, ?, ?);");
    	
    	
    	PreparedStatement stmt = conn.prepareStatement(query.toString());
    	stmt.setString(1, score.getNick());
    	stmt.setInt(2, score.getScore());
    	stmt.setString(3, score.getBeatenAi());
    	
    	int result = stmt.executeUpdate();
    	
    	return (result > 0);
    	
    }
    
    /**
     * Delivers the online highscore.
     * 
     * @return the highscore
     * @throws SQLException
     */
    public ResultSet getHighscore() throws SQLException{
    	
    	StringBuffer query = new StringBuffer();
    	
    	query.append("SELECT nick, score, ai FROM ").append(TABLE);
    	query.append(" ORDER BY score DESC, created_on ASC ");
    	query.append("LIMIT 10;");
    	
    	PreparedStatement stmt = conn.prepareStatement(query.toString());
    	
    	return stmt.executeQuery();
    	
    }
    
}
