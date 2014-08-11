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
import org.w3c.dom.Node;
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
		SubMenu submenu = new SubMenu();
		submenu.setDepcity(html.toString().split("startLocation=")[1].split("&")[0]);
		submenu.setLocation(html.toString().split("location=")[1].split("&")[0]);
		submenu.setLocation1(html.toString().split("location1=")[1].split("&")[0]);
		if(html.toString().toUpperCase().contains("THEME=")){
			submenu.setTheme(html.toString().split("Theme=")[1].split("&")[0]);
		}else{
			submenu.setTheme("");
		}
		if(html.toString().toUpperCase().contains("THEME1=")){
			submenu.setTheme1(html.toString().split("Theme1=")[1].split("&")[0]);
		}else{
			submenu.setTheme1("");
		}
		submenu.setLoc(html.toString().split("MLoc=")[1].split("\"")[0]);
		submenu.setName(html.getTag("span").removeAllTags().toString().trim());
		return submenu;
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
								try{
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
//									Website prdXmlPage = new Website(prdXmlUrl, "GET", website.getEncoding());
//									
//									Html prdXmlHtml = new Html(this.getHtml(httpclient, prdXmlPage));
//									System.out.println(prdXmlHtml.toString());
									
									// XML Document 객체 생성
									Document mainXml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(prdXmlUrl);
									
									// xpath 생성
									XPath xpath = XPathFactory.newInstance().newXPath();
									
									NodeList prdXmlList = (NodeList)xpath.evaluate("//Product", mainXml, XPathConstants.NODESET);
								    for(int idx = 0; idx<prdXmlList.getLength(); idx++){
								    	try{
									    	String prdno = prdXmlList.item(idx).getAttributes().getNamedItem("Pcode").getTextContent();
									    	if(prdCodeList.contains(prdno))
									    		continue;
									    	
									    	Prd prd = new Prd();
									    	prd.setLocation(submenu.getLocation());
									    	prd.setLocation1(submenu.getLocation1());
									    	prd.setTheme(submenu.getTheme());
									    	prd.setTheme1(submenu.getTheme1());
									    	prd.setDepArpt(submenu.getDepcity());
									    	prd.setLoc(submenu.getLoc());
									    	prd.setTagnId(website.getId());
									    	prd.setTrDiv(packageMap.get(menu.getName()));
									    	prd.setDmstDiv("A");
									    	prd.setPrdNo(prdno);
									    	
									    	Node node = prdXmlList.item(idx);
									    	NodeList childNode = node.getChildNodes();
									    	for(int i = 0; i<childNode.getLength(); i++){
	//								    		System.out.println(childNode.item(i).getNodeName());
									    		if(childNode.item(i).getNodeName().equals("Name")){
									    			NodeList dtlChildNode = childNode.item(i).getChildNodes();
	//								    			System.out.println(dtlChildNode.item(0).getTextContent());
									    			prd.setPrdNm(dtlChildNode.item(0).getTextContent());
									    		}else if(childNode.item(i).getNodeName().equals("Content")){
									    			NodeList dtlChildNode = childNode.item(i).getChildNodes();
	//								    			System.out.println(dtlChildNode.item(0).getTextContent());
									    			prd.setPrdDesc(dtlChildNode.item(0).getTextContent());
									    		}
									    	}
									    	
									    	prd.setAreaList(this.getAreaList(prd.getPrdNm() + " " + prd.getPrdDesc(), regionMenu));
									    	prd.setPrdUrl("http://www.modetour.com/Xml/Package/Get_Pcode.aspx?Ct=&Month=thismonth&Pcode=" + prd.getPrdNo() + "&Pd=&Type=01");
									    	
									    	prdCodeList.add(prd.getPrdNo());
									    	prdList.add(prd);
								    	}catch(Exception e){
								    		log("Prd Parcing Exception : ", e.getStackTrace()[0].toString());
								    	}
								    }
//								    break;
								}catch(Exception e){
									log("PrdList Exception : ", e.getStackTrace()[0].toString());
								}
							}
//							break;
						}
//						break;
					}
				}catch(Exception e){
					log("Menu Exception : ", e.getStackTrace()[0].toString());
				}
