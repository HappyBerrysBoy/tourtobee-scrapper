package hnc.tourtobee;

import static hnc.tourtobee.util.Util.log;
import static hnc.tourtobee.code.Codes.initCodes;
import hnc.tourtobee.scrapper.dataobject.Prd;
import hnc.tourtobee.scrapper.dataobject.PrdDtl;
import hnc.tourtobee.scrapper.dataobject.TtrTrArea;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import jh.project.httpscrapper.ScrapItem;
import jh.project.httpscrapper.ScrapResult;
import jh.project.httpscrapper.Scrapper;
import jh.project.httpscrapper.Website;
import jh.project.httpscrapper.dataobject._DataObject;
import oracle.jdbc.pool.OracleDataSource;



public class ScrappingEngine {
	
	public void insertPrd(Connection conn, ScrapResult scrapResult) {
		ArrayList<_DataObject> prdList = scrapResult.getResults();
		
		for(_DataObject dataObject : prdList){
			PreparedStatement pstmt;
			
			try{
			Prd prd = (Prd)dataObject;
			
				String query = "merge into t_prd a using (select "
										+ " ? tagn_id"
										+ ", ? prd_no"
										+ ", ? prd_nm"
										+ ", ? tr_div"
										+ ", ? dmst_div"
										+ ", ? prd_desc"
										+ ", ? prd_desc_md"
										+ ", ? prd_url"
										+ " from dual) b "
							+ "on (a.tagn_id = b.tagn_id and a.prd_no = b.prd_no) "
							+ "when matched then update set a.prd_nm= b.prd_nm, a.tr_div= b.tr_div, a.dmst_div= b.dmst_div, a.prd_desc= b.prd_desc, a.prd_desc_md= b.prd_desc_md, a.sel_dt = sysdate, a.prd_url = b.prd_url "
							+ "when not matched then insert (tagn_id, prd_no, prd_nm, tr_div, dmst_div, prd_desc, prd_desc_md, sel_dt, prd_url) "
							+ "values ( b.tagn_id, b.prd_no, b.prd_nm, b.tr_div, b.dmst_div, b.prd_desc, b.prd_desc_md, sysdate, b.prd_url)";
				pstmt = conn.prepareStatement(query);
				pstmt.setString(1, prd.getTagnId());
				pstmt.setString(2, prd.getPrdNo());
				pstmt.setString(3, prd.getPrdNm());
				pstmt.setString(4, prd.getTrDiv());
				pstmt.setString(5, prd.getDmstDiv());
				pstmt.setString(6, prd.getPrdDesc());
				pstmt.setString(7, prd.getPrdDescMd());
				pstmt.setString(8, prd.getPrdUrl());
				pstmt.executeUpdate();
				
				int index = 0;
				ArrayList<TtrTrArea> areaList = prd.getAreaList();
				query = "delete from ttr_tr_area where tagn_id =? and prd_no = ?";
				pstmt = conn.prepareStatement(query);
				pstmt.setString(1, prd.getTagnId());
				pstmt.setString(2, prd.getPrdNo());
				pstmt.executeUpdate();
				
				for (TtrTrArea area : areaList){
					query = "insert into ttr_tr_area (tagn_id, prd_no, tr_area_seq, tr_cntt, tr_nt_cd, tr_city_cd, tr_site_cd) "
							+ "values (?, ?, ?, ?, ?, ?, ?)";
					
					pstmt = conn.prepareStatement(query);
					pstmt.setString(1, prd.getTagnId());
					pstmt.setString(2, prd.getPrdNo());
					pstmt.setString(3, String.valueOf(index));
					pstmt.setString(4, area.getTrCntt());
					pstmt.setString(5, area.getTrNtCd());
					pstmt.setString(6, area.getTrCityCd());
					pstmt.setString(7, area.getSiteCd());
					pstmt.executeUpdate();
					
					index++;
				}

				ArrayList<PrdDtl> prdDtlList = prd.getPrdDtlLst();
				for(PrdDtl prdDtl : prdDtlList){
					mergePrdDtl(conn, prdDtl);
				}
				
			}catch(Exception e){
				System.out.println(e);
			}
			
			
		}
		
	}
	
	
	
	
	public void mergePrdDtl(Connection conn, PrdDtl prdDtl){
		
		String query = "merge into t_prd_dtl a using ("
						+ "select ? tagn_id"
						+ ", ? prd_no"
						+ ", ? prd_seq"
						+ ", ? prd_dtl_nm"
						+ ", ? dep_dt"
						+ ", ? arr_dt"
//						+ ",'5' tr_term"
						+ ", ? dep_arpt"
						+ ", ? arr_arpt"
						+ ", ? arln_id"
						+ ", ? prd_st"
						+ ", ? prd_url"
						+ ", ? prd_fee_ad"
						+ ", ? prd_fee_ch"
						+ ", ? prd_fee_bb"
						+ ", ? cmps_seat"
						+ ", ? exg_div"
						+ ", ? TR_TERM_BAK"
						+ ", ? DEP_DT_YMD"
						+ ", ? DEP_DT_HM"
						+ ", ? DEP_DT_WD"
						+ ", ? ARR_DT_YMD"
						+ ", ? ARR_DT_HM"
						+ ", ? ARR_DT_WD"
						+ " from dual) b"
					+ " on (a.tagn_id = b.tagn_id and a.prd_no = b.prd_no and a.prd_seq = b.prd_seq)"
					+ " when matched then update set "
										+ "a.PRD_DTL_NM =b.PRD_DTL_NM"
										+ ", a.DEP_DT = to_date(b.DEP_DT, 'yyyymmddhh24mi')"
										+ ", a.ARR_DT = to_date(b.ARR_DT, 'yyyymmddhh24mi')"
										+ ", a.TR_TERM = TRUNC(to_date(b.ARR_DT, 'yyyymmddhh24mi')) - TRUNC(to_date(b.DEP_DT, 'yyyymmddhh24mi')) + 1"
										+ ", a.DEP_ARPT =b.DEP_ARPT"
										+ ", a.ARR_ARPT =b.ARR_ARPT"
										+ ", a.ARLN_ID =b.ARLN_ID"
										+ ", a.PRD_ST =b.PRD_ST"
										+ ", a.PRD_URL =b.PRD_URL"
										+ ", a.PRD_FEE_AD =b.PRD_FEE_AD"
										+ ", a.PRD_FEE_CH =b.PRD_FEE_CH"
										+ ", a.PRD_FEE_BB =b.PRD_FEE_BB"
										+ ", a.CMPS_SEAT =b.CMPS_SEAT"
										+ ", a.EXG_DIV =b.EXG_DIV"
										+ ", a.SEL_DT =sysdate"
										+ ", a.TR_TERM_BAK = b.TR_TERM_BAK"
										+ ", a.DEP_DT_YMD = b.DEP_DT_YMD"
										+ ", a.DEP_DT_HM = b.DEP_DT_HM"
										+ ", a.DEP_DT_WD = b.DEP_DT_WD"
										+ ", a.ARR_DT_YMD = b.ARR_DT_YMD"
										+ ", a.ARR_DT_HM = b.ARR_DT_HM"
										+ ", a.ARR_DT_WD = b.ARR_DT_WD"
					+ " when not matched then insert (TAGN_ID, PRD_NO, PRD_SEQ, PRD_DTL_NM, DEP_DT, ARR_DT, TR_TERM, DEP_ARPT, ARR_ARPT, ARLN_ID, PRD_ST, PRD_URL, PRD_FEE_AD, PRD_FEE_CH, PRD_FEE_BB, CMPS_SEAT, EXG_DIV, SEL_DT, TR_TERM_BAK, DEP_DT_YMD, DEP_DT_HM, DEP_DT_WD, ARR_DT_YMD, ARR_DT_HM, ARR_DT_WD)"
					+ " values ( b.tagn_id , b.prd_no , b.PRD_SEQ , b.PRD_DTL_NM , to_date(b.DEP_DT, 'yyyymmddhh24mi') , to_date(b.ARR_DT, 'yyyymmddhh24mi') , TRUNC(to_date(b.ARR_DT, 'yyyymmddhh24mi')) - TRUNC(to_date(b.DEP_DT, 'yyyymmddhh24mi')) + 1 , b.DEP_ARPT , b.ARR_ARPT , b.ARLN_ID ,b.PRD_ST , b.PRD_URL , b.PRD_FEE_AD , b.PRD_FEE_CH , b.PRD_FEE_BB , b.CMPS_SEAT , b.EXG_DIV , sysdate, b.TR_TERM_BAK, b.DEP_DT_YMD, b.DEP_DT_HM, b.DEP_DT_WD, b.ARR_DT_YMD, b.ARR_DT_HM, b.ARR_DT_WD)";
		
		try{
			PreparedStatement pstmt = conn.prepareStatement(query);
			pstmt.setString(1, prdDtl.getTagnId());
			pstmt.setString(2, prdDtl.getPrdNo());
			pstmt.setString(3, prdDtl.getPrdSeq());
			pstmt.setString(4, prdDtl.getPrdDtlNm());
			pstmt.setString(5, prdDtl.getDepDt());
			pstmt.setString(6, prdDtl.getArrDt());
			pstmt.setString(7, prdDtl.getDepArpt());
			pstmt.setString(8, prdDtl.getArrArpt());
			pstmt.setString(9, prdDtl.getArlnId());
			pstmt.setString(10, prdDtl.getPrdSt());
			pstmt.setString(11, prdDtl.getPrdUrl());
			pstmt.setInt(12, Integer.parseInt(prdDtl.getPrdFeeAd()));
			pstmt.setInt(13, Integer.parseInt(prdDtl.getPrdFeeCh()));
			pstmt.setInt(14, Integer.parseInt(prdDtl.getPrdFeeBb()));
			pstmt.setInt(15, Integer.parseInt(prdDtl.getCmpsSeat()));
			pstmt.setString(16, prdDtl.getExgDiv());
			pstmt.setInt(17, Integer.parseInt(prdDtl.getTrTermBak()));
			pstmt.setString(18, prdDtl.getDepDtYmd());
			pstmt.setString(19, prdDtl.getDepDtHm());
			pstmt.setString(20, prdDtl.getDepDtWd());
			pstmt.setString(21, prdDtl.getArrDtYmd());
			pstmt.setString(22, prdDtl.getArrDtHm());
			pstmt.setString(23, prdDtl.getArrDtWd());
			
			pstmt.executeUpdate();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	
	
	
	
	
	
	
	
	public static void main(String[] args){
		
		log("!!!", "Process Start!!");
		
		int scrapMonth = 0;
		
		if (args != null && args.length >= 1){
			scrapMonth = Integer.parseInt(args[0]);
		}
		
		try {
			OracleDataSource ods = new OracleDataSource();
			ods.setURL("jdbc:oracle:thin:@hnctech73.iptime.org:1521:ora11g");
			ods.setUser("bigtour");
			ods.setPassword("bigtour");
			Connection conn = ods.getConnection();
			initCodes(conn);
			
			ScrappingEngine se = new ScrappingEngine();
			Scrapper sc = new Scrapper();
			ArrayList<ScrapItem> scItemList = sc.getScrapItem();
			
			for(ScrapItem scItem : scItemList){
				ArrayList<Website> websiteList = sc.getWebsite(scItem);
				
				for(Website website : websiteList){
					
					log(website.getId(), "Process Start!!");
					
					Calendar tempC = Calendar.getInstance();
					tempC.add(Calendar.MONTH, scrapMonth);
					String toMonth = String.format("%04d", tempC.get(Calendar.YEAR)) + String.format("%02d", tempC.get(Calendar.MONTH) + 1);
					HashMap<String, String> option = new HashMap<String, String>();
					option.put("until", toMonth);
//					option.put("month", "201408");
					ScrapResult sresult = sc.websiteScrap(website, option);
					
					log(website.getId(), "Scrap Finish");
					
					se.insertPrd(conn, sresult);

					log(website.getId(), "DB Insert Finish");
					
	//				ArrayList<_DataObject> resultList = sresult.getResults(); 
	//				
	//				for(_DataObject dataObject : resultList){
	//					
	//					Prd prd = (Prd)dataObject;
	//					
	//				}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		log("!!!", "Finish!!");
	}
	
}
