package Server_v1;

import java.util.List;

public class MenuService {
	// menuboard 전체 출력
	public void showAllMenus(MenuDAO menuDAO) {
		List<MenuDTO> menuList = menuDAO.getAllMenus();
		for(MenuDTO mnDTO : menuList) {
    		System.out.println("======메뉴판======");
			System.out.println();
    		System.out.printf("메뉴 : "+mnDTO.getName()+" 가격 : "+mnDTO.getPrice()+"원");
		}
	}
}