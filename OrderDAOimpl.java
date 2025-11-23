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
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        	//손님의 주문 들어옴 -> 손님에게 주문번호 1 부여 -> 다음손님부터 번호 : 2,3,4,5...
        	pstmt.setString(1, order.getName());
        	pstmt.setString(2, order.getName());
            pstmt.executeUpdate();
            try(ResultSet generatedKeys = pstmt.getGeneratedKeys()){
            	if(generatedKeys.next()) {
            		int no = generatedKeys.getInt(1);
            		order.setNo(no);
            	}
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

