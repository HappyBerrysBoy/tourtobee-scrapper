package hnc.tourtobee.scrapper.handler.website;

import static hnc.tourtobee.code.Codes.PRD_STATUS;
import static hnc.tourtobee.util.Util.log;
import hnc.tourtobee.scrapper.dataobject.Prd;
import hnc.tourtobee.scrapper.dataobject.PrdDtl;
import hnc.tourtobee.scrapper.handler.website.YellowBalloonHandler.DayList;
import hnc.tourtobee.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import jh.project.httpscrapper.Website;
import jh.project.httpscrapper.util.Html;

import org.apache.http.impl.client.CloseableHttpClient;

public class TourbaksaHandler extends _TouristAgencyHandler{

	private String defaultUrl = "http://www.tourbaksa.com";
	private HashMap<String, String> tourkind;
	
	public TourbaksaHandler(){
		tourkind = new HashMap<String, String>();
		tourkind.put("키즈투어", "P");
		tourkind.put("국내여행", "D");
		tourkind.put("골프", "G");
		tourkind.put("크루즈", "C");
		tourkind.put("해외패키지", "P");
		tourkind.put("레저/스포츠", "P");
		tourkind.put("해외자유", "F");
		tourkind.put("허니문", "W");
	}
	
	private class MenuList{
		private String url;
		private String tourkind;
		private String depCity;
		private List<SubMenuList> submenuList;
		
		private String getUrl() {
			return url;
		}
		private void setUrl(String url) {
			this.url = url;
		}
		private String getTourkind() {
			return tourkind;
		}
		private void setTourkind(String tourkind) {
			this.tourkind = tourkind;
		}
		private String getDepCity() {
			return depCity;
		}
		private void setDepCity(String depCity) {
			this.depCity = depCity;
		}
	}
	
	private class SubMenuList{
		private String region;
		private String url;
		
		private String getRegion() {
			return region;
		}
		private void setRegion(String region) {
			this.region = region;
		}
		private String getUrl() {
			return url;
		}
		private void setUrl(String url) {
			this.url = url;
		}
	}
	
	private class CityMenu{
		private String city;
		private String url;
		private String depCity;
		private String getCity() {
			return city;
		}
		private void setCity(String city) {
			this.city = city;
		}
		private String getUrl() {
			return url;
		}
		private void setUrl(String url) {
			this.url = url;
		}
		private String getDepCity() {
			return depCity;
		}
		private void setDepCity(String depCity) {
			this.depCity = depCity;
		}
	}
	