//				break;
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
				String prdDtlSummaryUrl = prd.getPrdUrl().replace("thismonth", month.substring(4, 6));	
				
				// XML Document 객체 생성
				Document mainXml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(prdDtlSummaryUrl);
				// xpath 생성
				XPath xpath = XPathFactory.newInstance().newXPath();
				
				NodeList dtlXmlList = (NodeList)xpath.evaluate("//SangList", mainXml, XPathConstants.NODESET);
				
				for(int idx=0; idx<dtlXmlList.getLength(); idx++){
					NodeList prdDtlNodeList = dtlXmlList.item(idx).getChildNodes();
					PrdDtl dtl = new PrdDtl();
					dtl.setTagnId(website.getId());
					dtl.setPrdNo(prd.getPrdNo());
					String depDay = "";
					String depTime = "";
					String arrDay = "";
					String arrTime = "";
					
					for(int i=0; i<prdDtlNodeList.getLength(); i++){
						Node dtlNode = prdDtlNodeList.item(i);
						String xmlTag = dtlNode.getNodeName();
						
						switch(xmlTag){
							case "SName":
								NodeList dtlNameNode = dtlNode.getChildNodes();
//								System.out.println(dtlNameNode.item(0).getTextContent());
								dtl.setPrdDtlNm(dtlNameNode.item(0).getTextContent());
								break;
							case "SNight":
//								System.out.println(dtlNode.getTextContent());
								dtl.setTrTermBak(dtlNode.getTextContent());
								break;
							case "SDay":
//								System.out.println(dtlNode.getTextContent());
								dtl.setTrTerm(dtlNode.getTextContent());
								break;
							case "SPrice":
//								System.out.println(dtlNode.getTextContent());
								dtl.setPrdFeeAd(dtlNode.getTextContent());
								break;
							case "SAirCode":
//								System.out.println(dtlNode.getTextContent());
								if(dtlNode.getTextContent().length() > 1)
									dtl.setArlnId(dtlNode.getTextContent().substring(0, 2));
								else
									dtl.setArlnId("");
								break;
							case "SAirName":
//								System.out.println(dtlNode.getTextContent());
								break;
							case "SPriceDay":
//								System.out.println(dtlNode.getTextContent());
								depDay = dtlNode.getTextContent();
								break;
							case "SArrivalDay":
//								System.out.println(dtlNode.getTextContent());
								arrDay = dtlNode.getTextContent();
								break;
							case "SPriceNum":
//								System.out.println(dtlNode.getTextContent());
								dtl.setPrdSeq(dtlNode.getTextContent());
								break;
							case "SMeet":
								if(!dtlNode.getTextContent().equals("33"))
									System.out.println(dtlNode.getTextContent());
								break;
							case "SstartAir":
//								System.out.println(dtlNode.getTextContent());
								break;
							case "SstartTime":
//								System.out.println(dtlNode.getTextContent());
								depTime = getOnlyNumber(dtlNode.getTextContent());
								break;
							case "SArrivalTime":
//								System.out.println(dtlNode.getTextContent());
								arrTime = getOnlyNumber(dtlNode.getTextContent());
								break;
							case "Sbooking":
//								System.out.println(dtlNode.getTextContent());
								break;
							case "SPrefixName":
								break;
							case "SDetailState":
//								System.out.println(dtlNode.getTextContent());
								String sts = "";
								if(dtlNode.getTextContent().equals("gray")){
									sts = "예약마감";
								}else if(dtlNode.getTextContent().equals("blue")){
									sts = "예약가능";
								}else if(dtlNode.getTextContent().equals("red")){
									sts = "출발확정";
								}else if(dtlNode.getTextContent().equals("green")){
									sts = "대기예약";
								}
								dtl.setPrdSt(PRD_STATUS.get(sts));
								break;
						}
					}
					
					dtl.setDepArpt(prd.getDepArpt());
					String prdUrl = "http://www.modetour.com/Package/Itinerary.aspx?startLocation=" 
										+ dtl.getDepArpt() + "&location=" + prd.getLocation() 
										+ "&location1=" + prd.getLocation1() + "&theme=" + prd.getTheme()
										+ "&theme1=" + prd.getTheme1() + "&MLoc=" + prd.getLoc() + "&Pnum=" + dtl.getPrdSeq(); 
					dtl.setPrdUrl(prdUrl);
					dtl.setDepDt(depDay + depTime);
					dtl.setArrDt(arrDay + arrTime);
					prdDtlList.add(dtl);
				}
			}
		}catch(Exception e){
			log("scrapPrdDtlSmmry", e.toString());
		}
		
		return prdDtlList;
	}
}
