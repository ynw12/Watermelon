package Service;

import java.io.PrintWriter;
import java.util.List;

import DB.MenuDAO;
import DB.MenuDTO;

public class MenuService {
	private final MenuDAO menuDAO;
	
	public MenuService(MenuDAO menuDAO) {
		this.menuDAO = menuDAO;
	}

	// menuboard 전체 출력
	public void showAllMenus(PrintWriter out) {
		List<MenuDTO> menuList = menuDAO.getAllMenus();

		out.println("======메뉴판======");
		out.println();
		for(MenuDTO mnDTO : menuList) {
    		out.printf("메뉴 : "+mnDTO.getName()+"| 가격 : "+mnDTO.getPrice()+"원\n");
		}
		out.println();
	}

}
