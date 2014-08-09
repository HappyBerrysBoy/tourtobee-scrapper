package hnc.tourtobee;

import static hnc.tourtobee.util.Util.log;
import hnc.tourtobee.scrapper.ScrappingEngine;
import hnc.tourtobee.scrapper.dataobject.Prd;
import hnc.tourtobee.scrapper.dataobject.PrdDtl;
import hnc.tourtobee.scrapper.handler.website._TouristAgencyHandler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;

import jh.project.httpscrapper.ScrapItem;
import jh.project.httpscrapper.Scrapper;
import jh.project.httpscrapper.Website;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
/**
 * Scrapper를 직접 실행하고자 할때 사용하는 Class
 * @author purepleya
 *
 */
public class Excuter {
	public static void main(String[] args){
		
		log("!!!", "Process Start!!");
		
		int scrapMonth = 0;
		
		if (args != null && args.length >= 1){
			scrapMonth = Integer.parseInt(args[0]);
		}
		
		try {
			
			
			ScrappingEngine se = new ScrappingEngine();
			Scrapper sc = new Scrapper();
			ArrayList<ScrapItem> scItemList = sc.getScrapItem();
			
			for(ScrapItem scItem : scItemList){
				ArrayList<Website> websiteList = sc.getWebsite(scItem);
				
				for(Website website : websiteList){
					if (!website.getId().equals("hanatour")) continue;
					log(website.getId(), "Process Start!!");
					
					Calendar tempC = Calendar.getInstance();
					tempC.add(Calendar.MONTH, scrapMonth);
					String toMonth = String.format("%04d", tempC.get(Calendar.YEAR)) + String.format("%02d", tempC.get(Calendar.MONTH) + 1);
					HashMap<String, String> options = new HashMap<String, String>();
					options.put("until", toMonth);
					
//					se.scrapPrd(website, options);
					se.scrapDtlSummary(website, options);
					
//					_TouristAgencyHandler handler = (_TouristAgencyHandler)website.getHandler();
//					CloseableHttpClient httpclient = HttpClients.createDefault();
//					
//					ArrayList<Prd> insPrds = se.getInsPrds(se.getConn(), website.getId());
//					HashSet<String> insPrdNoSet = new HashSet<String>();
//					for (Prd prd : insPrds){
//						insPrdNoSet.add(prd.getPrdNo());
//					}
//					
//					
//					ArrayList<Prd> prdList = handler.scrapPrdList(httpclient, website, options, null);
//					
//					int prdCnt = 0;
//					if (prdList != null && prdList.size() > 0){
//						for (Prd prd : prdList){
//							if (prd == null) continue;
//							se.mergePrd(se.getConn(), prd);
//							log(website.getId() + " Insert Prd ", prd.getPrdNo() + String.valueOf(prdCnt + 1));
//							
//							prdCnt++;
//						}
//					}
					
					
					
//					if (prdList != null && prdList.size() > 0){
//						for (Prd prd : prdList){
//							log(website.getId() + "   Prd(" + prd.getPrdNo() + ")", "Start DTL scrap");
//							ArrayList<PrdDtl> prdDtlList = handler.scrapPrdDtlSmmry(httpclient, website, options, prd);
//							log(website.getId() + "   Prd(" + prd.getPrdNo() + ")", String.valueOf(prdDtlList.size()) + " Dtls");
//							for (PrdDtl prdDtl : prdDtlList){
//								se.mergePrdDtl(se.getConn(), prdDtl);
//							}
//						}
//					}


				}
			}
			
			se.closeConn();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		log("!!!", "Finish!!");
		
	}
}
