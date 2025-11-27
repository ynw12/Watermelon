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
    public void moveOrderToDone(int no) {
        String insertSql = "INSERT INTO doneorder (doneno, donename) " + "SELECT no, name FROM ordermanagement WHERE no = ?";

        try (Connection conn = connector.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement insertPstmt = conn.prepareStatement(insertSql);) {
                // ordermanagement에서 doneorder로 data 복사 
                insertPstmt.setInt(1, no);
                insertPstmt.executeUpdate();

                conn.commit(); //db 이동 성공 시 그대로 확정
            } catch (SQLException e) {
                conn.rollback(); //실패하면 원래대로 돌아가기
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

	public OrderDTO getOrderByNo(int no) {  
        String sql = "SELECT no, name, status FROM ordermanagement WHERE no = ?";

        try (Connection conn = connector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, no);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    OrderDTO dto = new OrderDTO();
                    dto.setNo(rs.getInt("no"));
                    dto.setName(rs.getString("name"));
                    dto.setStatus(rs.getString("status"));
                    return dto;
                } else {
                    return null; // 조회 결과 없음
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[OrderDAO] ordermanagement 조회 중 DB 오류 발생");
            return null;
        } // Update : OrderManagement 테이블에 status update
   
    }
    
    // Update : OrderManagement 테이블에 status update
    // 1. WAITING -> DONE
    public void updateStatustoDone(int no) {
    	String updateSql = "update ordermanagement set status = ? where no = ?";

		try (Connection conn = connector.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(updateSql);){
			pstmt.setString(1, "DONE");
			pstmt.setInt(2, no);
			pstmt.executeUpdate();
			System.out.println("주문번호 "+no+"번의 상태가 DONE으로 업데이트 되었습니다.");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("[OrderDAO] StatustoDone 업데이트 실패");
		}
    } 
    
    // 2. DONE -> PICKUP
    public void updateStatustoPickUp(int no) {
    	String updateSql = "update ordermanagement set status = ? where no = ?";

		try (Connection conn = connector.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(updateSql);){
			pstmt.setString(1, "PICKUP");
			pstmt.setInt(2, no);
			pstmt.executeUpdate();
			System.out.println("주문번호 "+no+"번의 상태가 PICKUP으로 업데이트 되었습니다.");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("[OrderDAO] StatustoPickUp 업데이트 실패");
		}
    }   
}





