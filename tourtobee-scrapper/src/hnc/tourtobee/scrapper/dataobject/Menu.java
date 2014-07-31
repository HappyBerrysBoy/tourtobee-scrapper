package hnc.tourtobee.scrapper.dataobject;

import java.util.HashMap;
import java.util.HashSet;

public class Menu {
	private String menuCode;
	private String menuName;
	private String menuUrl;
	private HashSet<String> prdUrls;
//	private HashMap<String, String> menuNameByPrdUrl;
//	private HashMap<String, String> menuCodeByPrdUrl;
	
	
	
	public Menu() {
		prdUrls = new HashSet<String>();
//		menuNameByPrdUrl = new HashMap<String, String>();
//		menuCodeByPrdUrl = new HashMap<String, String>();;
	}
	
	
	public String getMenuCode() {
		return menuCode;
	}
	public void setMenuCode(String menuCode) {
		this.menuCode = menuCode;
	}
	public String getMenuName() {
		return menuName;
	}
	public void setMenuName(String menuName) {
		this.menuName = menuName;
	}
	public String getMenuUrl() {
		return menuUrl;
	}
	public void setMenuUrl(String menuUrl) {
		this.menuUrl = menuUrl;
	}
	public HashSet<String> getPrdUrls() {
		return prdUrls;
	}
	public void setPrdUrls(HashSet<String> prdUrls) {
		this.prdUrls = prdUrls;
	}
//	public HashMap<String, String> getMenuNameByPrdUrl() {
//		return menuNameByPrdUrl;
//	}
//	public void setMenuNameByPrdUrl(HashMap<String, String> menuNameByPrdUrl) {
//		this.menuNameByPrdUrl = menuNameByPrdUrl;
//	}
//	public HashMap<String, String> getMenuCodeByPrdUrl() {
//		return menuCodeByPrdUrl;
//	}
//	public void setMenuCodeByPrdUrl(HashMap<String, String> menuCodeByPrdUrl) {
//		this.menuCodeByPrdUrl = menuCodeByPrdUrl;
//	}
	
	
	
}
