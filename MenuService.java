package Server_v1;

import java.io.PrintWriter;
import java.util.List;

public class MenuService {
	private final MenuDAO menuDAO;
	
	public MenuService(MenuDAO menuDAO) {
		this.menuDAO = menuDAO;
	}

	// menuboard 전체 출력
	public void showAllMenus(PrintWriter out) {
		List<MenuDTO> menuList = menuDAO.getAllMenus();
		//print -> out 형태로 바꿈 : out.print로 해야 메세지 오갈 수 있음
		out.println("======메뉴판======");
		out.println();
		for(MenuDTO mnDTO : menuList) {
    		out.printf("메뉴 : "+mnDTO.getName()+"| 가격 : "+mnDTO.getPrice()+"원\n");
		}
		out.println();
	}

}