	@Override
	public ArrayList<Prd> scrapPrdList(CloseableHttpClient httpclient, Website website, HashMap<String, String> options, HashSet<String> insPrds) {
		
		ArrayList<Prd> prdList = new ArrayList<Prd>();
		
		try{
			Website mainPage = new Website();
			mainPage.setUrl(website.getUrl());
			mainPage.setMethod("GET");
			mainPage.setEncoding(website.getEncoding());
			
			Html mainHtml = new Html(this.getHtml(httpclient, mainPage));
			Html icnDepHtml = mainHtml.getValueById("city1").removeComment();
			Html pusDepHtml = mainHtml.getValueById("city2").removeComment();
			Html taeDepHtml = mainHtml.getValueById("city3").removeComment();
			
			List<MenuList> menuList = new ArrayList<MenuList>();
			getMenuList(icnDepHtml, menuList, "ICN");
			getMenuList(pusDepHtml, menuList, "PUS");
			getMenuList(taeDepHtml, menuList, "TAE");
			
			for(MenuList menu : menuList){
				for(SubMenuList submenu : menu.submenuList){
					Website subPage = new Website();
					subPage.setUrl(submenu.getUrl());
					subPage.setMethod("GET");
					subPage.setEncoding(website.getEncoding());
					
					Html subHtml = new Html(this.getHtml(httpclient, subPage));
					subHtml = subHtml.getValueByClass("leftArea").getValueByClass("clearfix").getValueByClass("sub4Depth");
					if(subHtml.toString().equals("")){
						subHtml = new Html(this.getHtml(httpclient, subPage));
						subHtml = subHtml.getValueByClass("leftArea").getValueByClass("clearfix").getValueByClass("subMenu sub3Depth");
					}
					
					List<CityMenu> cityMenuList = new ArrayList<CityMenu>();
					while(!subHtml.getTag("li").toString().trim().equals("")){
						Html regionHtml = subHtml.getTag("li");
						subHtml = subHtml.removeTag("li");
						
						if(regionHtml.toString().indexOf("href") < 0)
							continue;
						
						if(regionHtml.getTag("ul").toString().equals("")){
							CityMenu cityMenu = new CityMenu();
							cityMenu.setCity(regionHtml.removeAllTags().toString().trim());
							cityMenu.setUrl(defaultUrl + regionHtml.findRegex("href=['\"][^\"]+").toString().replace("href=\'", ""));
							cityMenu.setDepCity(menu.getDepCity());
							cityMenuList.add(cityMenu);
						}else{
							while(!regionHtml.getTag("ul").getTag("li").toString().trim().equals("")){
								Html detailCity = regionHtml.getTag("ul").getTag("li");
								regionHtml = regionHtml.getTag("ul").removeTag("li");
								
								if(!detailCity.getTag("ul").getTag("li").toString().equals("")){
									while(!detailCity.getTag("li").toString().trim().equals("")){
										CityMenu cityMenu = new CityMenu();
										Html detailCity2 = detailCity.getTag("ul").getTag("li");
										detailCity = detailCity.getTag("ul").removeTag("li");
										
										String replaceStr = detailCity2.findRegex("href=['\'][^\']+").toString();
										cityMenu.setCity(detailCity2.toString().replace(replaceStr, ""));
										cityMenu.setUrl(defaultUrl + detailCity2.findRegex("href=['\'][^\']+").toString().replace("href=\'", ""));
										cityMenu.setDepCity(menu.getDepCity());
										cityMenuList.add(cityMenu);
									}
								}else{
									CityMenu cityMenu = new CityMenu();
									String replaceStr = detailCity.findRegex("href=['\'][^\']+").toString();
									cityMenu.setCity(detailCity.toString().replace(replaceStr, ""));
									cityMenu.setUrl(defaultUrl + detailCity.findRegex("href=['\'][^\']+").toString().replace("href=\'", ""));
									cityMenu.setDepCity(menu.getDepCity());
									cityMenuList.add(cityMenu);
								}
							}
						}
					}
					
					for(CityMenu city : cityMenuList){
						Website prdPage = new Website();
						prdPage.setUrl(city.getUrl());
						prdPage.setMethod("GET");
						prdPage.setEncoding(website.getEncoding());
						
						Html prdHtml = new Html(this.getHtml(httpclient, prdPage));
						prdHtml = prdHtml.getValueByClass("list");
						
						while(!prdHtml.getValueByClass("itemList").toString().trim().equals("")){
							Html prdListHtml = prdHtml.getValueByClass("itemList");
							prdHtml = prdHtml.removeValueByClass("itemList");
							
							while(!prdListHtml.getTag("li").toString().trim().equals("")){
								Html prdDtlHtml = prdListHtml.getTag("li").removeComment();
								prdListHtml = prdListHtml.removeTag("li");
								
								Prd prd = new Prd();
								prd.setTagnId(website.getId());
//								System.out.println(prdDtlHtml.getTag("h4").removeAllTags().toString().trim());						// prdnm
								String prdnm = prdDtlHtml.getTag("h4").removeAllTags().toString().trim();
								String prddesc = prdDtlHtml.getValueByClass("note").removeAllTags().toString().trim();
								prd.setPrdNm(prdnm);
//								System.out.println(prdDtlHtml.getValueByClass("note").removeAllTags().toString().trim());			// prddesc
								prd.setPrdDesc(prddesc);
//								System.out.println(prdDtlHtml.getTag("span").removeAllTags().toString().split("박")[0].trim());		// night
								prd.setNight(prdDtlHtml.getTag("span").removeAllTags().toString().split("박")[0].trim());
//								System.out.println(prdDtlHtml.getTag("span").removeAllTags().toString().split("박")[1].split("일")[0].trim());		// trterm
								prd.setTrterm(prdDtlHtml.getTag("span").removeAllTags().toString().split("박")[1].split("일")[0].trim());
								String airIdx = prdDtlHtml.getValueByClass("detail liner").toString();
								if(airIdx.indexOf(".gif") > -1){
//									System.out.println(airIdx.substring(airIdx.indexOf(".gif") - 2, airIdx.indexOf(".gif")));		// aircode
									prd.setAircode(airIdx.substring(airIdx.indexOf(".gif") - 2, airIdx.indexOf(".gif")));
								}
								String prdno = prdDtlHtml.getValueByClass("itemNum detail").removeAllTags().toString().trim();
								prd.setPrdNo(prdno);
								String prdUrl = defaultUrl + "/xml/item_Index_List.asp?gy=" + prdno.substring(0, 4) + "&gs=" + prdno.substring(4).split("-")[0] 
													+ "&AirIDX=" + prdno.split("-")[1] + "&sd=thismonth01&" + city.getUrl().split("\\?")[1];		// 나중에 thismonth를 month로 치환해서 사용..
								System.out.println(prdUrl);
								prd.setPrdUrl(prdUrl);
								prd.setAreaList(this.getAreaList(prdnm + " " + prddesc, city.getCity()));
								prd.setTrDiv(tourkind.get(menu.getTourkind()));
								if(prd.getTrDiv().equals("D"))
									prd.setDmstDiv("D");
								else
									prd.setDmstDiv("A");
								prd.setDepArpt(city.getDepCity());
								prdList.add(prd);
//								break;
							}
//							break;
						}
//						break;
					}
//					break;
				}
//				break;
			}
			System.out.println("asdasdff");
			
		}catch(Exception e){
			log("Get Mainpage Exception : ", e.getStackTrace()[0].toString());
		}
			
		return prdList;
	}
	
