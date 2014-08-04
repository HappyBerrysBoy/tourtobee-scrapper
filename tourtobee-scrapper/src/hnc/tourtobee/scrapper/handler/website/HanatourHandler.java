package hnc.tourtobee.scrapper.handler.website;

import hnc.tourtobee.scrapper.dataobject.Menu;
import hnc.tourtobee.scrapper.dataobject.Prd;
import hnc.tourtobee.scrapper.dataobject.PrdDtl;
import static hnc.tourtobee.code.Codes.PRD_CLASS;
import static hnc.tourtobee.code.Codes.ARPT_NAME_CODE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import jh.project.httpscrapper.Website;
import jh.project.httpscrapper.util.Html;

import org.apache.http.impl.client.CloseableHttpClient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class HanatourHandler extends _TouristAgencyHandler {

	@Override
	public ArrayList<Prd> scrapPrdList(CloseableHttpClient httpclient, Website website, HashMap<String, String> options, HashSet<String> insPrds) {
		ArrayList<Prd> prdList = new ArrayList<Prd>();
		ArrayList<Menu> pkgMenuList = this.scrapPkgMenuList(httpclient, website);
		
		for (Menu menu : pkgMenuList){
			Website prdListWebsite = website;
			prdListWebsite.setUrl("http://www.hanatour.com/asp/booking/productPackage/pk-11000-list.asp?" + menu.getMenuUrl().split("\\?")[1]);
			String jsonStr = this.getHtml(httpclient, prdListWebsite).trim();
			jsonStr = jsonStr.replaceFirst("fnSetMstList\\(", "");
			jsonStr = jsonStr.substring(0, jsonStr.length() - 1);
			Gson gson = new GsonBuilder().create();
			JsonPrds prds = gson.fromJson(jsonStr, JsonPrds.class);
			
			for (JsonPrds.Prd jsonPrd : prds.cont){
				Prd p = new Prd();
				p.setTagnId(website.getId());
				p.setPrdNo(jsonPrd.pkg_mst_code);
				p.setPrdNm(jsonPrd.mst_name);
				p.setPrdDesc(jsonPrd.t_content);
				p.setPrdDesc(PRD_CLASS.get("패키지"));
				p.setDmstDiv("A");
				p.setPrdUrl("http://www.hanatour.com/asp/booking/productPackage/pk-11001.asp?pkg_mst_code=" + p.getPrdNo());
				
				if (menu.getMenuCode().contains("지방출발")){
					p.setDepArpt(ARPT_NAME_CODE.get(menu.getMenuName().substring(0, 2)));
				}else{
					p.setDepArpt(ARPT_NAME_CODE.get("인천"));
				}
				
				p.setAreaList(this.getAreaList(p.getPrdNm() + " " + p.getPrdDesc(), menu.getMenuName()));
				
				prdList.add(p);
			}

		}
		return prdList;
	}
	
	
	
	
	@Override
	public ArrayList<PrdDtl> scrapPrdDtlSmmry(CloseableHttpClient httpclient, Website website, HashMap<String, String> options, Prd prd) {
		String url = "http://www.hanatour.com/asp/booking/productPackage/pk-11001-list.asp?"
					+ "pkg_mst_code=" + prd.getPrdNo()
					+ "tour_scheduled_year="
					+ "tour_scheduled_month=";
		return super.scrapPrdDtlSmmry(httpclient, website, options, prd);
	}
	
	
	
	
	private ArrayList<Menu> scrapPkgMenuList(CloseableHttpClient httpclient, Website website){
		ArrayList<Menu> menuList = new ArrayList<Menu>();
		Website menuSite = website;
		menuSite.setUrl("http://www.hanatour.com/asp/booking/oversea/oversea-main.asp?hanacode=main_q_pack");
		Html html = new Html(this.getHtml(httpclient, website));
		html = new Html(html.removeComment().getValueByClass("DepthGroup").toString().substring(2));
		
		while (html.getTag("div").toString().length() > 0){
			Html menuHtml = html.getTag("div");
			
			String menuCode = menuHtml.getTag("dt").getTag("a").findRegex("alt=[\"']+[^\"']*").toString();
			menuCode = menuCode.replaceAll("alt=[\"']+", "");
			
			Html subMenu = menuHtml.getTag("dd");
			while (subMenu.getTag("dl").toString().length() > 0 ){
				
				if (subMenu.getTag("dl").getTag("dt").convertSpecialChar().removeAllTags().toString().length() > 0){
					String menuName = subMenu.getTag("dl").getTag("dt").convertSpecialChar().removeAllTags().toString();
					String menuUrl = subMenu.getTag("dl").getTag("dt").findRegex("href=[\"']+[^\"']+").convertSpecialChar().toString();
					menuUrl = menuUrl.replaceAll("href=[\"']+", "");
					Menu m = new Menu();
					m.setMenuCode(menuCode);
					m.setMenuName(menuName);
					m.setMenuUrl(menuUrl);
					menuList.add(m);
				}else{
					Html subSubMenu = subMenu.getTag("dl");
					while (subSubMenu.getTag("dd").toString().length() > 0){
						if (subSubMenu.getTag("dd").getTag("strong").convertSpecialChar().toString().length() > 0){
							String menuName = subSubMenu.getTag("dd").getTag("strong").removeAllTags().convertSpecialChar().toString();
							String menuUrl = subSubMenu.getTag("dd").findRegex("http:\\/\\/[^\"']+").convertSpecialChar().toString();
							Menu m = new Menu();
							m.setMenuCode(menuCode);
							m.setMenuName(menuName);
							m.setMenuUrl(menuUrl);
							menuList.add(m);
						}
						subSubMenu = subSubMenu.removeTag("dd");
					}
				}
				
				subMenu = subMenu.removeTag("dl");
			}
			
			html = html.removeTag("div");
		}
		
		return menuList;
	}
	
	
	private class JsonPrds{
		private Head head;
		private ArrayList<Prd> cont;
		
		private class Head{
			private String tot_cnt;
			private String pub_area_code;
			private String pub_country;
			private String pub_city;
			private String dept_code;
			private String DY_LIST;
			private String page_num;
			private String page_len;
			private String flatfile_yn;
		}
		
		private class Prd{
			private String sort_no;
			private String pkg_mst_code;
			private String sMonth;
			private String min_amt;
			private String max_amt;
			private String mst_name;
			private String t_content;
			private String img_seq;
			private String dy_list;
			private String content;
			private String tour_day;
			private String start_dy;
			private String orderSeq;
		}
	}
	
	private class JsonPrdDtl{
		
	}
}
