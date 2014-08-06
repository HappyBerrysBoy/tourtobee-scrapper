package hnc.tourtobee.scrapper.handler.website;

import static hnc.tourtobee.code.Codes.ARPT_NAME_CODE;
import static hnc.tourtobee.code.Codes.PRD_CLASS;
import static hnc.tourtobee.code.Codes.PRD_STATUS;
import static hnc.tourtobee.code.Codes.WEEK_DAY_NUMBER;
import static hnc.tourtobee.util.Util.log;
import hnc.tourtobee.scrapper.dataobject.Prd;
import hnc.tourtobee.scrapper.dataobject.PrdDtl;
import hnc.tourtobee.scrapper.dataobject.TtrTrArea;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.xml.sax.InputSource;


public class YellowBalloonHandler extends _TouristAgencyHandler {
	
	class Submenu{
		private String code;
		private String name;
		private String goodtypecd;
		private String sbar;
		
		String getCode() {
			return code;
		}
		void setCode(String code) {
			this.code = code;
		}
		String getName() {
			return name;
		}
		void setName(String name) {
			this.name = name;
		}
		String getGoodtypecd() {
			return goodtypecd;
		}
		void setGoodtypecd(String goodtypecd) {
			this.goodtypecd = goodtypecd;
		}
		String getSbar() {
			return sbar;
		}
		void setSbar(String sbar) {
			this.sbar = sbar;
		}
		
	}
	