	private void getMenuList(Html html, List<MenuList> menuList, String depCity){
		Html menuHtml = null;
		Html subMenuHtml = null;
		while(!html.getTag("ul").getTag("li").toString().trim().equals("")){
			menuHtml = html.getTag("ul").getTag("li");
			html = html.getTag("ul").removeTag("li");
			
			MenuList menu = new MenuList();
			String menuUrl = menuHtml.getTag("a").findRegex("href=['\"][^\"]+").toString().replace("href=\"", "");
			if(!menuUrl.toUpperCase().contains("SUBMAIN"))
				continue;
			menu.setUrl(defaultUrl + menuUrl);
			menu.setTourkind(menuHtml.getTag("a").removeAllTags().toString().trim());
			menu.setDepCity(depCity);
			menu.submenuList = new ArrayList<SubMenuList>();
			while(!menuHtml.getTag("ul").getTag("li").toString().trim().equals("")){
				subMenuHtml = menuHtml.getTag("ul").getTag("li");
				menuHtml = menuHtml.getTag("ul").removeTag("li");
				
				SubMenuList subMenu = new SubMenuList();
				subMenu.setUrl(defaultUrl + subMenuHtml.getTag("a").findRegex("href=['\"][^\"]+").toString().replace("href=\"", ""));
				subMenu.setRegion(subMenuHtml.removeAllTags().toString().trim());
				menu.submenuList.add(subMenu);
			}
			
			menuList.add(menu);
		}
	}
	
