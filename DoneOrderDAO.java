package Server_v1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DoneOrderDAO {
	private final DatabaseConnector connector; 
	public DoneOrderDAO(DatabaseConnector connector) {
		this.connector = connector;
	}
	// Read : doneorder 테이블의 완료된 번호,주문들 select
	public List<DoneOrderDTO> getAllDoneOrders() { 
    	String selectsql = "Select doneno, donename from doneorder";
    	List<DoneOrderDTO> doneOrderList = new ArrayList<>();
    	
    	try (Connection conn = connector.getConnection();
    			Statement stmt = conn.createStatement();
    			ResultSet rs = stmt.executeQuery(selectsql);){
        	
        	while(rs.next()) {
        		DoneOrderDTO DoneOrders = new DoneOrderDTO();
        		DoneOrders.setDoneNo(rs.getInt("doneno"));
        		DoneOrders.setDoneName(rs.getString("donename"));
        		doneOrderList.add(DoneOrders);
        		System.out.println("주문완료 내역을 읽어오는 중...");
        	}
        } catch (SQLException e) {
               e.printStackTrace();
               System.out.println("[DoneOrderDAO] DoneOrder 테이블 조회 중 DB 오류 발생");
        }
    	return doneOrderList;
    }

    // Create : DoneOrder 테이블에 data insert 
    public void insertDoneOrder(DoneOrderDTO doneOrderDto) {
    	String insertSql = "Insert into doneorder (doneno, donename) VALUES (?, ?)";
    	try (Connection conn = connector.getConnection();
    			PreparedStatement pstmt = conn.prepareStatement(insertSql);){
    		pstmt.setInt(1, doneOrderDto.getDoneNo());
    		pstmt.setString(2, doneOrderDto.getDoneName());
    		
    		pstmt.executeUpdate();
    		System.out.println("주문완료 내역이 추가 되었습니다!");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("[DoneOrderDAO] DoneOrder에 주문완료내역 추가 실패");
		}	
    }
    
    // Delete : DoneOrder 테이블에 data delete
    public void deleteDoneOrder(int no) {
    	String deleteSql = "delete from doneorder where doneno = ?";

		try (Connection conn = connector.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(deleteSql);){
			pstmt.setInt(1, no);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("[DoneOrderDAO] DoneOrder에 주문완료내역 삭제 실패");
		}
    }   
}

