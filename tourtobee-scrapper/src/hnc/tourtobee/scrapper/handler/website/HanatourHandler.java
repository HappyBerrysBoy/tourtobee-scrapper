package hnc.tourtobee.scrapper.handler.website;

import hnc.tourtobee.scrapper.dataobject.Menu;
import hnc.tourtobee.scrapper.dataobject.Prd;
import hnc.tourtobee.scrapper.dataobject.PrdDtl;
import static hnc.tourtobee.code.Codes.PRD_CLASS;
import static hnc.tourtobee.code.Codes.ARPT_NAME_CODE;
import static hnc.tourtobee.code.Codes.PRD_STATUS;
import static hnc.tourtobee.code.Codes.WEEK_DAY_NUMBER;

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
		ArrayList<Menu> hnmnMenuList = this.scrapHnmnMenuList(httpclient, website);
		ArrayList<Menu> golfMenuList = this.scrapGolfMenuList(httpclient, website);
		ArrayList<Menu> cruiseMenuList = this.scrapCruiseMenuList(httpclient, website);
		ArrayList<Menu> jejuMenuList = this.scrapJejuMenuList(httpclient, website);
		
		for(int i = 0 ; i < 5 ; i++){
			ArrayList<Menu> menuList = new ArrayList<Menu>();
			if(i == 0){
				menuList = pkgMenuList;
			}else if(i == 1){
				menuList = hnmnMenuList;
			}else if(i == 2){
				menuList = golfMenuList;
			}else if(i == 3){
				menuList = cruiseMenuList;
			}else if(i == 4){
				menuList = jejuMenuList;
			}
			
			for(Menu menu : menuList){
				Website prdListWebsite = website;
				String url = "http://www.hanatour.com/asp/booking/productPackage/pk-11000-list.asp?" + menu.getMenuUrl().split("\\?")[1];
					
				prdListWebsite.setUrl(url);
				String jsonStr = this.getHtml(httpclient, prdListWebsite).trim();
				jsonStr = jsonStr.replaceFirst("fnSetMstList\\(", "");
				jsonStr = jsonStr.substring(0, jsonStr.length() - 1);
				Gson gson = new GsonBuilder().create();
				JsonPrds prds = gson.fromJson(jsonStr, JsonPrds.class);
				
				for (JsonPrds.Cont jsonPrd : prds.cont){
					Prd p = new Prd();
					p.setTagnId(website.getId());
					p.setPrdNo(jsonPrd.pkg_mst_code);
					if (insPrds != null && insPrds.contains(p.getPrdNo())) continue;
					p.setPrdNm(jsonPrd.mst_name);
					p.setPrdDesc(jsonPrd.t_content);
					
					p.setPrdUrl("http://www.hanatour.com/asp/booking/productPackage/pk-11001.asp?pkg_mst_code=" + p.getPrdNo());
					
					if(i == 0){
						p.setPrdDesc(PRD_CLASS.get("패키지"));
						p.setDmstDiv("A");
						if (menu.getMenuCode().contains("지방출발")){
							p.setDepArpt(ARPT_NAME_CODE.get(menu.getMenuName().substring(0, 2)));
						}else{
							p.setDepArpt(ARPT_NAME_CODE.get("인천"));
						}
					}else if(i == 1){
						p.setPrdDesc(PRD_CLASS.get("허니문"));
						p.setDmstDiv("A");
						if (menu.getMenuCode().contains("지방출발")){
							p.setDepArpt(ARPT_NAME_CODE.get(menu.getMenuName().substring(0, 2)));
						}else{
							p.setDepArpt(ARPT_NAME_CODE.get("인천"));
						}
					}else if(i == 2){
						p.setPrdDesc(PRD_CLASS.get("골프"));
						p.setDmstDiv("A");
						if (menu.getMenuCode().contains("지방출발")){
							p.setDepArpt(ARPT_NAME_CODE.get(menu.getMenuName().substring(0, 2)));
						}else{
							p.setDepArpt(ARPT_NAME_CODE.get("인천"));
						}
					}else if(i == 3){
						p.setPrdDesc(PRD_CLASS.get("크루즈"));
						p.setDmstDiv("A");
						if (menu.getMenuCode().contains("지방출발")){
							p.setDepArpt(ARPT_NAME_CODE.get(menu.getMenuName().substring(0, 2)));
						}else{
							p.setDepArpt(ARPT_NAME_CODE.get("인천"));
						}
					}else if(i == 4){
						p.setDmstDiv("D");
						if (menu.getMenuName().equals("허니문")){
							p.setPrdDesc(PRD_CLASS.get("허니문"));
						}else{
							p.setPrdDesc(PRD_CLASS.get("국내"));
							if (!menu.getMenuName().contains("세미패키지")){
								p.setDepArpt(ARPT_NAME_CODE.get(menu.getMenuName().substring(0, 2)));
							}else{
								p.setDepArpt(ARPT_NAME_CODE.get("인천"));
							}
						}
					}
					
					p.setAreaList(this.getAreaList(p.getPrdNm() + " " + p.getPrdDesc(), menu.getMenuName()));
					
					prdList.add(p);
				}
			}
		}
		return prdList;
	}
	
	
	
	
	@Override
	public ArrayList<PrdDtl> scrapPrdDtlSmmry(CloseableHttpClient httpclient, Website website, HashMap<String, String> options, Prd prd) {
		HashSet<String> monthSet = this.getMonthSet(options);
		ArrayList<PrdDtl> prdDtlList = new ArrayList<PrdDtl>();
		
		for (String month : monthSet){
			String url = "http://www.hanatour.com/asp/booking/productPackage/pk-11001-list.asp?"
					+ "pkg_mst_code=" + prd.getPrdNo()
					+ "&tour_scheduled_year=" + month.substring(0, 4)
					+ "&tour_scheduled_month=" + month.substring(4, 6);
			Website prdDtlWebsite = website;
			prdDtlWebsite.setUrl(url);
			String jsonStr = this.getHtml(httpclient, prdDtlWebsite).trim();
			jsonStr = jsonStr.replaceFirst("fnSetPkgSchedule\\(", "");
			jsonStr = jsonStr.substring(0, jsonStr.length() - 1);
			Gson gson = new GsonBuilder().create();
			JsonPrdDtl prdDtls = gson.fromJson(jsonStr, JsonPrdDtl.class);
			
			for (JsonPrdDtl.Cont jsonPrdDtl : prdDtls.cont){
				
				String prdSeq = jsonPrdDtl.pcode;
				String prdDtlNm = jsonPrdDtl.pname;
				String depDt = month.substring(0, 4) + jsonPrdDtl.sdate.replaceAll("[ \\/\\(\\):월화수목금토일]", "");
				String depDtYmd = depDt.substring(0, 8);
				String depDtHm = depDt.substring(8, 12);
				String depDtWd = jsonPrdDtl.sdate.substring(7, 8);
				String arrDt = "";
				if (Integer.parseInt(jsonPrdDtl.sdate.substring(0, 2)) > Integer.parseInt(jsonPrdDtl.adate.substring(0, 2))){
					arrDt = String.valueOf(Integer.parseInt(month.substring(0, 4)) + 1 );
				}else{
					arrDt = String.valueOf(Integer.parseInt(month.substring(0, 4)));
				}
				arrDt = arrDt + jsonPrdDtl.adate.replaceAll("[ \\/\\(\\):월화수목금토일]", "");
				String arrDtYmd = arrDt.substring(0, 8);
				String arrDtHm = arrDt.substring(8, 12);
				String arrDtWd = jsonPrdDtl.adate.substring(7, 8);
				
				String prdSt = "";
				if (jsonPrdDtl.lminute.equals("0")){
					prdSt = PRD_STATUS.get("예약마감");
				}else if (jsonPrdDtl.lminute.equals("1")){
					prdSt = PRD_STATUS.get("예약가능");
				}else if (jsonPrdDtl.lminute.equals("2")){
					prdSt = PRD_STATUS.get("출발확정");
				}else{
					prdSt = PRD_STATUS.get("예약가능");
				}
				
				String prdFeeAd = jsonPrdDtl.amt;
				String prdUrl = "http://www.hanatour.com/asp/booking/productPackage/pk-12000.asp?"
								+ "pkg_code=" + prdSeq
								+ "&promo_doumi_code=";
				
				PrdDtl prdDtl = new PrdDtl();
				prdDtl.setTagnId(website.getId());
				prdDtl.setPrdNo(prd.getPrdNo());
				prdDtl.setPrdSeq(prdSeq);
				prdDtl.setPrdDtlNm(prdDtlNm);
				prdDtl.setDepDt(depDt);
				prdDtl.setDepDtYmd(depDtYmd);
				prdDtl.setDepDtHm(depDtHm);
				prdDtl.setDepDtWd(WEEK_DAY_NUMBER.get(depDtWd));
				prdDtl.setArrDt(arrDt);
				prdDtl.setArrDtYmd(arrDtYmd);
				prdDtl.setArrDtHm(arrDtHm);
				prdDtl.setArrDtWd(WEEK_DAY_NUMBER.get(arrDtWd));
				prdDtl.setDepArpt(prd.getDepArpt());
				prdDtl.setArlnId(jsonPrdDtl.acode);
				prdDtl.setPrdSt(prdSt);
				prdDtl.setPrdFeeAd(prdFeeAd);
				prdDtl.setPrdUrl(prdUrl);
				
				prdDtlList.add(prdDtl);
			}
		}
		return prdDtlList;
	}
	
	
	
	
	private ArrayList<Menu> scrapPkgMenuList(CloseableHttpClient httpclient, Website website){
		ArrayList<Menu> menuList = new ArrayList<Menu>();
		Website menuSite = website;
		menuSite.setUrl("http://www.hanatour.com/asp/booking/oversea/oversea-main.asp?hanacode=main_q_pack");
		Html html = new Html(this.getHtml(httpclient, website));
		html = new Html(html.removeComment().getValueByClass("DepthGroup").toString().substring(2));
		
		while (html.getTag("div").toString().length() > 0){
			Html menuHtml = html.getTag("div");
			html = html.removeTag("div");
			
			String menuCode = menuHtml.getTag("dt").getTag("a").findRegex("alt=[\"']+[^\"']*").toString();
			menuCode = menuCode.replaceAll("alt=[\"']+", "");
			
			Html subMenu = menuHtml.getTag("dd");
			while (subMenu.getTag("dl").toString().length() > 0 ){
				
				if (subMenu.getTag("dl").getTag("dt").convertSpecialChar().removeAllTags().toString().length() > 0){
					String menuName = subMenu.getTag("dl").getTag("dt").convertSpecialChar().removeAllTags().toString();
					String menuUrl = subMenu.getTag("dl").getTag("dt").findRegex("href=[\"']+[^\"']+").convertSpecialChar().toString();
					menuUrl = menuUrl.replaceAll("href=[\"']+", "").replaceAll("%[\\s\\S^]{5}", "");
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
							String menuUrl = subSubMenu.getTag("dd").findRegex("http:\\/\\/[^\"']+").convertSpecialChar().toString().replaceAll("%[\\s\\S^]{5}", "");
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
		}
		
//		for(Menu menu : menuList){
//			System.out.println(menu.getMenuCode() + " / " + menu.getMenuName() + " / " + menu.getMenuUrl());
//		}
//		menuList = new ArrayList<Menu>();
		return menuList;
	}
	
	
	private ArrayList<Menu> scrapHnmnMenuList(CloseableHttpClient httpclient, Website website){
		ArrayList<Menu> menuList = new ArrayList<Menu>();
		Website menuSite = website;
		menuSite.setUrl("http://www.hanatour.com/asp/booking/honeymoon/hr-main.asp");
		Html html = new Html(this.getHtml(httpclient, website));
		html = new Html(html.removeComment().getValueByClass("DepthGroup").toString().substring(2));
		
		while (html.getTag("div").toString().length() > 0){
			Html menuHtml = html.getTag("div");
			html = html.removeTag("div");
			String menuCode = menuHtml.getTag("dt").getTag("a").findRegex("alt=[\"']+[^\"']*").toString();
			menuCode = menuCode.replaceAll("alt=[\"']+", "");
			String menuName = menuCode;
			String menuUrl = "";
			
			if (menuCode.equals("더보기") || menuCode.equals("H웨딩")){
				continue;
			}else if (menuCode.equals("지방출발")){
				Html subMenuHtml = menuHtml.getTag("dd").getTag("div");
				while (subMenuHtml.getTag("dl").toString().length() > 0){
					Html subSubMenuHtml = subMenuHtml.getTag("dl");
					subMenuHtml = subMenuHtml.removeTag("dl");
					menuName = subSubMenuHtml.getTag("dt").removeAllTags().removeComment().toString();
					while(subSubMenuHtml.getTag("dd").toString().length() > 0){
						menuUrl = subSubMenuHtml.getTag("dd").findRegex("http:\\/\\/[^\"']+").convertSpecialChar().toString().replaceAll("%[\\s\\S^]{5}", "");
						Menu m = new Menu();
						m.setMenuCode(menuCode);
						m.setMenuName(menuName);
						m.setMenuUrl(menuUrl);
						menuList.add(m);
						subSubMenuHtml = subSubMenuHtml.removeTag("dd");
					}
				}
			}else{
				menuUrl = menuHtml.getTag("dt").getTag("a").findRegex("href=[\"']+[^\"']+").convertSpecialChar().toString();
				menuUrl = menuUrl.replaceAll("href=[\"']+", "").replaceAll("%[\\s\\S^]{5}", "");
				Menu m = new Menu();
				m.setMenuCode(menuCode);
				m.setMenuName(menuName);
				m.setMenuUrl(menuUrl);
				menuList.add(m);
			}
		}
//		for(Menu menu : menuList){
//			System.out.println(menu.getMenuCode() + " / " + menu.getMenuName() + " / " + menu.getMenuUrl());
//		}
//		menuList = new ArrayList<Menu>();
		return menuList;
	}
	
	
	private ArrayList<Menu> scrapGolfMenuList(CloseableHttpClient httpclient, Website website){
		ArrayList<Menu> menuList = new ArrayList<Menu>();
		Website menuSite = website;
		menuSite.setUrl("http://www.hanatour.com/asp/booking/golf/golf-main.asp");
		Html html = new Html(this.getHtml(httpclient, website));
		html = new Html(html.removeComment().getValueByClass("DepthGroup").toString().substring(2));
		
		while (html.getTag("div").toString().length() > 0){
			Html menuHtml = html.getTag("div");
			html = html.removeTag("div");
			
			String menuCode = menuHtml.getTag("dt").getTag("a").findRegex("alt=[\"']+[^\"']*").toString();
			menuCode = menuCode.replaceAll("alt=[\"']+", "");
			if (menuCode.equals("국내골프")) continue;
			
			Html subMenu = menuHtml.getTag("dd");
			while (subMenu.getTag("dl").toString().length() > 0 ){
				
				String menuName = subMenu.getTag("dl").getTag("dt").convertSpecialChar().removeAllTags().toString();
				String menuUrl = subMenu.getTag("dl").getTag("dt").findRegex("href=[\"']+[^\"']+").convertSpecialChar().toString();
				menuUrl = menuUrl.replaceAll("href=[\"']+", "").replaceAll("%[\\s\\S^]{5}", "");
				Menu m = new Menu();
				m.setMenuCode(menuCode);
				m.setMenuName(menuName);
				m.setMenuUrl(menuUrl);
				menuList.add(m);
				subMenu = subMenu.removeTag("dl");
			}
		}
//		for(Menu menu : menuList){
//			System.out.println(menu.getMenuCode() + " / " + menu.getMenuName() + " / " + menu.getMenuUrl());
//		}
//		menuList = new ArrayList<Menu>();
		return menuList;
	}
	
	
	private ArrayList<Menu> scrapCruiseMenuList(CloseableHttpClient httpclient, Website website){
		ArrayList<Menu> menuList = new ArrayList<Menu>();
		Website menuSite = website;
		menuSite.setUrl("http://www.hanatour.com/asp/booking/cruise/cruise-main.asp");
		Html html = new Html(this.getHtml(httpclient, website));
		html = new Html(html.removeComment().getValueByClass("DepthGroup").toString().substring(2));
		
		while (html.getTag("div").toString().length() > 0){
			Html menuHtml = html.getTag("div");
			html = html.removeTag("div");
			
			String menuCode = menuHtml.getTag("dt").getTag("a").findRegex("alt=[\"']+[^\"']*").toString();
			menuCode = menuCode.replaceAll("alt=[\"']+", "");
			if (menuCode.equals("크루즈안내")) continue;
			String menuName = menuCode;
			String menuUrl = menuHtml.getTag("dt").getTag("a").findRegex("href=[\"']+[^\"']+").convertSpecialChar().toString().trim();
			
			Html subMenu = menuHtml.getTag("dd").getTag("dl");
			if (subMenu.toString().length() > 0){
				while (subMenu.getTag("dd").toString().length() > 0 ){
					Html subSubMenu = subMenu.getTag("dd");
					subMenu = subMenu.removeTag("dd");
					menuName = subSubMenu.removeAllTags().convertSpecialChar().toString().trim();
					menuUrl = subSubMenu.findRegex("http:\\/\\/[^\"']+").convertSpecialChar().toString().replaceAll("%[\\s\\S^]{5}", "");
					Menu m = new Menu();
					m.setMenuCode(menuCode);
					m.setMenuName(menuName);
					m.setMenuUrl(menuUrl);
					menuList.add(m);
				}
			}else{
				Menu m = new Menu();
				m.setMenuCode(menuCode);
				m.setMenuName(menuName);
				m.setMenuUrl(menuUrl);
				menuList.add(m);
			}
		}
//		for(Menu menu : menuList){
//			System.out.println(menu.getMenuCode() + " / " + menu.getMenuName() + " / " + menu.getMenuUrl());
//		}
//		menuList = new ArrayList<Menu>();
		return menuList;
	}
	
	
	private ArrayList<Menu> scrapJejuMenuList(CloseableHttpClient httpclient, Website website){
		ArrayList<Menu> menuList = new ArrayList<Menu>();
		String menuCode = "제주여행";
		
		Menu m1 = new Menu();
		m1.setMenuCode(menuCode);
		m1.setMenuName("서울출발");
		m1.setMenuUrl("http://www.hanatour.com/asp/booking/productPackage/pk-11000.asp?area=K&pub_country=KR&pub_city=CJU&start_city=AF9&etc_code=P&hanacode=AK_CJU_LNB_pkg_s");
		menuList.add(m1);
		
		Menu m2 = new Menu();
		m2.setMenuCode(menuCode);
		m2.setMenuName("부산출발");
		m2.setMenuUrl("http://www.hanatour.com/asp/booking/productPackage/pk-11000.asp?area=K&pub_country=KR&pub_city=CJU&start_city=PUS&etc_code=P&hanacode=AK_CJU_LNB_pkg_b");
		menuList.add(m2);
		
		Menu m3 = new Menu();
		m3.setMenuCode(menuCode);
		m3.setMenuName("대구출발");
		m3.setMenuUrl("http://www.hanatour.com/asp/booking/productPackage/pk-11000.asp?area=K&pub_country=KR&pub_city=CJU&start_city=TAE&etc_code=P&hanacode=AK_CJU_LNB_pkg_t");
		menuList.add(m3);
		
		Menu m4 = new Menu();
		m4.setMenuCode(menuCode);
		m4.setMenuName("청주출발");
		m4.setMenuUrl("http://www.hanatour.com/asp/booking/productPackage/pk-11000.asp?area=K&pub_country=KR&pub_city=CJU&start_city=CJJ&etc_code=P&hanacode=AK_CJU_LNB_pkg_c");
		menuList.add(m4);
		
		Menu m5 = new Menu();
		m5.setMenuCode(menuCode);
		m5.setMenuName("광주출발");
		m5.setMenuUrl("http://www.hanatour.com/asp/booking/productPackage/pk-11000.asp?area=K&pub_country=KR&pub_city=CJU&start_city=KWJ&etc_code=P&hanacode=AK_CJU_LNB_pkg_g");
		menuList.add(m5);
		
		Menu m6 = new Menu();
		m6.setMenuCode(menuCode);
		m6.setMenuName("세미패키지");
		m6.setMenuUrl("http://www.hanatour.com/asp/booking/productPackage/pk-11000.asp?area=K&pub_country=KR&pub_city=CJU&etc_code=P&product_name=&hanacode=AK_CJU_LNB_aircartel");
		menuList.add(m6);
		
		Menu m7 = new Menu();
		m7.setMenuCode(menuCode);
		m7.setMenuName("허니문");
		m7.setMenuUrl("http://www.hanatour.com/asp/booking/productPackage/pk-11000.asp?area=K&pub_country=KR&pub_city=&etc_code=W&hanacode=AK_CJU_LNB_honey");
		menuList.add(m7);
//		for(Menu menu : menuList){
//			System.out.println(menu.getMenuCode() + " / " + menu.getMenuName() + " / " + menu.getMenuUrl());
//		}
//		menuList = new ArrayList<Menu>();
		return menuList;
	}
	
	
	
	
	private class JsonPrds{
		private Head head;
		private ArrayList<Cont> cont;
		
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
		
		private class Cont{
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
		private Head head;
		private ArrayList<Cal> cal;
		private ArrayList<Cont> cont;
		
		private class Head{
			private String mst;
			private String prev;
			private String curr;
			private String next;
			private String sday;
			private String flatfile_yn;
		}
		private class Cal{
			private String day;
			private String wday;
			private String dcol;
		}
		private class Cont{
			private String pcode;
			private String sdate;
			private String adate;
			private String acode;
			private String aline;
			private String tday;
			private String grade;
			private String gname;
			private String pname;
			private String amt;
			private String lminute;	
		}
	}
}