	@Override
	public ArrayList<PrdDtl> scrapPrdDtlSmmry(CloseableHttpClient httpclient, Website website, HashMap<String, String> options, Prd prd) {
		HashSet<String> monthSet = this.getMonthSet(options);
		ArrayList<PrdDtl> prdDtlList = new ArrayList<PrdDtl>();
		Html prdDtlHtml = null;
		try{
//			prd.setPrdUrl("http://www.tourbaksa.com/xml/item_Index_List.asp?gy=JOBS&gs=214&AirIDX=3&sd=thismonth01&M1=1&M2=8&M3=9&M4=16&M5=618");
			for (String month : monthSet){
				String prdDtlSummaryUrl = prd.getPrdUrl().replace("thismonth", month);	
				
				Website prdDtlListSite = new Website();
				prdDtlListSite.setUrl(prdDtlSummaryUrl);
				prdDtlListSite.setMethod("GET");
				prdDtlListSite.setEncoding(website.getEncoding());
				
				Html prdDtlListHtml = new Html(this.getHtml(httpclient, prdDtlListSite));
				
				prdDtlListHtml = prdDtlListHtml.getValueByClass("reservationTable").getTag("tbody");
				Boolean firstDtl = true;
				String depTime = "";
				String arrTime = "";
				
				while(prdDtlListHtml.getTag("tr").toString().length() > 0){
					try{
						PrdDtl dtl = new PrdDtl();
						prdDtlHtml = prdDtlListHtml.getTag("tr");
//						System.out.println("=======================================================");
						prdDtlListHtml = prdDtlListHtml.removeTag("tr");
						
//						System.out.println(month + prdDtlHtml.getValueByClass("startDate").removeAllTags().getOnlyNumber().toString().trim());	// depDt
//						System.out.println(prdDtlHtml.getValueByClass("price").removeAllTags().getOnlyNumber().toString().trim());		// price
						Boolean boolWait = false;
//						System.out.println(prdDtlHtml.getValueByClass("status").removeAllTags().toString().trim());				// 대기예약인 경우만.. 체크..
						if(prdDtlHtml.getValueByClass("status").removeAllTags().toString().trim().equals("대기예약"))
							boolWait = true;
						Html reservation = prdDtlHtml.getValueByClass("reservation");
//						System.out.println(defaultUrl + reservation.findRegex("href=['\"][^\"]+").toString().replace("href=\'", "").replace("'", ""));		// url
						dtl.setPrdUrl(defaultUrl + reservation.findRegex("href=['\"][^\"]+").toString().replace("href=\'", "").replace("'", ""));
//						System.out.println(reservation.toString().split("EV_YM=")[1].split("&")[0] + reservation.toString().split("EV_SEQ=")[1].split("&")[0]);		// prdseq
						String sts = reservation.removeAllTags().toString().trim();
//						System.out.println(sts);		// prdSt
						if (sts.contains("바로예약")){
							sts = "출발확정";
						}else if (sts.contains("예약마감")){
							sts = "예약마감";
						}else if (sts.contains("예약접수")){
							sts = "예약가능";
						}else if (boolWait && sts.contains("예약접수")){
							sts = "대기예약";
						}else{
							sts = "예약가능";
						}
						
						if (firstDtl){
							Website prdDtlSite = new Website();
							prdDtlSite.setUrl(dtl.getPrdUrl());
							prdDtlSite.setMethod("GET");
							prdDtlSite.setEncoding(website.getEncoding());
							
							Html prdDtlInfoHtml = new Html(this.getHtml(httpclient, prdDtlSite));
							Html startHtml = prdDtlInfoHtml.getValueByClass("startArrival").getTag("b").removeAllTags();
//							System.out.println(getOnlyNumber(startHtml.toString().split("\\)")[1]));		// start time
							depTime = getOnlyNumber(startHtml.toString().split("\\)")[1]);
							prdDtlInfoHtml = prdDtlInfoHtml.removeValueByClass("startArrival");
							Html arrivalHtml = prdDtlInfoHtml.getValueByClass("startArrival").getTag("b").removeAllTags();
							arrTime = getOnlyNumber(arrivalHtml.toString().split("\\)")[1]);
//							System.out.println(arrTime);		// start time
							firstDtl = false;
						}
						
						dtl.setTagnId(website.getId());
						dtl.setPrdNo(prd.getPrdNo());
						dtl.setPrdSeq(reservation.toString().split("EV_YM=")[1].split("&")[0] + reservation.toString().split("EV_SEQ=")[1].split("&")[0]);
						dtl.setPrdDtlNm(prd.getPrdNm());
						dtl.setDepDt(month.substring(0, 4) + prdDtlHtml.getValueByClass("startDate").removeAllTags().getOnlyNumber().toString().trim() + depTime);
//						System.out.println(month.substring(0, 4) + prdDtlHtml.getValueByClass("startDate").removeAllTags().getOnlyNumber().toString().trim());
//						System.out.println(Integer.parseInt(prd.getTrterm()) + "");
//						System.out.println(Util.setOperationDate(month.substring(0, 4) + prdDtlHtml.getValueByClass("startDate").removeAllTags().getOnlyNumber().toString().trim(), Integer.parseInt(prd.getTrterm()) - 1));
						dtl.setArrDt(Util.setOperationDate(month.substring(0, 4) + prdDtlHtml.getValueByClass("startDate").removeAllTags().getOnlyNumber().toString().trim(), Integer.parseInt(prd.getTrterm()) - 1) + arrTime);
						dtl.setDepArpt(prd.getDepArpt());
						dtl.setArlnId(prd.getAircode());
						dtl.setPrdSt(PRD_STATUS.get(sts));
						dtl.setPrdFeeAd(prdDtlHtml.getValueByClass("price").removeAllTags().getOnlyNumber().toString().trim());

						prdDtlList.add(dtl);
					}catch(Exception e){
						log("PrdDtl Parcing Exception : ", e.getStackTrace()[0].toString());
					}
				}
//					break;
			}
		}catch(Exception e){
			log("scrapPrdDtlSmmry", e.toString());
		}
		
		return prdDtlList;
	}
}
