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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ModetourHandler extends _TouristAgencyHandler{

	public ModetourHandler(){}
	
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
			
			Html overseasHtml = mainHtml.getValueByClass("overseas");
			Html domesticHtml = mainHtml.getValueByClass("domestic");
			
			List<MenuUrl> menuList = new ArrayList<MenuUrl>();
			
			while(!overseasHtml.getTag("ul").getTag("li").toString().trim().equals("")){
				Html menuHtml = overseasHtml.getTag("ul").getTag("li");
				MenuUrl menu = new MenuUrl();
				menu.setDepcity(menuHtml.toString().split("startLocation=")[1].split("&")[0]);
				menu.setId(menuHtml.toString().split("id=")[1].split("&")[0]);
				menu.setType(menuHtml.toString().split("type=")[1].split("&")[0]);
				menu.setLoc(menuHtml.toString().split("MLoc=")[1].split("\"")[0]);
				menu.setName(menuHtml.getTag("span").removeAllTags().toString().trim());
				menuList.add(menu);
			}
			
			while(!domesticHtml.getTag("ul").getTag("li").toString().trim().equals("")){
				Html menuHtml = overseasHtml.getTag("ul").getTag("li");
				MenuUrl menu = new MenuUrl();
				menu.setDepcity(menuHtml.toString().split("startLocation=")[1].split("&")[0]);
				menu.setId(menuHtml.toString().split("id=")[1].split("&")[0]);
				menu.setType(menuHtml.toString().split("type=")[1].split("&")[0]);
				menu.setLoc(menuHtml.toString().split("MLoc=")[1].split("\"")[0]);
				menu.setName(menuHtml.getTag("span").removeAllTags().toString().trim());
				menuList.add(menu);
			}
			
			System.out.println("");
			
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
