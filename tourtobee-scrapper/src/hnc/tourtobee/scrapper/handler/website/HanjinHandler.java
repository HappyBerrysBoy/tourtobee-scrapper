package hnc.tourtobee.scrapper.handler.website;

import hnc.tourtobee.scrapper.dataobject.Menu;
import hnc.tourtobee.scrapper.dataobject.Prd;
import hnc.tourtobee.scrapper.dataobject.PrdDtl;
import static hnc.tourtobee.code.Codes.ARPT_NAME_CODE;
import static hnc.tourtobee.code.Codes.PRD_STATUS;
import static hnc.tourtobee.code.Codes.WEEK_DAY_NUMBER;
import static hnc.tourtobee.util.Util.log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import jh.project.httpscrapper.Website;
import jh.project.httpscrapper.util.Html;

import org.apache.http.impl.client.CloseableHttpClient;

public class HanjinHandler extends _TouristAgencyHandler {

	@Override
	public ArrayList<Prd> scrapPrdList(CloseableHttpClient httpclient, Website website, HashMap<String, String> options, HashSet<String> insPrds) {
		ArrayList<Prd> prdList = new ArrayList<Prd>();
		Html html = new Html(this.getHtml(httpclient, website));
		ArrayList<Menu> menuList = getMenuUrlList(website, html.removeComment().getValueByClass("mainNavitopd").toString());
		
		for (Menu menu : menuList){
			log(website.getId() + " - Get Menu", menu.getMenuName());
			Website menuSite = new Website();
			menuSite.setId(website.getId());
			menuSite.setName(website.getName());
			menuSite.setUrl(menu.getMenuUrl());
			menuSite.setMethod(website.getMethod());
			menuSite.setEncoding(website.getEncoding());
			
			Html menuHtml = new Html(this.getHtml(httpclient, menuSite));
			String depArpt = menuHtml.getValueByClass("shotTitle1").removeAllTags().toString();
			if (depArpt.contains("부산")){
				depArpt = "부산";
			}else if (depArpt.contains("청주")){
				depArpt = menu.getMenuName().trim().substring(0, 2);
			}else{
				depArpt = "인천";
			}
			menuHtml = menuHtml.getTag("xml").getTag("product");
			int prdCnt = 0;
			while(menuHtml.getTag("item").toString().length() > 0){
				Html prdHtml = menuHtml.getTag("item");
				menuHtml = menuHtml.removeTag("item");
				
				Prd prd = new Prd();
				prd.setTagnId(website.getId());
				prd.setPrdNo(prdHtml.getTag("PRODUCT_CODE").removeAllTags().toString());
				prd.setPrdNm(prdHtml.getTag("PRODUCT_NAME").toString().replaceAll("<!\\[CDATA\\[", "").replaceAll("\\]\\]>", "").replaceAll("<[/]*PRODUCT_NAME>", ""));
				prd.setTrDiv(menu.getMenuCode());
				prd.setDmstDiv("A");
				prd.setPrdDesc(prdHtml.getTag("PROINTRODUCE").toString().replaceAll("<!\\[CDATA\\[", "").replaceAll("\\]\\]>", "").replaceAll("<[/]*PROINTRODUCE>", ""));
				prd.setAreaList(this.getAreaList(prd.getPrdNm() + " " + prd.getPrdDesc(), menu.getMenuName()));
				prd.setDepArpt(ARPT_NAME_CODE.get(depArpt));
				
				if (insPrds == null || !insPrds.contains(prd.getPrdNo())){
					prdList.add(prd);
					prdCnt++;
				}
				
			}
			log("  Prd Scrapping", String.valueOf(prdCnt) + " Prds Scrapped");
		}
		return prdList;
	}


	
	


