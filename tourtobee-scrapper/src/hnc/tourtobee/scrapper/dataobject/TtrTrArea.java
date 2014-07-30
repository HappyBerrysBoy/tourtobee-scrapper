package hnc.tourtobee.scrapper.dataobject;

import jh.project.httpscrapper.dataobject._DataObject;

/**
 * TTR_TR_AREA에 입력할 자료
 * _DataObject 를 상속함
 * 
 * @author purepleya
 * @since 2014-07-23
 */
public class TtrTrArea extends _DataObject{
	
	private String trCntt;
	private String trNtCd;
	private String trCityCd;
	private String siteCd;
	
	
	public String getTrCntt() {
		return trCntt;
	}
	public void setTrCntt(String trCntt) {
		this.trCntt = trCntt;
	}
	public String getTrNtCd() {
		return trNtCd;
	}
	public void setTrNtCd(String trNtCd) {
		this.trNtCd = trNtCd;
	}
	public String getTrCityCd() {
		return trCityCd;
	}
	public void setTrCityCd(String trCityCd) {
		this.trCityCd = trCityCd;
	}
	public String getSiteCd() {
		return siteCd;
	}
	public void setSiteCd(String siteCd) {
		this.siteCd = siteCd;
	}
	
}
