package hnc.tourtobee.scrapper.handler.website;

import hnc.tourtobee.scrapper.dataobject.Prd;
import hnc.tourtobee.scrapper.dataobject.PrdDtl;
import hnc.tourtobee.scrapper.dataobject.TtrTrArea;
import hnc.tourtobee.util.Util;
import static hnc.tourtobee.code.Codes.PRD_CLASS;
import static hnc.tourtobee.code.Codes.WEEK_DAY_NUMBER;
import static hnc.tourtobee.code.Codes.PRD_STATUS;
import static hnc.tourtobee.code.Codes.ARPT_NAME_CODE;
import static hnc.tourtobee.code.Codes.findGetAreaString;
import static hnc.tourtobee.util.Util.getSystemMonth;
import static hnc.tourtobee.util.Util.log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jh.project.httpscrapper.Website;
import jh.project.httpscrapper.handler.website._WebsiteHandler;
import jh.project.httpscrapper.util.Html;

import org.apache.http.impl.client.CloseableHttpClient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;



public class KRTHandler extends _TouristAgencyHandler{
	
	
	@Override
	public ArrayList<Prd> scrapPrd(CloseableHttpClient httpclient, Website website, HashMap<String, String> options, HashSet<String> insPrds) {
		ArrayList<Prd> prdList = new ArrayList<Prd>();
		try {
			HashSet<String> prdUrls = new HashSet<String>();
			ArrayList<Menu> menuList = getMenuUrls(this.getHtml(httpclient, website));
			
			for (Menu menu : menuList){
				String menuUrl = menu.mLink;
				Website subSite = new Website();
				subSite.setUrl(menuUrl);
				subSite.setMethod(website.getMethod());
				subSite.setEncoding(website.getEncoding());
				prdUrls.addAll(getPrdUrls(this.getHtml(httpclient, subSite)));
				
				log(menu.mMenu, String.valueOf(prdUrls.size()));
				
				int prdCnt = 0;
				for(String prdUrl : prdUrls){
					prdCnt++;
					Prd prd = new Prd();
					
					String prdNo = prdUrl.split("good_cd")[1].split("&")[0].replace("=", "");
					
					if ((insPrds != null && insPrds.contains(prdNo))) continue; 
					
					Website prdSite = new Website();
					prdSite.setUrl("http://www.krt.co.kr" + prdUrl);
					prdSite.setMethod(website.getMethod());
					prdSite.setEncoding(website.getEncoding());
					String prdHtml = this.removeComment(this.getHtml(httpclient, prdSite));
					
					prd.setTagnId("KRT");
					prd.setPrdNo(prdNo);
					prd.setPrdUrl(prdSite.getUrl());
					prd.setPrdNm(this.removeAllTags(this.getValueByClass(prdHtml, "tit_text")));
					
					if (menu.mD1code.equals("G3")){
						prd.setTrDiv(PRD_CLASS.get("허니문"));
						prd.setDmstDiv("A");
					}else if (menu.mD1code.equals("G5")){
						prd.setTrDiv(PRD_CLASS.get("국내"));
						prd.setDmstDiv("D");
					}else if (menu.mD1code.equals("G7")){
						prd.setTrDiv(PRD_CLASS.get("골프"));
						prd.setDmstDiv("A");
					}else {
						prd.setTrDiv(PRD_CLASS.get("패키지"));
						prd.setDmstDiv("A");
					}
					
					ArrayList<String> areaCodeList = findGetAreaString(prd.getPrdNm());
					if (areaCodeList.size() <= 0) areaCodeList = findGetAreaString(menu.mMenu);
					ArrayList<TtrTrArea> areaList = new ArrayList<TtrTrArea>();
					for (String areaCode :  areaCodeList){
						TtrTrArea area = new TtrTrArea();
						String[] areaCodeSplit = areaCode.split("/");
						area.setTrCityCd(areaCodeSplit[0]);
						area.setTrNtCd(areaCodeSplit[1]);
						area.setTrCntt(areaCodeSplit[2]);
						areaList.add(area);
					}
					prd.setAreaList(areaList);
					
					prdList.add(prd);
					log("  " + prd.getPrdNo(), String.valueOf(prdCnt) + "/" + String.valueOf(prdUrls.size()));
				}

			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return prdList;
	}
	

	@Override
	public ArrayList<PrdDtl> scrapPrdDtlSmmry(CloseableHttpClient httpclient, Website website, HashMap<String, String> options, String prdUrl, String prdNo) {
		
		HashSet<String> monthSet = new HashSet<String>();
		if (options != null){
			if (options.get("until") != null){
				monthSet = Util.getYearMonthSet(options.get("until"));
			}
			if (options.get("month") != null){
				monthSet.add(options.get("month"));
			}
		}else{
			monthSet.add(getSystemMonth());
		}
		
		ArrayList<PrdDtl> prdDtlList = new ArrayList<PrdDtl>();
		for (String month : monthSet){
			String prtType = prdUrl.split("/")[prdUrl.split("/").length - 1].split("_")[0];

			String prdDtlListUrl = "http://www.krt.co.kr/_inc2014/_StartList.asp?"
							+ "param1=" + prdNo
							+ "&param2=" + month
							+ "&param3=" + getSystemMonth()
							+ "&param4=" + prtType
							+ "&param5=0"
							+ "&param6=" + getSystemMonth();
			Website prdDtlListSite = new Website();
			prdDtlListSite.setUrl(prdDtlListUrl);
			prdDtlListSite.setMethod("GET");
			prdDtlListSite.setEncoding(website.getEncoding());
			
			Html prdDtlListHtml = new Html(this.getHtml(httpclient, prdDtlListSite));
			prdDtlListHtml = prdDtlListHtml.getTag("div").getTag("table");
			
			while (true){
				prdDtlListHtml = new Html(prdDtlListHtml.toString().substring(1));
				String subHtmlStr = prdDtlListHtml.getTag("table").toString();
				
				if (subHtmlStr.trim().length() <= 0) break;
				
				Html subHtml = new Html(subHtmlStr);
				String depDt = subHtml.getTag("TD").removeAllTags().convertSpecialChar().toString();
				String wkDay =  depDt.split("\\(")[1].substring(0, 1);
				depDt = month.substring(0, 4) + depDt.split("\\(")[0].replace("/", "").trim();
				
				String feeAd = subHtml.removeTag("TD").getTag("TD").removeAllTags().convertSpecialChar().toString();
				feeAd = feeAd.replace(",", "").trim();
				String seq = subHtml.removeTag("TD").removeTag("TD").getTag("TD").toString();
				String seqArr[] = seq.split("((?i)href)=[\"\']")[1].split("((?i)catNum)=")[1].split("_");
				seq = seqArr[4] + "_" + seqArr[5];
				String name = subHtml.removeTag("TD").removeTag("TD").getTag("TD").removeAllTags().convertSpecialChar().toString().trim();
				String airLine = subHtml.removeTag("TD").removeTag("TD").removeTag("TD").getTag("TD").removeAllTags().toString();
				airLine = airLine.split("alt=[\"\']")[1];
				airLine = airLine.split("\\.")[0];
				String depTm = subHtml.removeTag("TD").removeTag("TD").removeTag("TD").getTag("TD").removeAllTags().convertSpecialChar().toString();
				depTm = depTm.replaceAll("[\\(|\\)|:]*", "").trim();
				String prdSt = subHtml.removeTag("TD").removeTag("TD").removeTag("TD").removeTag("TD").getTag("TD").removeAllTags().convertSpecialChar().toString().trim();
				if (prdSt.equals("예약마감")){
					prdSt = "예약마감";
				}else if (prdSt.equals("예약대기")){
					prdSt = "대기예약";
				}else if (prdSt.equals("예약가능")){
					prdSt = "예약가능";
				}else if (prdSt.equals("출발예정")){
					prdSt = "예약가능";
				}else if (prdSt.equals("출발확정")){
					prdSt = "출발확정";
				}else if (prdSt.equals("마감임박")){
					prdSt = "예약가능";
				}else{
					prdSt = "예약가능";
				}
				
				prdDtlListHtml = prdDtlListHtml.removeTag("tr");
				
//				System.out.println(depDt + "/" + wkDay + "/" + feeAd + "/" + seq + "/" + name + "/" + depTm + "/" + prdSt);
//				System.out.println("==================================");
				PrdDtl prdDtl = new PrdDtl();
				prdDtl.setPrdNo(prdNo);
				prdDtl.setPrdSeq(seq);
				prdDtl.setDepDt(depDt + depTm);
				prdDtl.setDepDtYmd(depDt);
				prdDtl.setDepDtHm(depTm);
				prdDtl.setDepDtWd(WEEK_DAY_NUMBER.get(wkDay));
				prdDtl.setPrdDtlNm(name);
				prdDtl.setArlnId(airLine);
				prdDtl.setPrdFeeAd(feeAd);
				prdDtl.setPrdSt(prdSt);
				
				prdDtlList.add(prdDtl);
			}
		}
		
		return prdDtlList;
	}





	@Override
	public ArrayList getResult(CloseableHttpClient httpclient, Website website, HashMap<String, String> options) {
		ArrayList<Prd> prdList = new ArrayList<Prd>();
		try {
			HashSet<String> prdUrls = new HashSet<String>();
			ArrayList<Menu> menuList = getMenuUrls(this.getHtml(httpclient, website));

			for (Menu menu : menuList){
				String menuUrl = menu.mLink;
				Website subSite = new Website();
				subSite.setUrl(menuUrl);
				subSite.setMethod(website.getMethod());
				subSite.setEncoding(website.getEncoding());
				prdUrls.addAll(getPrdUrls(this.getHtml(httpclient, subSite)));
				
				
				log("\tTotal Product", String.valueOf(prdUrls.size()));
				
				int prdCnt = 0;
				for(String prdUrl : prdUrls){
					prdCnt++;
					Prd prd = new Prd();
					
					Website prdSite = new Website();
					prdSite.setUrl("http://www.krt.co.kr" + prdUrl);
					prdSite.setMethod(website.getMethod());
					prdSite.setEncoding(website.getEncoding());
					String prdHtml = this.removeComment(this.getHtml(httpclient, prdSite));
					
					String prdNo = prdUrl.split("good_cd")[1].split("&")[0].replace("=", "");
					
					
					prd.setTagnId("KRT");
					prd.setPrdNo(prdNo);
					prd.setPrdUrl(prdSite.getUrl());
					prd.setPrdNm(this.removeAllTags(this.getValueByClass(prdHtml, "tit_text")));
					
					if (menu.mD1code.equals("G3")){
						prd.setTrDiv(PRD_CLASS.get("허니문"));
						prd.setDmstDiv("A");
					}else if (menu.mD1code.equals("G5")){
						prd.setTrDiv(PRD_CLASS.get("국내"));
						prd.setDmstDiv("D");
					}else if (menu.mD1code.equals("G7")){
						prd.setTrDiv(PRD_CLASS.get("골프"));
						prd.setDmstDiv("A");
					}else {
						prd.setTrDiv(PRD_CLASS.get("패키지"));
						prd.setDmstDiv("A");
					}
					
					ArrayList<String> areaCodeList = findGetAreaString(prd.getPrdNm());
					if (areaCodeList.size() <= 0) areaCodeList = findGetAreaString(menu.mMenu);
					ArrayList<TtrTrArea> areaList = new ArrayList<TtrTrArea>();
					for (String areaCode :  areaCodeList){
						TtrTrArea area = new TtrTrArea();
						String[] areaCodeSplit = areaCode.split("/");
						area.setTrCityCd(areaCodeSplit[0]);
						area.setTrNtCd(areaCodeSplit[1]);
						area.setTrCntt(areaCodeSplit[2]);
						areaList.add(area);
					}
					prd.setAreaList(areaList);
					
					log("\t\tProcessing", String.valueOf(prdCnt) + "/" + prdUrls.size() + " - " + prd.getPrdUrl());
					
					HashSet<String> monthSet = new HashSet<String>();
					if (options != null){
						if (options.get("until") != null){
							monthSet = Util.getYearMonthSet(options.get("until"));
						}
						if (options.get("month") != null){
							monthSet.add(options.get("month"));
						}
					}else{
						monthSet.add(getSystemMonth());
					}
					
					ArrayList<PrdDtl> prdDtlList = new ArrayList<PrdDtl>();
					ArrayList<PrdDtl> prdDtlUrlList = new ArrayList<PrdDtl>();
					
					for (String month : monthSet){
						String prtType = menuUrl.split("/")[menuUrl.split("/").length - 1].split("_")[0];

						String prdDtlListUrl = "http://www.krt.co.kr/_inc2014/_StartList.asp?"
										+ "param1=" + prdNo
										+ "&param2=" + month
										+ "&param3=" + getSystemMonth()
										+ "&param4=" + prtType
										+ "&param5=0"
										+ "&param6=" + getSystemMonth();
						Website prdDtlListSite = new Website();
						prdDtlListSite.setUrl(prdDtlListUrl);
						prdDtlListSite.setMethod("GET");
						prdDtlListSite.setEncoding(website.getEncoding());
						String prdDtlListHtml = this.getHtml(httpclient, prdDtlListSite);
						
						prdDtlListHtml = prdDtlListHtml.replace(this.getTag(prdDtlListHtml, "TABLE"), "");
						prdDtlListHtml = prdDtlListHtml.replace(this.getTag(prdDtlListHtml, "TABLE"), "");
						prdDtlListHtml = this.getTag(prdDtlListHtml, "TABLE").replaceFirst("(<(?i)table)++[>| ]", "");
						
						String prdDtlHtml = "";
						while ((prdDtlHtml = this.getTag(prdDtlListHtml, "TABLE")).length() > 0){
							prdDtlListHtml = prdDtlListHtml.replace(prdDtlHtml, "");
							
							prdDtlHtml = this.convertSpecialChar(prdDtlHtml);
							
							String tempString = this.getTag(prdDtlHtml, "TD");
							prdDtlHtml = prdDtlHtml.replace(tempString, "");
							tempString = tempString.replace("/", "");
							tempString = this.removeAllTags(tempString);
							String[] dt = tempString.split("[\\(|\\)]");
							
							tempString = this.getTag(prdDtlHtml, "TD");
							prdDtlHtml = prdDtlHtml.replace(tempString, "");
							tempString = this.getTag(prdDtlHtml, "TD");
							prdDtlHtml = prdDtlHtml.replace(tempString, "");
							String[] strUrl = tempString.split("((?i)href=)[\"|\']");
							strUrl = strUrl[1].split("[\"|\']");
							String prdDtlNm = this.removeAllTags(tempString);
							
							PrdDtl prdDtl = new PrdDtl();
							prdDtl.setPrdUrl("http://www.krt.co.kr/" + strUrl[0].replaceFirst("\\.\\./", ""));
							prdDtlUrlList.add(prdDtl);
							
						}
					}
					
					log("\t\t\t==>", String.valueOf(prdDtlUrlList.size()) + " Product Details ...");
					for (PrdDtl prdDtlUrl : prdDtlUrlList){
						Website prdDtlSite = new Website();
						prdDtlSite.setUrl(prdDtlUrl.getPrdUrl());
						prdDtlSite.setMethod("GET");
						prdDtlSite.setEncoding(website.getEncoding());
						
						PrdDtl prdDtl = this.getProductDetail(httpclient, prdDtlSite);
						prdDtlList.add(prdDtl);
//						break;
					}
					log("\t\t\t==>", String.valueOf(prdDtlUrlList.size()) + " Product Details Complete");
					
					prd.setPrdDtlLst(prdDtlList);
					
					prdList.add(prd);
//					break;

				}
				
//				System.out.println(prdList.size());
//				for (Prd prd : prdList){
//					System.out.println(prd.getPrdNo() + " : " + prd.getPrdNm());
//					
//					for (TtrTrArea area : prd.getAreaList()){
//						System.out.println(area.toJson());
//					}
//					
//					for (PrdDtl prdDtl: prd.getPrdDtlLst()){
//						System.out.println(prdDtl.toJson());
//					}
//				}
//				break;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return prdList;
	}
	
	
	public PrdDtl getProductDetail(CloseableHttpClient httpclient, Website prdDtlSite){
		PrdDtl prdDtl = new PrdDtl();
		String prdDtlHtml = this.removeComment(this.getHtml(httpclient, prdDtlSite));
		Html html = new Html(prdDtlHtml);
		
		String prdNo = this.findRegex(prdDtlHtml, "goStartListNew\\([\'|\"]+[^,]*"); 
		prdNo = this.findRegex(prdNo, "[\'|\"][\\s\\S]*[\'|\"]");
		String pkgMaininfo = this.getValueByClass(prdDtlHtml, "pkg_maininfo2");
		String prdSeq = this.removeAllTags(this.getTag(pkgMaininfo, "TR").replace(this.getTag(pkgMaininfo, "td"), ""));
		String depDt = this.removeAllTags(this.removeTag(this.getTag(this.removeTag(this.removeTag(pkgMaininfo, "TR"), "TR"), "TR"), "TD"));
		String depDtWd = depDt.replaceAll("[\\s\\S]+\\(", "");
		depDtWd = depDtWd.replaceAll("\\)[\\s\\S]+", "");
		depDtWd = WEEK_DAY_NUMBER.get(depDtWd);
		depDt = depDt.replaceAll("[년|월|일|월|화|수|목|금|토| |\\(|\\)|:]", "").trim();
		String arrDt = this.removeAllTags(this.removeTag(this.getTag(this.removeTag(this.removeTag(this.removeTag(pkgMaininfo, "TR"), "TR"), "TR"), "TR"), "TD"));
		String arrDtWd = arrDt.replaceAll("[\\s\\S]+\\(", "");
		arrDtWd = arrDtWd.replaceAll("\\)[\\s\\S]+", "");
		arrDtWd = WEEK_DAY_NUMBER.get(arrDtWd);
		arrDt = arrDt.replaceAll("[년|월|일|월|화|수|목|금|토| |\\(|\\)|:]", "").trim();
		String arlnId = this.getTag(this.removeTag(this.removeTag(this.removeTag(this.removeTag(this.removeTag(pkgMaininfo, "TR"), "TR"), "TR"), "TR"), "TD"), "span").split("(\\.(?i)gif)")[0];
		arlnId = arlnId.substring(arlnId.length() - 2, arlnId.length());
		
		String depArpt = html.getTag("body").removeComment().toString().split("출발당일미팅장소")[1];
		html = new Html(depArpt);
		depArpt = html.removeTag("tr").removeTag("tr").removeTag("tr").getTag("tr").getTag("span").removeAllTags().toString().substring(0, 2);
		html = new Html(this.getValueByClass(prdDtlHtml, "pkg_maininfo2").split("상품가격")[1]);
		html = html.removeTag("tr").getTag("table").getTag("tr").removeTag("table");
		Html subHtml = new Html(html.convertSpecialChar().toString());
		String feeAd = subHtml.getTag("table").getTag("td").removeAllTags().toStringWithConvert().replaceAll(",", "");		
		String feeCh = subHtml.removeTag("td").getTag("table").getTag("td").removeAllTags().toStringWithConvert().replaceAll(",", "");
		String feeBb = subHtml.removeTag("td").removeTag("td").getTag("table").getTag("td").removeAllTags().toStringWithConvert().replaceAll(",", "");
		
		prdDtl.setTagnId("KRT");
		prdDtl.setPrdNo(prdNo.replaceAll("[\'|\"]", "").trim());
		prdDtl.setPrdDtlNm(this.removeAllTags(this.getValueByClass(prdDtlHtml, "tit_text")).trim());
		prdDtl.setPrdSeq(prdSeq.trim());
		prdDtl.setDepDt(depDt);
		prdDtl.setArrDt(arrDt);
		prdDtl.setDepDtYmd(depDt.substring(0, 8));
		prdDtl.setDepDtHm(depDt.substring(8, 12));
		prdDtl.setDepDtWd(depDtWd);
		prdDtl.setArrDtYmd(arrDt.substring(0, 8));
		prdDtl.setArrDtHm(arrDt.substring(8, 12));
		prdDtl.setArrDtWd(arrDtWd);
		prdDtl.setArlnId(arlnId);
		prdDtl.setPrdUrl(prdDtlSite.getUrl());
		prdDtl.setDepArpt(ARPT_NAME_CODE.get(depArpt));
		prdDtl.setPrdFeeAd(feeAd);
		prdDtl.setPrdFeeCh(feeCh);
		prdDtl.setPrdFeeBb(feeBb);
		
		if (prdDtl.getPrdSt() == null || prdDtl.getPrdSt().trim() == ""){
			String prtType = prdDtl.getPrdUrl().split("/")[prdDtl.getPrdUrl().split("/").length - 1].split("_")[0];
			String prdDtlListUrl = "http://www.krt.co.kr/_inc2014/_StartList.asp?"
					+ "param1=" + prdDtl.getPrdNo()
					+ "&param2=" + prdDtl.getDepDtYmd().substring(0, 6)
					+ "&param3=" + getSystemMonth()
					+ "&param4=" + prtType
					+ "&param5=0"
					+ "&param6=" + getSystemMonth();
	
			Website prdDtlListSite = new Website();
			prdDtlListSite.setUrl(prdDtlListUrl);
			prdDtlListSite.setMethod("GET");
			prdDtlListSite.setEncoding(prdDtlSite.getEncoding());
			String prdDtlListHtml = this.getHtml(httpclient, prdDtlListSite);
			
			html = new Html(prdDtlListHtml.split(prdDtl.getPrdUrl().split("\\?")[1])[1]);
			html = html.removeTag("td").getTag("td").removeAllTags();
			
			String prdSt = html.toStringWithConvert().trim(); 
			
			if (prdSt.equals("예약마감")){
				prdSt = "예약마감";
			}else if (prdSt.equals("예약대기")){
				prdSt = "대기예약";
			}else if (prdSt.equals("예약가능")){
				prdSt = "예약가능";
			}else if (prdSt.equals("출발예정")){
				prdSt = "예약가능";
			}else if (prdSt.equals("출발확정")){
				prdSt = "출발확정";
			}else if (prdSt.equals("마감임박")){
				prdSt = "예약가능";
			}else{
				prdSt = "예약가능";
			}
			
			prdDtl.setPrdSt(PRD_STATUS.get(prdSt));
		}
		


//		ARR_ARPT	VARCHAR2(50 BYTE)	Yes		9	
//		CMPS_SEAT	NUMBER	Yes		17	여유좌석
//		SEL_DT	DATE	Yes		18	
//		TR_TERM_BAK	NUMBER	Yes		19	
	
		return prdDtl;
	}
	
	
	/**
	 * 사이트의 menu별(동남아, 허니문..) url을 가져온다.
	 * 메뉴 구조 중 가장 하위 단계에 있는 url 만 가져오도록 작성함.
	 * @param html html 코드
	 * @return url 이 담겨있는 ArrayList
	 * @throws IOException
	 */
	private ArrayList<Menu> getMenuUrls(String html) throws IOException{
		ArrayList<String> urls = new ArrayList<String>();
		ArrayList<Menu> menuList = new ArrayList<KRTHandler.Menu>();
		
		String[] htmlLine = html.split("\n");
		
		for	(String line: htmlLine){
			if (line.length() < 14 || !line.substring(0, 14).equals("menuArray.push")) continue;
			Gson gson = new GsonBuilder().create();
			Menu m = gson.fromJson(line.trim().replace("menuArray.push(", "").replace(");", ""), Menu.class);
			menuList.add(m);
		}
		
		int i = 0;
		while(true){
			if (i > menuList.size() - 1) break;
			
			Menu menu = menuList.get(i);
			
			if (!menu.mD1code.substring(0, 1).equals("G")){
				menuList.remove(i);
				continue;
			}

			//자유여행 제외
			if (menu.mMenu.contains("자유여행")){
				menuList.remove(i);
				continue;
			}
			
			//국내 여행은 제주여행만 처리
			if (menu.mD1code.equals("G5")){
				String menuCode = menu.mD1code + menu.mD2code + menu.mD3code + menu.mD4code;
				if (!(menuCode.equals("G511") || menuCode.equals("G512") || menuCode.equals("G513"))){
					menuList.remove(i);
					continue;
				}
			}
			
			//항공권, 호텔 제외
			if (menu.mD1code.equals("G8") || menu.mD1code.equals("G9")){
				menuList.remove(i);
				continue;
			}
			
			
			if (!menu.mD4code.equals("")){
				i++;
				continue;
			}
			
			boolean increaseI = true;
			for(int j = i + 1; j <= menuList.size() - 1 ; j++){
				Menu compareMenu = menuList.get(j);
				if (menu.mD1code.equals(compareMenu.mD1code) 
						&& menu.mD2code.equals(compareMenu.mD2code) 
						&& menu.mD3code.equals(compareMenu.mD3code) 
						&& menu.mD4code.equals("") && !compareMenu.mD3code.equals("")){
					
					menuList.remove(i);
					increaseI = false;
					break;
				}
				
				if (menu.mD1code.equals(compareMenu.mD1code) 
						&& menu.mD2code.equals(compareMenu.mD2code) 
						&& menu.mD3code.equals("") && !compareMenu.mD3code.equals("")){
					
					menuList.remove(i);
					increaseI = false;
					break;
				}
				
				if (menu.mD1code.equals(compareMenu.mD1code) 
						&& menu.mD2code.equals("") && !compareMenu.mD2code.equals("")){
					
					menuList.remove(i);
					increaseI = false;
					break;
				}
			}
			
			if (increaseI){
				i++;
			}
		}
		
		for(Menu menu : menuList){
			urls.add(menu.mLink);
		}
//		return urls;
		return menuList;
	}
	
	
	
	/**
	 * 화면에 있는 여행 상품의 URL을 가져온다.
	 * @param html html 코드
	 * @return url 이 담겨있는 HashSet
	 * @throws IOException
	 */
	private HashSet<String> getPrdUrls(String html){
		HashSet<String> urls = new HashSet<String>();
		String[] htmlLine = html.split("\n");
		
		for	(String line: htmlLine){
			Pattern tag = Pattern.compile("<a href=\"[^\'\">]*good_cd=[^\'\">]*\"");  
			Matcher mat = tag.matcher(line);  
			if (mat.find()){
				String substr = line.substring(mat.start(), mat.end());
				tag = Pattern.compile("(\"[^\"]*\"|\'[^\']*\')");
				mat = tag.matcher(substr);
				
				if (mat.find()) urls.add(substr.substring(mat.start() + 1, mat.end() - 1)); 
			}
		}
		return urls;
	}
	
	
	
	private class Menu{
		private String mGubun;
		private String mGtype;
		private String mDepth;
		private String mD1code;
		private String mD2code;
		private String mD3code;
		private String mD4code;
		private String mMenu;
		private String mSubmenu;
		private String mLink;
		
		@Override
		public String toString() {
			return this.mMenu + " (" +  this.mD1code + "-" + this.mD2code + "-" + this.mD3code + "-" + this.mD4code + ") : " + this.mLink;
		}
		
	}
}