	@Override
	public ArrayList<PrdDtl> scrapPrdDtlSmmry(CloseableHttpClient httpclient, Website website, HashMap<String, String> options, Prd prd) {
		HashSet<String> monthSet = this.getMonthSet(options);
		ArrayList<PrdDtl> prdDtlList = new ArrayList<PrdDtl>();
		Html prdDtlHtml = null;
		
		try{
			for (String month : monthSet){
				String prdDtlSummaryUrl = "http://www.kaltour.com/Content/Product/TourList.aspx?"
						+ "viewListOrCalendar=Y"
						+ "&divType=LIST"
						+ "&divProduct=divListTypePL_"
						+ "&divTour=divListTypeProductTour_"
						+ "&pkgpnh=" + prd.getPrdNo()
						+ "&selMonth=" + month;	
				
				Website prdDtlListSite = new Website();
				prdDtlListSite.setUrl(prdDtlSummaryUrl);
				prdDtlListSite.setMethod("GET");
				prdDtlListSite.setEncoding(website.getEncoding());
				
				Html prdDtlListHtml = new Html(this.getHtml(httpclient, prdDtlListSite));
				prdDtlListHtml = prdDtlListHtml.getTag("tbody");
				
				while(prdDtlListHtml.getTag("tr").toString().length() > 0){
					prdDtlHtml = prdDtlListHtml.getTag("tr");
					prdDtlListHtml = prdDtlListHtml.removeTag("tr");
					
					String depDt = prdDtlHtml.getTag("li").removeAllTags().toString();
					String depWkDay = "";
					String depHm = "0000";
					if (depDt.substring(0, 2).equals("출발")){
						depDt = depDt.substring(2);
						depWkDay = depDt.split("\\(")[1].split("\\)")[0];
						depWkDay = WEEK_DAY_NUMBER.get(depWkDay);
						depDt = depDt.replaceAll("\\([\\s\\S]\\)", "").replaceAll("[:\\- ]*", "");
						depDt = month.substring(0, 4) + depDt;
						if (depDt.length() > 8 ) depHm = depDt.substring(8, 12);
						depDt = depDt.substring(0, 8);
					}
					String arrDt = prdDtlHtml.removeTag("li").getTag("li").removeAllTags().toString();
					String arrWkDay = "";
					String arrHm = "0000";
					if (arrDt.substring(0, 2).equals("도착")){
						arrDt = arrDt.substring(2);
						arrWkDay = arrDt.split("\\(")[1].split("\\)")[0];
						arrWkDay = WEEK_DAY_NUMBER.get(arrWkDay);
						arrDt = arrDt.replaceAll("\\([\\s\\S]\\)", "").replaceAll("[:\\- ]*", "");
						arrDt = month.substring(0, 4) + arrDt;
						if (arrDt.length() > 8 ) arrHm = arrDt.substring(8, 12);
						arrDt = arrDt.substring(0, 8);
					}
					String prdDtlNm = prdDtlHtml.removeTag("td").getTag("td").removeAllTags().toString().trim();
					String getPrdDtlUrlArgs[] = prdDtlHtml.removeTag("td").getTag("td").toString().split("<a href=\"javascript:MoveOverseasView\\(")[1].split("\\);")[0].split(",");
					String prdDtlUrl = this.getPrdDtlUrl(getPrdDtlUrlArgs[0].replaceAll("'", "")
														, getPrdDtlUrlArgs[1].replaceAll("'", "")
														, getPrdDtlUrlArgs[2].replaceAll("'", "")
														, getPrdDtlUrlArgs[3].replaceAll("'", ""));
					String prdSeq = getPrdDtlUrlArgs[1].replaceAll("'", "");
					String airLine = "";
					if (prdDtlHtml.removeTag("td").removeTag("td").getTag("td").toString().split("<img src='/images/icon/").length > 1){
						airLine = prdDtlHtml.removeTag("td").removeTag("td").getTag("td").toString().split("<img src='/images/icon/")[1].split("\\.")[0];
						airLine = airLine.substring(airLine.length() - 2, airLine.length());
					}
					String feeAd = prdDtlHtml.removeTag("td").removeTag("td").removeTag("td").getTag("td").removeAllTags().toString().split("원")[0].replace("," ,  "");
					try{
						int intFeeAd = Integer.parseInt(feeAd);
					}catch(Exception e){
						feeAd = "9999999";
					}
					String status = prdDtlHtml.removeTag("td").removeTag("td").removeTag("td").removeTag("td").getTag("td").toString();
					if (status.contains("09_icon21.gif")){
	//					마감
						status = "예약마감";
					}else if (status.contains("09_icon17.gif")){
	//					출발확정
						status = "출발확정";
					}else if (status.contains("09_icon20.gif")){
	//					대기예약
						status = "대기예약";
					}else if (status.contains("09_icon50.gif")){
	//					출발예정
						status = "예약가능";
					}else if (status.contains("09_icon51.gif")){
	//					예약중
						status = "예약가능";
					}else{
						status = "예약가능";
					}
					status = PRD_STATUS.get(status);
	
					PrdDtl prdDtl = new PrdDtl();
					prdDtl.setTagnId(website.getId());
					prdDtl.setPrdNo(prd.getPrdNo());
					prdDtl.setPrdSeq(prdSeq);
					prdDtl.setDepDt(depDt + depHm);
					prdDtl.setDepDtYmd(depDt);
					prdDtl.setDepDtHm(depHm);
					prdDtl.setDepDtWd(depWkDay);
					prdDtl.setArrDt(arrDt + arrHm);
					prdDtl.setArrDtYmd(arrDt);
					prdDtl.setArrDtHm(arrHm);
					prdDtl.setArrDtWd(arrWkDay);
					prdDtl.setDepArpt(prd.getDepArpt());
					prdDtl.setPrdDtlNm(prdDtlNm);
					prdDtl.setPrdUrl(prdDtlUrl);
					prdDtl.setArlnId(airLine);
					prdDtl.setPrdFeeAd(feeAd);
					prdDtl.setPrdSt(status);
					
					prdDtlList.add(prdDtl);
				}
			}
		}catch(Exception e){
			log("scrapPrdDtlSmmry", e.toString());
		}
		
		return prdDtlList;
	}



