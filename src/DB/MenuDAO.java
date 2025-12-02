package DB;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MenuDAO {
	
	private final DatabaseConnector connector; 

    public MenuDAO(DatabaseConnector connector) { 
        this.connector = connector;
    }
    // Read : menuboard 테이블의 전체 data select
    public List<MenuDTO> getAllMenus() {
        String selectsql = "Select * from menuboard";
        List<MenuDTO> menuList = new ArrayList<>();
        
        try (Connection conn = connector.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(selectsql)){
               
               while(rs.next()) {
                   MenuDTO menu = new MenuDTO();
                   menu.setName(rs.getString("name")); 
                   menu.setPrice(rs.getInt("price"));
                   menuList.add(menu);
                   System.out.println("메뉴판 읽어오는 중...");
               }
           } catch (SQLException e) {
               System.out.println("[MenuDAO] menuboard 테이블 조회 중 DB 오류 발생");
               e.printStackTrace();
           }
           return menuList;
    }
    
//    // 2. Create : menuboard 테이블에 Insert  
//    // -> 메뉴 변동 없이 진행할 예정이므로 메뉴 추가가 필요할 떄 더 구체적으로 디벨롭하겠습니당
//    public void CreateMenu(MenuDTO menuboard) {
//    	String insertsql = "INSERT INTO menuboard (name, price) VALUES (?, ?)";
//    	try (Connection conn = connector.getConnection();
//    			PreparedStatement pstmt = conn.prepareStatement(insertsql);){
//    		
//    		pstmt.setString(1, menuboard.getName());
//    		pstmt.setInt(2, menuboard.getPrice());
//    		pstmt.executeUpdate();
//    		System.out.println("메뉴 추가 성공!");
//    	} catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
}