	@Override
	public ArrayList<Prd> scrapPrdList(CloseableHttpClient httpclient, Website website, HashMap<String, String> options, HashSet<String> insPrds) {
		
		ArrayList<Prd> prdList = new ArrayList<Prd>();
		
		try{
			
			HashMap<String, String> urlMap = new HashMap<String, String>();
			urlMap.put("A01", "overseas");
			urlMap.put("A03", "airtel");
			urlMap.put("A06", "Honeymoon");
			urlMap.put("A09", "Overseas");		// Golf
			urlMap.put("A12", "Overseas");		// Domestic but 주소는 overseas를 사용
			urlMap.put("A15", "Overseas");		// 지역 출발 but 주소는 overseas를 사용
			urlMap.put("A18", "Overseas");		// Cruise but 주소는 overseas를 사용
		    
		    HashMap<String, String> packageMap = new HashMap<String, String>();
		    packageMap.put("A01", "P");
		    packageMap.put("A03", "F");
		    packageMap.put("A06", "W");
		    packageMap.put("A09", "G");
		    packageMap.put("A12", "D");
		    packageMap.put("A15", "P");
		    packageMap.put("A18", "C");
		    
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
//						prdUrl = "http://www.ybtour.co.kr/Goods/overseas/list.asp?sub_area_cd=1794";
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
						
						if (!menu.equals("A03") && !menu.equals("A06")){
							log("  Prd Scrapping", "Menu " + urlMap.get(menu) + " Scrapping");
							while(prdHtml.getValueByClass("travelList_wrap").toString().contains("travelList_wrap")){
								Html prdDtlHtml = prdHtml.getValueByClass("travelList_wrap");
								
								if(prdDtlHtml.toString().equals(""))
									break;
								
								prdHtml = prdHtml.removeValueByClass("travelList_wrap");
								Prd prd = new Prd();
								String prdName = prdDtlHtml.findRegex("title=['\"][^\"]+").toString().replace("title=\"", "");
								String prdDesc = prdDtlHtml.getValueByClass("route").removeAllTags().toString();
								String prdNo = prdDtlHtml.getTag("a").toString().split("'")[1];
								prd.setTagnId(website.getId());
								prd.setPrdNo(prdNo);
								prd.setPrdNm(prdName);
								prd.setTrDiv(packageMap.get(menu));
								prd.setDmstDiv(dmstDiv);
								prd.setPrdDesc(prdDesc);
								prd.setAreaList(this.getAreaList(prdName + " " + prdDesc, detailmenu.getName()));
								prd.setDepArpt(depArpt);
								prd.setPrdUrl("http://www.ybtour.co.kr/Goods/" + urlMap.get(menu) + "/inc_evList_ajax.asp?goodCD=" + prdNo + "&startDT=");
								prdList.add(prd);
//								System.out.println(website.getId());
//								System.out.println(prdDtlHtml.getTag("a").toString().split("'")[1]);
//								System.out.println(prdDtlHtml.findRegex("title=['\"][^\"]+").toString().replace("title=\"", ""));
//								System.out.println(packageMap.get(menu));
//								System.out.println(dmstDiv);
//								System.out.println(prdDtlHtml.getValueByClass("route").removeAllTags().toString());
//								System.out.println(depArpt);
//								ArrayList tmpArray = this.getAreaList(getAttr(prdDtlHtml.getTag("img").toString(), "title"), prdDtlHtml.getValueByClass("route").removeAllTags().toString());
//								for(int area=0; area<tmpArray.size(); area++){
//									TtrTrArea t = (TtrTrArea)tmpArray.get(area);
//									System.out.println(t.getTrCntt());
//									System.out.println(t.getTrNtCd());
//									System.out.println(t.getTrCityCd());
//									System.out.println(t.getSiteCd());
//								}
							}
							log("  Prd Scrapping", String.valueOf(prdList.size()) + " Prds Scrapped");
						}else{
							
						}
						
//						break;
					}
//					break;
				}
				break;
			}
		}catch(Exception e){
			e.printStackTrace();
			log(this.getClass().getName() + " - scrapPrdList", e.toString());
		}
		return prdList;
	}
	
	private String getOnlyNumber(String html){
		String result = html;
		Pattern tag = Pattern.compile("[^0-9]+");
		Matcher mat = tag.matcher(result);
		result = mat.replaceAll("");
		
		return result;
	}
	
	@Override
	public ArrayList<PrdDtl> scrapPrdDtlSmmry(CloseableHttpClient httpclient, Website website, HashMap<String, String> options, Prd prd) {
		HashSet<String> monthSet = this.getMonthSet(options);
		ArrayList<PrdDtl> prdDtlList = new ArrayList<PrdDtl>();
		Html prdDtlHtml = null;
		try{
			for (String month : monthSet){
				String prdDtlSummaryUrl = prd.getPrdUrl() + month;	
				
				Website prdDtlListSite = new Website();
				prdDtlListSite.setUrl(prdDtlSummaryUrl);
				prdDtlListSite.setMethod("GET");
				prdDtlListSite.setEncoding(website.getEncoding());
				
				Html prdDtlListHtml = new Html(this.getHtml(httpclient, prdDtlListSite));
				
				prdDtlListHtml = prdDtlListHtml.getTag("tbody");
				
				while(prdDtlListHtml.getTag("tr").toString().length() > 0){
					PrdDtl dtl = new PrdDtl();
					prdDtlHtml = prdDtlListHtml.getTag("tr");
//					System.out.println("=======================================================");
					prdDtlListHtml = prdDtlListHtml.removeTag("tr");
					
					dtl.setTagnId(website.getId());
					dtl.setPrdNo(prd.getPrdNo());
					dtl.setDepDt(month.substring(0, 4) + getOnlyNumber(prdDtlHtml.getTag("span").removeAllTags().toString()));
					prdDtlHtml.setHtml(prdDtlHtml.toString().replace(prdDtlHtml.getTag("span").toString(), ""));
					dtl.setArrDt(month.substring(0, 4) + getOnlyNumber(prdDtlHtml.getTag("span").removeAllTags().toString()));
					prdDtlHtml.setHtml(prdDtlHtml.toString().replace(prdDtlHtml.getTag("span").toString(), ""));
					dtl.setArlnId(prdDtlHtml.toString().substring(prdDtlHtml.toString().indexOf(".gif") - 4, prdDtlHtml.toString().indexOf(".gif") - 2));
					dtl.setPrdSeq(prdDtlHtml.getValueByClass("lt").toString().split("ev_seq=")[1].split("&")[0]);
					dtl.setPrdUrl("http://www.ybtour.co.kr" + prdDtlHtml.getValueByClass("lt").findRegex("href=['\"][^\"]+").toString().replace("href=\"", ""));
					dtl.setPrdDtlNm(prdDtlHtml.getValueByClass("lt").removeAllTags().toString().trim());
					dtl.setPrdFeeAd(getOnlyNumber(prdDtlHtml.getValueByClass("blue").removeAllTags().toString()));
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
				}
				//break;
			}
		}catch(Exception e){
			log("scrapPrdDtlSmmry", e.toString());
		}
		
		return prdDtlList;
	}
}
