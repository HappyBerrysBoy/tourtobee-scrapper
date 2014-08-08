package hnc.tourtobee.scrapper.handler.website;

import static hnc.tourtobee.code.Codes.PRD_STATUS;
import static hnc.tourtobee.util.Util.log;
import hnc.tourtobee.scrapper.dataobject.Prd;
import hnc.tourtobee.scrapper.dataobject.PrdDtl;

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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class YellowBalloonHandler extends _TouristAgencyHandler {
	
	class Submenu{
		private String code;
		private String name;
		private String goodtypecd;
		private String sbar;
		
		private String getCode() {
			return code;
		}
		private void setCode(String code) {
			this.code = code;
		}
		private String getName() {
			return name;
		}
		private void setName(String name) {
			this.name = name;
		}
		private String getGoodtypecd() {
			return goodtypecd;
		}
		private void setGoodtypecd(String goodtypecd) {
			this.goodtypecd = goodtypecd;
		}
		private String getSbar() {
			return sbar;
		}
		private void setSbar(String sbar) {
			this.sbar = sbar;
		}
	}
	
	class DayList{
		private String day;
		private String status;
		private String url;
		
		private String getDay() {
			return day;
		}
		private void setDay(String day) {
			this.day = day;
		}
		private String getStatus() {
			return status;
		}
		private void setStatus(String status) {
			this.status = status;
		}
		private String getUrl() {
			return url;
		}
		private void setUrl(String url) {
			this.url = url;
		}
		
	}
	
	HashMap<String, String> urlMap = new HashMap<String, String>();
    
    HashMap<String, String> packageMap = new HashMap<String, String>();
    
    public YellowBalloonHandler(){
    	urlMap.put("A01", "overseas");
		urlMap.put("A03", "airtel");
		urlMap.put("A06", "Honeymoon");
		urlMap.put("A09", "Overseas");		// Golf
		urlMap.put("A12", "Overseas");		// Domestic but 주소는 overseas를 사용
		urlMap.put("A15", "Overseas");		// 지역 출발 but 주소는 overseas를 사용
		urlMap.put("A18", "Overseas");		// Cruise but 주소는 overseas를 사용
		
		packageMap.put("A01", "P");
	    packageMap.put("A03", "F");
	    packageMap.put("A06", "W");
	    packageMap.put("A09", "G");
	    packageMap.put("A12", "D");
	    packageMap.put("A15", "P");
	    packageMap.put("A18", "C");
    }
	
	@Override
	public ArrayList<Prd> scrapPrdList(CloseableHttpClient httpclient, Website website, HashMap<String, String> options, HashSet<String> insPrds) {
		
		ArrayList<Prd> prdList = new ArrayList<Prd>();
		
		try{
			// XML Document 객체 생성
			Document mainXml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(website.getUrl() + "?ML=1&MCD=");
			
			// xpath 생성
			XPath xpath = XPathFactory.newInstance().newXPath();
			
			List<String> listMenu = new ArrayList<String>(); 
			NodeList menuList = (NodeList)xpath.evaluate("//List/MenuCD", mainXml, XPathConstants.NODESET);
		    for(int idx = 0; idx<menuList.getLength(); idx++){
		    	listMenu.add(menuList.item(idx).getTextContent());
		    }
			
			for(String menu : listMenu){
				Document submenuXml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(website.getUrl() + "?ML=2&MCD=" + menu);
				NodeList submenuNodeList = (NodeList)xpath.evaluate("//List/MenuCD", submenuXml, XPathConstants.NODESET);
				
				// Honeymoon debug 위한 부분... 
				if(menu.equals("A03"))
					continue;
//				if(!menu.equals("A06"))
//					continue;
				
				List<String> listSubmenu = new ArrayList<String>();
				for(int i=0; i<submenuNodeList.getLength(); i++){
					listSubmenu.add(submenuNodeList.item(i).getTextContent());
				}
				
				for(String submenu : listSubmenu){
					Document detailmenuXml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(website.getUrl() + "?ML=3&MCD=" + submenu);
					NodeList detailmenuNodeList = (NodeList)xpath.evaluate("//List", detailmenuXml, XPathConstants.NODESET);
					
					List<Submenu> listdetailmenu = new ArrayList<Submenu>();
					for(int j=0; j<detailmenuNodeList.getLength(); j++){
						Node detailNode = detailmenuNodeList.item(j);
						
						if(detailNode.getNodeType() == Node.ELEMENT_NODE){
							Element nodeElmnt = (Element)detailNode;
							Submenu sub = new Submenu();
							
							sub.setCode(nodeElmnt.getElementsByTagName("MenuCD").item(0).getFirstChild().getTextContent());
							sub.setName(nodeElmnt.getElementsByTagName("MenuNM").item(0).getFirstChild().getTextContent());
							sub.setGoodtypecd(nodeElmnt.getElementsByTagName("GoodTypeCD").item(0).getFirstChild().getTextContent());
							sub.setSbar(nodeElmnt.getElementsByTagName("SBAR").item(0).getFirstChild().getTextContent());
							listdetailmenu.add(sub);
						}
					}
					
					for(Submenu detailmenu : listdetailmenu){
						try{
							String depArpt = "";
							String dmstDiv = "";
							
							if(menu.equals("A15")){
								depArpt = "PUS";
							}else{
								depArpt = "ICN";
							}
							
							if(menu.equals("A12")){
								dmstDiv = "D";
							}else{
								dmstDiv = "A";
							}
							
							if(detailmenu.getSbar().trim().equals(""))
								continue;
							
							String prdUrl = "http://www.ybtour.co.kr/Goods/" + urlMap.get(menu) + "/list.asp?sub_area_cd=" + detailmenu.getSbar();
//							prdUrl = "http://www.ybtour.co.kr/Goods/Overseas/list.asp?sub_area_cd=K803";
							Website prdSite = new Website();
							prdSite.setUrl(prdUrl);
							prdSite.setMethod("GET");
							prdSite.setEncoding(website.getEncoding());
							log(website.getId() + " - Product Url", prdUrl);
							
							Html prdHtml = new Html(this.getHtml(httpclient, prdSite));
							prdHtml = prdHtml.getValueByClass("travel_top_section");
							if (prdHtml.toString().equals(""))
								continue;
							
							prdHtml = new Html(prdHtml.toString().substring(2));
							//System.out.println(prdHtml.toString());
							
							// 자유여행, 허니문의 경우 양식이 다르다..
							log("  Prd Scrapping", "Menu " + urlMap.get(menu) + " Scrapping");
							while(prdHtml.getValueByClass("travelList_wrap").toString().contains("travelList_wrap")){
								try{
									Html prdDtlHtml = prdHtml.getValueByClass("travelList_wrap");
									
									if(prdDtlHtml.toString().equals(""))
										break;
									
									prdHtml = prdHtml.removeValueByClass("travelList_wrap");
									Prd prd = new Prd();
									String prdName = prdDtlHtml.findRegex("title=['\"][^\"]+").toString().replace("title=\"", "");
									String prdDesc = prdDtlHtml.getValueByClass("route").removeAllTags().toString();
									String prdNo = "";
									String prdDetailUrl = "";
									if (!menu.equals("A03") && !menu.equals("A06")){
										prdNo = prdDtlHtml.getTag("a").toString().split("'")[1];
										prdDetailUrl = "http://www.ybtour.co.kr/Goods/" + urlMap.get(menu) + "/inc_evList_ajax.asp?goodCD=" + prdNo + "&startDT=";
									}else{
										prdNo = prdDtlHtml.toString().split("good_cd=")[1].split("&")[0];
										prdDetailUrl = "http://www.ybtour.co.kr/Goods/overseas/inc_view_cal_dev.asp?good_type_cd="
												+ prdNo.substring(0, 1)
												+ "&area_cd=" + prdNo.substring(1, 3)
												+ "&good_yy=" + prdNo.substring(3, 7)
												+ "&good_seq=" + prdNo.substring(7);
									}
									
									prd.setTagnId(website.getId());
									prd.setPrdNo(prdNo);
									prd.setPrdNm(prdName);
									prd.setTrDiv(packageMap.get(menu));
									prd.setDmstDiv(dmstDiv);
									prd.setPrdDesc(prdDesc);
									prd.setAreaList(this.getAreaList(prdName + " " + prdDesc, detailmenu.getName()));
									prd.setDepArpt(depArpt);
									prd.setPrdUrl(prdDetailUrl);
									prdList.add(prd);
								}catch(Exception e){
									log("Prd Detail Parcing Exception : ", e.getStackTrace()[0].toString());
								}
							}
							log("  Prd Scrapping", String.valueOf(prdList.size()) + " Prds Scrapped");
						}catch(Exception e){
							log("Prd Menu Parcing Exception : ", e.getStackTrace()[0].toString());
						}
						break;
					}
					break;
				}
				break;
			}
		}catch(Exception e){
			e.printStackTrace();
			log(this.getClass().getName() + " - scrapPrdList", e.toString());
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
					String prdDtlSummaryUrl = prd.getPrdUrl() + month;	
					
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
							prdDtlHtml = prdDtlListHtml.getValueByClass("font_num");
							
							if(prdDtlHtml.toString().equals(""))
								break;
							
							prdDtlListHtml = prdDtlListHtml.removeValueByClass("font_num");
							
							if(!prdDtlHtml.toString().contains("strong"))
								continue;
							
							DayList day = new DayList();
							String detailUrl = "http://www.ybtour.co.kr/goods/" + prd.getPrdUrl().split("oods/")[1].split("/")[0]
												+ "/" + prdDtlHtml.getTag("a").findRegex("href=['\"][^\"]+").toString().replace("href=\"", "");
							String status = prdDtlHtml.getTag("font").toString().toUpperCase();
							if (status.contains("#EC1515")){
								status = "예약가능";
							}else if(status.contains("#1592EC")){		// 대기예약 색상 정확하진 않음..
								status = "대기예약";
							}else if(status.contains("#666666")){
								status = "예약마감";
							}else{
								status = "예약가능";
							}
							day.setStatus(PRD_STATUS.get(status));
							day.setUrl(detailUrl);
							day.setDay(prdDtlHtml.getTag("font").removeAllTags().toString().trim());
							dayList.add(day);
							
	//						break;
						}catch(Exception e){
							log("PrdDtl Parcing Exception : ", e.getStackTrace()[0].toString());
						}
					}
					
					List<DayList> anodayList = new ArrayList<DayList>();
					getPrdDtlList(httpclient, website, prdDtlList, prd.getPrdNo(), dayList, anodayList, true);
					getPrdDtlList(httpclient, website, prdDtlList, prd.getPrdNo(), anodayList, null, false);
				}
			}
		}catch(Exception e){
			log("scrapPrdDtlSmmry", e.toString());
		}
		
		return prdDtlList;
	}
	
	private void getPrdDtlList(CloseableHttpClient httpclient, Website website, ArrayList<PrdDtl> getPrdDtl, String prdno, List<DayList> dayList, List<DayList> adddayList, Boolean first){
		for(DayList day : dayList){
			try{
				String prdDtlUrl = day.getUrl();	
				
				Website prdDtlDayListSite = new Website();
				prdDtlDayListSite.setUrl(prdDtlUrl);
				prdDtlDayListSite.setMethod("GET");
				prdDtlDayListSite.setEncoding(website.getEncoding());
				
				Html prdDtlDayListHtml = new Html(this.getHtml(httpclient, prdDtlDayListSite));
				
				prdDtlDayListHtml = prdDtlDayListHtml.getValueByClass("info_wrap");
				
				PrdDtl dtl = new PrdDtl();
				dtl.setTagnId(website.getId());
				dtl.setPrdNo(prdno);
				dtl.setPrdDtlNm(prdDtlDayListHtml.getTag("h2").removeAllTags().toString().trim());
				
				int depSubIdx = 0;
				Html depTimeHtml = new Html(prdDtlDayListHtml.getTag("tr").getTag("td").toString().split("<br")[0]);
				String depTime = depTimeHtml.removeAllTags().toString().trim();
				if (!depTime.contains("[") || !depTime.contains("]") || getOnlyNumber(depTime).length() < 12){
					depSubIdx = 8;
				}else if(getOnlyNumber(depTime.split("\\[")[1].split("\\]")[0]).length() < 4){
					depSubIdx = 8;
				}else{
					depSubIdx = 12;
				}
				dtl.setDepDt(getOnlyNumber(depTime).substring(0, depSubIdx));
				
				prdDtlDayListHtml = prdDtlDayListHtml.removeTag("tr");
				
				int arrSubIdx = 0;
				Html arrTimeHtml = new Html(removeAllTags(prdDtlDayListHtml.getTag("tr").getTag("td").toString().split("<br")[1]).trim());
				String arrTime = arrTimeHtml.removeAllTags().toString().trim();
				if(!arrTime.contains("[") || !arrTime.contains("]") || getOnlyNumber(arrTime).length() < 12){
					arrSubIdx = 8;
				}else if(getOnlyNumber(arrTime.split("\\[")[1].split("\\]")[0]).length() < 4){
					arrSubIdx = 8;
				}else{
					arrSubIdx = 12;
				}
				dtl.setArrDt(getOnlyNumber(arrTime).substring(0, arrSubIdx));
				
				prdDtlDayListHtml = prdDtlDayListHtml.removeTag("tr");
				
//				System.out.println("dep : " + dtl.getDepDt() + ", arr : " + dtl.getArrDt());
				dtl.setPrdFeeAd(prdDtlDayListHtml.getTag("em").removeAllTags().getOnlyNumber().toString().trim());
				prdDtlDayListHtml = prdDtlDayListHtml.removeTag("tr");
				String airCode = prdDtlDayListHtml.getTag("td").toString();
				dtl.setArlnId(airCode.substring(airCode.indexOf(".gif") - 4, airCode.indexOf(".gif") - 2));
				dtl.setPrdSeq(day.getUrl().split("ev_seq=")[1]);
				dtl.setPrdUrl(prdDtlUrl);
				dtl.setPrdSt(day.getStatus());
				getPrdDtl.add(dtl);
				
				if (first){
					prdDtlDayListHtml = prdDtlDayListHtml.removeTag("tbody");
					prdDtlDayListHtml = prdDtlDayListHtml.removeTag("thead");
					while(!prdDtlDayListHtml.getTag("tr").toString().trim().equals("")){
						prdDtlDayListHtml = prdDtlDayListHtml.removeTag("tr");
						if(!prdDtlDayListHtml.getTag("tr").getTag("td").toString().trim().equals("")){
							DayList addDay = new DayList();
							addDay.setDay(day.getDay());
							addDay.setStatus(day.getStatus());
							addDay.setUrl(day.getUrl().split("ev_seq=")[0] + "ev_seq=" + prdDtlDayListHtml.getTag("tr").getTag("td").toString().split("goGoodsView")[1].split(",")[1].split("\\)")[0].trim());
							adddayList.add(addDay);
						}
					}
				}
			}catch(Exception e){
				log("getPrdDtlList Exception : ", e.getStackTrace()[0].toString());
			}
		}
	}
}
