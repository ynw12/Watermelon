package DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
	private static String dbIp = "172.30.1.87"; // ip 변경시 수정의 용이성을 위해 변수로 지정.
	private static final String URL = "jdbc:mysql://" + dbIp + ":3306/ordernpickup";
	// private static final String URL = "jdbc:mysql://192.168.0.78:3306/ordernpickup";
	private static final String USER = "watermelon";
	private static final String PASS = "wkvmtlf";
	/*static {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("드라이버 로드 실패", e);
        }
    }*/
	public Connection getConnection() throws SQLException{
		return DriverManager.getConnection(URL, USER, PASS);
	}
}
