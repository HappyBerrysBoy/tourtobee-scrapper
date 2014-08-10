package hnc.tourtobee.scrapper.handler.website;

import static hnc.tourtobee.code.Codes.PRD_STATUS;
import static hnc.tourtobee.util.Util.log;
import hnc.tourtobee.scrapper.dataobject.Prd;
import hnc.tourtobee.scrapper.dataobject.PrdDtl;
import hnc.tourtobee.scrapper.handler.website.YellowBalloonHandler.DayList;
import hnc.tourtobee.scrapper.handler.website.YellowBalloonHandler.Submenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import jh.project.httpscrapper.Website;
import jh.project.httpscrapper.util.Html;

import org.apache.http.impl.client.CloseableHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class ModetourHandler extends _TouristAgencyHandler{
	HashMap<String, String> packageMap = new HashMap<String, String>();
	
	public ModetourHandler(){
		packageMap.put("패키지", "P");
	    packageMap.put("자유", "F");
	    packageMap.put("허니문", "W");
	    packageMap.put("골프", "G");
	    packageMap.put("JM", "P");
	    packageMap.put("크루즈", "C");
	    packageMap.put("부산·지방출발", "P");
	    packageMap.put("제주", "D");
	}
	
	private class MenuUrl{
		private String depcity;
		private String id;
		private String type;
		private String loc;
		private String name;
		
		private String getDepcity() {
			return depcity;
		}
		private void setDepcity(String depcity) {
			this.depcity = depcity;
		}
		private String getId() {
			return id;
		}
		private void setId(String id) {
			this.id = id;
		}
		private String getType() {
			return type;
		}
		private void setType(String type) {
			this.type = type;
		}
		private String getLoc() {
			return loc;
		}
		private void setLoc(String loc) {
			this.loc = loc;
		}
		private String getName() {
			return name;
		}
		private void setName(String name) {
			this.name = name;
		}
		private String makeUrl(){
			return "http://www.modetour.com/Package/subMain2.aspx?startLocation=" + this.depcity + "&id=" 
					+ this.id + "&type=" + this.type + "&MLoc=" + this.loc;
		}
	}
	
	private class SubMenu{
		private String depcity;
		private String location;
		private String location1;
		private String theme;
		private String theme1;
		private String loc;
		private String name;
		
		private String getDepcity() {
			return depcity;
		}
		private void setDepcity(String depcity) {
			this.depcity = depcity;
		}
		private String getLocation() {
			return location;
		}
		private void setLocation(String location) {
			this.location = location;
		}
		private String getLocation1() {
			return location1;
		}
		private void setLocation1(String location1) {
			this.location1 = location1;
		}
		private String getTheme() {
			return theme;
		}
		private void setTheme(String theme) {
			this.theme = theme;
		}
		private String getTheme1() {
			return theme1;
		}
		private void setTheme1(String theme1) {
			this.theme1 = theme1;
		}
		private String getLoc() {
			return loc;
		}
		private void setLoc(String loc) {
			this.loc = loc;
		}
		private String getName() {
			return name;
		}
		private void setName(String name) {
			this.name = name;
		}
		private String makeUrl(){
			return "http://www.modetour.com/Package/List.aspx?startLocation=" + this.depcity + "&location=" + this.location
					+ "&location1=" + this.location1 + "&Theme=" + this.theme + "&Theme1=" + this.theme1 + "&MLoc=" + this.loc;
		}
	}
	
	private MenuUrl setMenuUrl(Html html){
		MenuUrl menu = new MenuUrl();
		menu.setDepcity(html.toString().split("startLocation=")[1].split("&")[0]);
		menu.setId(html.toString().split("id=")[1].split("&")[0]);
		menu.setType(html.toString().split("type=")[1].split("&")[0]);
		menu.setLoc(html.toString().split("MLoc=")[1].split("\"")[0]);
		menu.setName(html.getTag("span").removeAllTags().toString().trim());
		return menu;
	}
	
	private SubMenu setSubMenuUrl(Html html){
		SubMenu menu = new SubMenu();
		menu.setDepcity(html.toString().split("startLocation=")[1].split("&")[0]);
		menu.setLocation(html.toString().split("location=")[1].split("&")[0]);
		menu.setLocation1(html.toString().split("location1=")[1].split("&")[0]);
		if(html.toString().toUpperCase().contains("THEME=")){
			menu.setTheme(html.toString().split("Theme=")[1].split("&")[0]);
		}else{
			menu.setTheme("");
		}
		if(html.toString().toUpperCase().contains("THEME1=")){
			menu.setTheme1(html.toString().split("Theme1=")[1].split("&")[0]);
		}else{
			menu.setTheme1("");
		}
		menu.setLoc(html.toString().split("MLoc=")[1].split("\"")[0]);
		menu.setName(html.getTag("span").removeAllTags().toString().trim());
		return menu;
	}
	
	@Override
	public ArrayList<Prd> scrapPrdList(CloseableHttpClient httpclient, Website website, HashMap<String, String> options, HashSet<String> insPrds) {
		
		ArrayList<Prd> prdList = new ArrayList<Prd>();
		
		try{
			Website mainPage = new Website(website.getUrl(), "GET", website.getEncoding());
			
			Html mainHtml = new Html(this.getHtml(httpclient, mainPage));
			
			Html overseasHtml = mainHtml.getValueByClass("overseas");
			Html domesticHtml = mainHtml.getValueByClass("domestic");
			
			List<MenuUrl> menuList = new ArrayList<MenuUrl>();
			List<String> prdCodeList = new ArrayList<String>();
			
			while(!overseasHtml.getTag("ul").getTag("li").toString().trim().equals("")){
				Html menuHtml = overseasHtml.getTag("ul").getTag("li");
				overseasHtml = overseasHtml.getTag("ul").removeTag("li");
				if(!menuHtml.toString().toUpperCase().contains("SUBMAIN2"))
					continue;
				
				menuList.add(setMenuUrl(menuHtml));
			}
			
			while(!domesticHtml.getTag("ul").getTag("li").toString().trim().equals("")){
				Html menuHtml = domesticHtml.getTag("ul").getTag("li");
				domesticHtml = domesticHtml.getTag("ul").removeTag("li");
				if(!menuHtml.toString().toUpperCase().contains("제주"))
					continue;
				
				menuList.add(setMenuUrl(menuHtml));
			}
			
			for(MenuUrl menu : menuList){
				try{
					Website menuPage = new Website(menu.makeUrl(), "GET", website.getEncoding());
					
					Html menuHtml = new Html(this.getHtml(httpclient, menuPage));
					menuHtml = menuHtml.getValueByClass("submain");
					
					while(!menuHtml.getValueByClass("cols").toString().equals("")){
						Html subMenuHtml = menuHtml.getValueByClass("cols");
						menuHtml = menuHtml.removeValueByClass("cols");
						
						while(!subMenuHtml.getTag("dl").toString().equals("")){
							Html regionMenuHtml = subMenuHtml.getTag("dl");
							subMenuHtml = subMenuHtml.removeTag("dl");
							
							while(!regionMenuHtml.getTag("dt").toString().equals("") 
									|| !regionMenuHtml.getTag("dd").toString().equals("")){
								Html regionHtml = regionMenuHtml.getTag("dt");
								String regionMenu = regionHtml.removeAllTags().toString().trim();
								
								if(regionHtml.toString().equals("")){
									regionHtml = regionMenuHtml.getTag("dd");
									regionMenuHtml = regionMenuHtml.removeTag("dd");
								}else{
									regionMenuHtml = regionMenuHtml.removeTag("dt");
								}
								
								SubMenu submenu = setSubMenuUrl(regionHtml);
								String anCode = submenu.makeUrl().split("location=LOC")[1].split("&")[0];
								String themeCode = submenu.makeUrl().split("Theme=")[1].split("&")[0];
								String prdXmlUrl = "http://www.modetour.com/XML/Package/Get_ProductList.aspx?AN=" + anCode + "&Ct=&PL=1000&Pd=&Pn=1&TN=" + themeCode;
								System.out.println("prdXmlUrl : " + prdXmlUrl);
//								
//								Website prdXmlPage = new Website(prdXmlUrl, "GET", website.getEncoding());
//								
//								Html prdXmlHtml = new Html(this.getHtml(httpclient, prdXmlPage));
//								System.out.println(prdXmlHtml.toString());
								
								// XML Document 객체 생성
								Document mainXml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(prdXmlUrl);
								
								// xpath 생성
								XPath xpath = XPathFactory.newInstance().newXPath();
								
								List<String> listPrdXml = new ArrayList<String>(); 
								NodeList prdXmlList = (NodeList)xpath.evaluate("//Product", mainXml, XPathConstants.NODESET);
								Prd[] prdTempList = new Prd[prdXmlList.getLength()];
							    for(int idx = 0; idx<prdXmlList.getLength(); idx++){
//							    	System.out.println(prdXmlList.item(idx).getAttributes().getNamedItem("Pcode").getTextContent());
							    	prdTempList[idx] = new Prd();
							    	prdTempList[idx].setTagnId(website.getId());
							    	prdTempList[idx].setTrDiv(packageMap.get(menu.getName()));
							    	prdTempList[idx].setDmstDiv("A");
							    	prdTempList[idx].setPrdNo(prdXmlList.item(idx).getAttributes().getNamedItem("Pcode").getTextContent());
//							    	listPrdXml.add(prdXmlList.item(idx).getTextContent());
							    }
							    
							    NodeList prdNameXmlList = (NodeList)xpath.evaluate("//Product/Name", mainXml, XPathConstants.NODESET);
							    for(int idx = 0; idx<prdNameXmlList.getLength(); idx++){
//							    	System.out.println(prdXmlList.item(idx).getAttributes().getNamedItem("Pcode").getTextContent());
							    	prdTempList[idx].setPrdNm(prdNameXmlList.item(idx).getTextContent());
//							    	listPrdXml.add(prdXmlList.item(idx).getTextContent());
							    }
							    
							    NodeList prdDescXmlList = (NodeList)xpath.evaluate("//Product/Content", mainXml, XPathConstants.NODESET);
							    for(int idx = 0; idx<prdDescXmlList.getLength(); idx++){
//							    	System.out.println(prdXmlList.item(idx).getAttributes().getNamedItem("Pcode").getTextContent());
							    	prdTempList[idx].setPrdDesc(prdDescXmlList.item(idx).getTextContent());
							    	prdTempList[idx].setAreaList(this.getAreaList(prdTempList[idx].getPrdNm() + " " + prdTempList[idx].getPrdDesc(), regionMenu));
							    	prdTempList[idx].setPrdUrl("http://www.modetour.com/Xml/Package/Get_Pcode.aspx?Ct=&Month=thismonth&Pcode=" + prdTempList[idx].getPrdNo() + "&Pd=&Type=01");
							    	prdList.add(prdTempList[idx]);
//							    	listPrdXml.add(prdXmlList.item(idx).getTextContent());
							    }
							    
							    for(String detailPrd : listPrdXml){
							    	if (prdCodeList.contains(detailPrd))
							    		continue;
							    }
							}
						}
					}
					
				}catch(Exception e){
					log("Menu Exception : ", e.getStackTrace()[0].toString());
				}
			}
			
		}catch(Exception e){
			log("Get Mainpage Exception : ", e.getStackTrace()[0].toString());
		}
			
		return prdList;
	}
	
	@Override
	public ArrayList<PrdDtl> scrapPrdDtlSmmry(CloseableHttpClient httpclient, Website website, HashMap<String, String> options, Prd prd) {
		HashSet<String> monthSet = this.getMonthSet(options);
		ArrayList<PrdDtl> prdDtlList = new ArrayList<PrdDtl>();
		Html prdDtlHtml = null;
		try{
//			prd.setTrDiv("W");
//			prd.setPrdUrl("http://www.ybtour.co.kr/Goods/overseas/inc_view_cal_dev.asp?good_type_cd=2&area_cd=10&good_yy=2009&good_seq=88");
			for (String month : monthSet){
				if(!"FW".contains(prd.getTrDiv())){
					String prdDtlSummaryUrl = prd.getPrdUrl().replace("thismonth", month.substring(4, 6));	
					
					Website prdDtlListSite = new Website();
					prdDtlListSite.setUrl(prdDtlSummaryUrl);
					prdDtlListSite.setMethod("GET");
					prdDtlListSite.setEncoding(website.getEncoding());
					
					Html prdDtlListHtml = new Html(this.getHtml(httpclient, prdDtlListSite));
					
					prdDtlListHtml = prdDtlListHtml.getTag("tbody");
					
					while(prdDtlListHtml.getTag("tr").toString().length() > 0){
						try{
							PrdDtl dtl = new PrdDtl();
							prdDtlHtml = prdDtlListHtml.getTag("tr");
	//						System.out.println("=======================================================");
							prdDtlListHtml = prdDtlListHtml.removeTag("tr");
							
							dtl.setTagnId(website.getId());
							dtl.setPrdNo(prd.getPrdNo());
							dtl.setDepDt(month.substring(0, 4) + prdDtlHtml.getTag("span").removeAllTags().getOnlyNumber().toString());
							prdDtlHtml.setHtml(prdDtlHtml.toString().replace(prdDtlHtml.getTag("span").toString(), ""));
							dtl.setArrDt(month.substring(0, 4) + prdDtlHtml.getTag("span").removeAllTags().getOnlyNumber().toString());
//							System.out.println("dep : " + dtl.getDepDt() + ", arr : " + dtl.getArrDt());
							prdDtlHtml.setHtml(prdDtlHtml.toString().replace(prdDtlHtml.getTag("span").toString(), ""));
							dtl.setArlnId(prdDtlHtml.toString().substring(prdDtlHtml.toString().indexOf(".gif") - 4, prdDtlHtml.toString().indexOf(".gif") - 2));
							dtl.setPrdSeq(prdDtlHtml.getValueByClass("lt").toString().split("ev_seq=")[1].split("&")[0]);
							dtl.setPrdUrl("http://www.ybtour.co.kr" + prdDtlHtml.getValueByClass("lt").findRegex("href=['\"][^\"]+").toString().replace("href=\"", ""));
							dtl.setPrdDtlNm(prdDtlHtml.getValueByClass("lt").removeAllTags().toString().trim());
							dtl.setPrdFeeAd(prdDtlHtml.getValueByClass("blue").removeAllTags().getOnlyNumber().toString());
							prdDtlHtml = prdDtlHtml.removeValueByClass("blue");
							prdDtlHtml = prdDtlHtml.removeTag("td");
							prdDtlHtml = prdDtlHtml.removeTag("td");
							prdDtlHtml = prdDtlHtml.removeTag("td");
							prdDtlHtml = prdDtlHtml.removeTag("td");
							prdDtlHtml = prdDtlHtml.removeTag("td");
							
							String sts = prdDtlHtml.getTag("td").removeAllTags().toString().trim();
							if (sts.contains("출발확정")){
								sts = "출발확정";
							}else if (sts.contains("예약마감")){
								sts = "예약마감";
							}else if (sts.contains("예약가능")){
								sts = "예약가능";
							}else if (sts.contains("예약대기")){
								sts = "대기예약";
							}else{
								sts = "예약가능";
							}
							
							dtl.setPrdSt(PRD_STATUS.get(sts));
							
							prdDtlList.add(dtl);
						}catch(Exception e){
							log("PrdDtl Parcing Exception : ", e.getStackTrace()[0].toString());
						}
					}
//					break;
				}else{
					// 자유여행, 허니문인 경우..
					String prdDtlSummaryUrl = prd.getPrdUrl();	
					
					Website prdDtlListSite = new Website();
					prdDtlListSite.setUrl(prdDtlSummaryUrl);
					prdDtlListSite.setMethod("GET");
					prdDtlListSite.setEncoding(website.getEncoding());
					
					Html prdDtlListHtml = new Html(this.getHtml(httpclient, prdDtlListSite));
					
					prdDtlListHtml = prdDtlListHtml.getTag("tbody");
					List<DayList> dayList = new ArrayList<DayList>();
					
					while(!prdDtlListHtml.getValueByClass("font_num").toString().trim().equals("")){
						try{
							
						}catch(Exception e){
							log("PrdDtl Parcing Exception : ", e.getStackTrace()[0].toString());
						}
					}
					
				}
			}
		}catch(Exception e){
			log("scrapPrdDtlSmmry", e.toString());
		}
		
		return prdDtlList;
	}
}
