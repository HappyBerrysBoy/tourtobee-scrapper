package hnc.tourtobee.scrapper.handler.website;

import hnc.tourtobee.scrapper.dataobject.Prd;
import hnc.tourtobee.scrapper.dataobject.PrdDtl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.http.impl.client.CloseableHttpClient;

import jh.project.httpscrapper.Website;
import jh.project.httpscrapper.handler.website._WebsiteHandler;

public class _TouristAgencyHandler extends _WebsiteHandler {

	public _TouristAgencyHandler() {
		super();
	}
	
	
	
	/**
	 * 상품 목록을 스크랩한다.
	 * @param httpclient
	 * @param website
	 * @param options until(yyyymm), month(yyyymm) 를 키로 갖는 옵션을 가질 수 있다. (until:~까지 스크랩, month:특정월 스크랩)
	 * @param insPrds 이미 입력된 상품 - 스크래핑에서 제외 된다.
	 * @return
	 */
	public ArrayList<Prd> scrapPrd(CloseableHttpClient httpclient, Website website, HashMap<String, String> options, HashSet<String> insPrds){
		return null;
	}
	
	
	public ArrayList<PrdDtl> scrapPrdDtl(CloseableHttpClient httpclient, ArrayList<Website> websiteList, HashMap<String, String> options, HashSet<String> insPrdDtls){
		return null;
	}
	
	
	public ArrayList<PrdDtl> scrapPrdDtlSmmry(CloseableHttpClient httpclient, Website website, HashMap<String, String> options, String prdUrl, String prdNo){
		return null;
	}
	
	
}