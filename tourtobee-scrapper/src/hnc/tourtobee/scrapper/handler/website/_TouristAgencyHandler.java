package hnc.tourtobee.scrapper.handler.website;

import static hnc.tourtobee.code.Codes.findGetAreaString;
import static hnc.tourtobee.util.Util.getSystemMonth;
import hnc.tourtobee.scrapper.dataobject.Menu;
import hnc.tourtobee.scrapper.dataobject.Prd;
import hnc.tourtobee.scrapper.dataobject.PrdDtl;
import hnc.tourtobee.scrapper.dataobject.TtrTrArea;
import hnc.tourtobee.util.Util;

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
	 * 상품 정보를 스크랩 한다.
	 * @param httpclient
	 * @param website
	 * @param menu
	 * @param prdUrl 상품 정보 URL
	 * @param options until, month 둘 중 하나의 옵션을 가진다.
	 * @param insPrds 이미 입력된 상품 NO (입력에서 제외 됨)
	 * @return 상품 정보 목록
	 */
	public ArrayList<Prd> scrapPrdList(CloseableHttpClient httpclient, Website website, HashMap<String, String> options, HashSet<String> insPrds){
		return null;
	}
	
	
	/**
	 * 상품 세부 목록을 스크랩한다.
	 * @param httpclient
	 * @param website
	 * @param options until(yyyymm), month(yyyymm) 를 키로 갖는 옵션을 가질 수 있다. (until:~까지 스크랩, month:특정월 스크랩)
	 * @param insPrdDtls 이미 입력된 상품 세부 목록 - 스크래핑에서 제외 된다.
	 * @return 상품 세부 목록
	 */
	public ArrayList<PrdDtl> scrapPrdDtl(CloseableHttpClient httpclient, ArrayList<Website> websiteList, HashMap<String, String> options, HashSet<String> insPrdDtls){
		return null;
	}
	
	
	/**
	 * 상품 세부 목록예약 정보를 스크랩한다.
	 * @param httpclient
	 * @param website
	 * @param options until(yyyymm), month(yyyymm) 를 키로 갖는 옵션을 가질 수 있다. (until:~까지 스크랩, month:특정월 스크랩)
	 * @param prdUrl 스크래핑 할 상품 URL
	 * @param prdNo 스크래핑 할 상품 번호
	 * @return 상품 세부 목록예약 정보
	 */
	public ArrayList<PrdDtl> scrapPrdDtlSmmry(CloseableHttpClient httpclient, Website website, HashMap<String, String> options, Prd prd){
		return null;
	}
	
	/**
	 * hint의 내용으로 여행 지역 을 찾아낸다
	 * @param hint1
	 * @param hint2
	 * @return 여행 지역 목록
	 */
	public ArrayList<TtrTrArea> getAreaList(String hint1, String hint2){
		ArrayList<String> areaCodeList = findGetAreaString(hint1);
		if (areaCodeList.size() <= 0) areaCodeList = findGetAreaString(hint2);
		ArrayList<TtrTrArea> areaList = new ArrayList<TtrTrArea>();
		for (String areaCode :  areaCodeList){
			TtrTrArea area = new TtrTrArea();
			String[] areaCodeSplit = areaCode.split("/");
			area.setSiteCd(areaCodeSplit[0]);
			area.setTrCityCd(areaCodeSplit[1]);
			area.setTrNtCd(areaCodeSplit[2]);
			area.setTrCntt(areaCodeSplit[3]);
			areaList.add(area);
		}
		
		return areaList;
	}
	
	
	
	/**
	 * 조회할 월 (yyyymm)Set 을 가져온다.
	 * @param options
	 * @return 조회할 월 (yyyymm)Set 
	 */
	public HashSet<String> getMonthSet(HashMap<String, String> options){
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
		return monthSet;
	}
	
	
}