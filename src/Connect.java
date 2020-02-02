import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

public class Connect {
	public ResultSet rs;
	Statement st;
	Connection con;
	PreparedStatement pStat;
	ResultSetMetaData rsm;

	public Connect() {
		// TODO Auto-generated constructor stub
		try {
			Class.forName("com.mysql.jdbc.Driver");

            // Nanti prk nya diubah sesuai dengan nama db yang mau di connect
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/guieditor","root","");  
            st = con.createStatement();  
            
            System.out.println("Connected to the database..");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Error Connection");
		}
	}

	public ResultSet executeQuery(String query) {
		try {
			rs = st.executeQuery(query);
			rsm = rs.getMetaData();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Error Connection RS");
		}
		return rs;
	}

//	public void executeUpdate(String query) {
//		try {
//			st.executeUpdate(query);
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//	public void executePStatement(String query) {
//		try {
//			pStat = con.prepareStatement(query);
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			System.out.println("Error Connection PSTATEMENT");
//		}
//	}

	public void executeInsertToFiles(String name) {
		try {
			pStat = con
					.prepareStatement("INSERT INTO files (name) VALUES (?)");
			pStat.setString(1, name);
			pStat.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void executeInsertToDetails(int id,String componentName, int x, int y,String value) {
		try {
			pStat = con
					.prepareStatement("INSERT INTO details (fileId,componentName,x,y,value) VALUES(?, ?, ?, ?, ?)");
			pStat.setInt(1, id);
			pStat.setString(2, componentName);
			pStat.setInt(3, x);
			pStat.setInt(4, y);
			pStat.setString(5, value);
			pStat.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void executeDeleteDetails(int id){
    	try {
			pStat = con.prepareStatement("DELETE FROM details WHERE fileId=?");
			pStat.setInt(1,id);
			pStat.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
