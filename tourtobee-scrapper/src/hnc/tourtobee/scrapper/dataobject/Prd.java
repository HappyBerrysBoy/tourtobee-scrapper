package hnc.tourtobee.scrapper.dataobject;

import java.util.ArrayList;

import jh.project.httpscrapper.dataobject._DataObject;


/**
 * T_PRD에 입력할 자료
 * _DataObject 를 상속함
 * 
 * @author purepleya
 * @since 2014-07-23
 */
public class Prd extends _DataObject{
	ArrayList<PrdDtl> prdDtlLst;
	ArrayList<TtrTrArea> areaList;
	
	private String tagnId;
	private String prdNo;
	private String cnttDiv;
	private String ntCd;
	private String arrCity;
	private String prdNm;
	private String trDiv;
	private String dmstDiv;
	private String prdDesc;
	private String prdDescMd;
	private String prdUrl;
	
	
	public Prd() {
		prdDtlLst = new ArrayList<PrdDtl>();
		areaList = new ArrayList<TtrTrArea>();
	}
	


	public ArrayList<PrdDtl> getPrdDtlLst() {
		return prdDtlLst;
	}
	public void setPrdDtlLst(ArrayList<PrdDtl> prdDtlLst) {
		this.prdDtlLst = prdDtlLst;
	}
	public void addPrdDtlLst(PrdDtl prdDtl){
		this.prdDtlLst.add(prdDtl);
	}
	public ArrayList<TtrTrArea> getAreaList() {
		return areaList;
	}
	public void setAreaList(ArrayList<TtrTrArea> areaList) {
		this.areaList = areaList;
	}
	public void addAreaList(TtrTrArea area){
		this.areaList.add(area);
	}

	public String getTagnId() {
		return tagnId;
	}
	public void setTagnId(String tagnId) {
		this.tagnId = tagnId;
	}
	public String getPrdNo() {
		return prdNo;
	}
	public void setPrdNo(String prdNo) {
		this.prdNo = prdNo;
	}
	public String getCnttDiv() {
		return cnttDiv;
	}
	public void setCnttDiv(String cnttDiv) {
		this.cnttDiv = cnttDiv;
	}
	public String getNtCd() {
		return ntCd;
	}
	public void setNtCd(String ntCd) {
		this.ntCd = ntCd;
	}
	public String getArrCity() {
		return arrCity;
	}
	public void setArrCity(String arrCity) {
		this.arrCity = arrCity;
	}
	public String getPrdNm() {
		return prdNm;
	}
	public void setPrdNm(String prdNm) {
		this.prdNm = prdNm;
	}
	public String getTrDiv() {
		return trDiv;
	}
	public void setTrDiv(String trDiv) {
		this.trDiv = trDiv;
	}
	public String getDmstDiv() {
		return dmstDiv;
	}
	public void setDmstDiv(String dmstDiv) {
		this.dmstDiv = dmstDiv;
	}
	public String getPrdDesc() {
		return prdDesc;
	}
	public void setPrdDesc(String prdDesc) {
		this.prdDesc = prdDesc;
	}
	public String getPrdDescMd() {
		return prdDescMd;
	}
	public void setPrdDescMd(String prdDescMd) {
		this.prdDescMd = prdDescMd;
	}
	public String getPrdUrl() {
		return prdUrl;
	}
	public void setPrdUrl(String prdUrl) {
		this.prdUrl = prdUrl;
	}
	
}
