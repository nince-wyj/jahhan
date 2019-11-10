import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Test {

	public static void main(String[] args) {
		// Connecting
		try (Connection con = DriverManager.getConnection("jdbc:neo4j:bolt://192.168.164.129:10087", "neo4j", "ledwaf")) {

		    // Querying
		    String query = "MATCH (n:UserDemo) where n.user_id = 1 RETURN n LIMIT 1";
		    try(Statement stmt = con.createStatement())
			{
			      ResultSet rs = stmt.executeQuery(query);
			      while(rs.next())
			      {
				    System.out.println(rs.getString("p.age")+":"+rs.getString("p.school"));
			      }
		         }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