	private String getPrdDtlUrl(String pnh, String hno, String seq, String sts){
		String url = "http://www.kaltour.com/ProductOverseas/OverseasView?"
					+ "pkgpnh=" + pnh
					+ "&hg1hno=" + hno
					+ "&hg1seq=" + seq
					+ "&hg1sts=" + sts;
		return url;
	}


	private ArrayList<Menu> getMenuUrlList(Website website, String htmlStr){
		ArrayList<Menu> menuList = new ArrayList<Menu>();
		Menu menu = new Menu();
		String menuName = "";
		String menuUrl = "";
		Html html = new Html(htmlStr.substring(2));
		
		//해외패키지
		Html categoryHtml = new Html(html.getTag("div").toString().substring(2));
		categoryHtml = categoryHtml.removeTag("div");
		
		while(categoryHtml.getTag("div").toString().length() > 0){
			Html subMenuHtml = categoryHtml.getTag("div");
			
			while(subMenuHtml.getTag("li").toString().length() > 0){
				menuUrl = subMenuHtml.getTag("li").getTag("a").findRegex("href=[ \"']+[\\s\\S]*['\"]+").toString().replaceAll("href=", "").replaceAll("['\"]", "");
				menuUrl = website.getUrl() + menuUrl;
				menuName = subMenuHtml.getTag("li").getTag("a").removeAllTags().toString();
				menu = new Menu();
				menu.setMenuName(menuName);
				menu.setMenuUrl(menuUrl);
				menu.setMenuCode("P");
				menuList.add(menu);
				
				subMenuHtml = subMenuHtml.removeTag("li");
			}
			
			categoryHtml = categoryHtml.removeTag("div");
		}
		
		//크루즈
		categoryHtml = html.removeTag("div").removeTag("div").getTag("div");
		
		while(categoryHtml.getTag("div").toString().length() > 0){
			Html subMenuHtml = categoryHtml.getTag("div");
			
			while(subMenuHtml.getTag("li").toString().length() > 0){
				menuUrl = subMenuHtml.getTag("li").getTag("a").findRegex("href=[ \"']+[\\s\\S]*['\"]+").toString().replaceAll("href=", "").replaceAll("['\"]", "");
				menuUrl = website.getUrl() + menuUrl;
				menuName = subMenuHtml.getTag("li").getTag("a").removeAllTags().toString();
				menu = new Menu();
				menu.setMenuName(menuName);
				menu.setMenuUrl(menuUrl);
				menu.setMenuCode("C");
				menuList.add(menu);
				
				subMenuHtml = subMenuHtml.removeTag("li");
			}
			
			categoryHtml = categoryHtml.removeTag("div");
		}
		
		//허니문
		categoryHtml = html.removeTag("div").removeTag("div").removeTag("div").getTag("div");
		
		while(categoryHtml.getTag("div").toString().length() > 0){
			Html subMenuHtml = categoryHtml.getTag("div");
			
			while(subMenuHtml.getTag("li").toString().length() > 0){
				menuUrl = subMenuHtml.getTag("li").getTag("a").findRegex("href=[ \"']+[\\s\\S]*['\"]+").toString().replaceAll("href=", "").replaceAll("['\"]", "");
				menuUrl = website.getUrl() + menuUrl;
				menuName = subMenuHtml.getTag("li").getTag("a").removeAllTags().toString();
				menu = new Menu();
				menu.setMenuName(menuName);
				menu.setMenuUrl(menuUrl);
				menu.setMenuCode("W");
				menuList.add(menu);
				
				subMenuHtml = subMenuHtml.removeTag("li");
			}
			
			categoryHtml = categoryHtml.removeTag("div");
		}
		
		
		//골프
		categoryHtml = html.removeTag("div").removeTag("div").removeTag("div").removeTag("div").getTag("div");
		
		while(categoryHtml.getTag("div").toString().length() > 0){
			Html subMenuHtml = categoryHtml.getTag("div");
			
			while(subMenuHtml.getTag("li").toString().length() > 0){
				menuUrl = subMenuHtml.getTag("li").getTag("a").findRegex("href=[ \"']+[\\s\\S]*['\"]+").toString().replaceAll("href=", "").replaceAll("['\"]", "");
				menuUrl = website.getUrl() + menuUrl;
				menuName = subMenuHtml.getTag("li").getTag("a").removeAllTags().toString();
				menu = new Menu();
				menu.setMenuName(menuName);
				menu.setMenuUrl(menuUrl);
				menu.setMenuCode("G");
				menuList.add(menu);
				
				subMenuHtml = subMenuHtml.removeTag("li");
			}
			
			categoryHtml = categoryHtml.removeTag("div");
		}
//		부산출발은 직접 해줘야함...
		menu = new Menu();
		menu.setMenuUrl("http://www.kaltour.com/ProductOverseas/OverseasList?pkgdep=PUSSM&PKGMOK=06&LMNU=5_6_0_0&MNU2COD=174&MNUCOD=");
		menu.setMenuName("부산출발");
		menuList.add(menu);

		
//		국내, 에어텔은 제외함
		
		return menuList;
	}

	
}
