package Server_v1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class OrderDAOimpl {

    private final DatabaseConnector connector;

    public OrderDAOimpl(DatabaseConnector connector) {
        this.connector = connector;
    }
    public void CreateOrder(OrderDTO order) {
        String sql = "INSERT INTO ordermanagement (name) VALUES (?)";
        try (Connection conn = connector.getConnection();
             PreparedStatement pstmt =
                 conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        	pstmt.setString(1, order.getName());
            pstmt.executeUpdate();
            try(ResultSet generatedKeys = pstmt.getGeneratedKeys()){
            	if(generatedKeys.next()) {
            		int no = generatedKeys.getInt(1);
            		order.setNo(no);
            		System.out.println(no + "손님 추가 띠띠");
            	}
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
