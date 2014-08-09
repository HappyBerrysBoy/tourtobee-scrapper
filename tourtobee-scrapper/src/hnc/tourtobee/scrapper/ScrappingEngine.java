package hnc.tourtobee.scrapper;


import static hnc.tourtobee.code.Codes.initCodes;
import static hnc.tourtobee.util.Util.log;
import hnc.tourtobee.scrapper.dataobject.Prd;
import hnc.tourtobee.scrapper.dataobject.PrdDtl;
import hnc.tourtobee.scrapper.dataobject.TtrTrArea;
import hnc.tourtobee.scrapper.handler.website._TouristAgencyHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import jh.project.httpscrapper.Website;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import oracle.jdbc.pool.OracleDataSource;


public class ScrappingEngine {
	private Connection conn;
	
	public Connection getConn() {
		return conn;
	}
	public void setConn(Connection conn) {
		this.conn = conn;
	}
	public void closeConn(){
		try{
			this.conn.close();
		}catch(Exception e){
			log(this.getClass().getName(),  e.toString());
		}
		this.conn = null;
	}
	public void initConn() throws SQLException{
		this.conn = null;
		OracleDataSource ods = new OracleDataSource();
		ods.setURL("jdbc:oracle:thin:@hnctech73.iptime.org:1521:ora11g");
		ods.setUser("bigtour");
		ods.setPassword("bigtour");
		this.conn = ods.getConnection();
	}
	
	public ScrappingEngine() throws SQLException{
		initConn();
		initCodes(this.conn);
	}
	
	/**
	 * 이미 입력된 Prd를 조회 한다.(T_PRD)
	 * @param conn Connection
	 * @param tagnId TAGN_ID
	 * @return 이미 입력된 Prd 목록
	 */
	public ArrayList<Prd> getInsPrds(Connection conn, String tagnId){
		ArrayList<Prd> insPrds = new ArrayList<Prd>();
		try{
			String query = "SELECT TAGN_ID, PRD_NO, PRD_NM, TR_DIV, DMST_DIV, PRD_URL"
						+ " FROM T_PRD"
						+ " WHERE TAGN_ID = ?"
//						+ "   AND PRD_NO NOT IN (SELECT distinct PRD_NO FROM T_PRD_DTL WHERE TAGN_ID = ?)"
						;
			PreparedStatement pstmt = conn.prepareStatement(query);
			pstmt.setString(1, tagnId);
//			pstmt.setString(2, tagnId);
			ResultSet rs = pstmt.executeQuery();
			
			while(rs.next()){
				Prd prd = new Prd();
				prd.setTagnId(rs.getString("TAGN_ID"));
				prd.setPrdNo(rs.getString("PRD_NO"));
				prd.setPrdNm(rs.getString("PRD_NM"));
				prd.setTrDiv(rs.getString("TR_DIV"));
				prd.setDmstDiv(rs.getString("DMST_DIV"));
				prd.setPrdUrl(rs.getString("PRD_URL"));
				insPrds.add(prd);
			}

			pstmt.close();
			rs.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return insPrds;
	}
	
	
	/**
	 * T_PRD 자료를 merge (insert or update) 한다.
	 * @param conn Connection
	 * @param prd PRD
	 */
	public void mergePrd(Connection conn, Prd prd) {
		
		try{
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
						+ "when matched then update set "
									+ "a.prd_nm= b.prd_nm"
									+ ", a.tr_div= b.tr_div"
									+ ", a.dmst_div= b.dmst_div"
									+ ", a.prd_desc= b.prd_desc"
									+ ", a.prd_desc_md= b.prd_desc_md"
									+ ", a.sel_dt = sysdate"
									+ ", a.prd_url = b.prd_url "
						+ "when not matched then insert (tagn_id, prd_no, prd_nm, tr_div, dmst_div, prd_desc, prd_desc_md, sel_dt, prd_url) "
						+ "values ( b.tagn_id, b.prd_no, b.prd_nm, b.tr_div, b.dmst_div, b.prd_desc, b.prd_desc_md, sysdate, b.prd_url)";
			PreparedStatement pstmt = conn.prepareStatement(query);
			pstmt.setString(1, prd.getTagnId());
			pstmt.setString(2, prd.getPrdNo());
			pstmt.setString(3, prd.getPrdNm());
			pstmt.setString(4, prd.getTrDiv());
			pstmt.setString(5, prd.getDmstDiv());
			pstmt.setString(6, prd.getPrdDesc());
			pstmt.setString(7, prd.getPrdDescMd());
			pstmt.setString(8, prd.getPrdUrl());
			pstmt.executeUpdate();
			pstmt.clearParameters();
			pstmt.close();
			pstmt = null;
			
			
			int index = 0;
			ArrayList<TtrTrArea> areaList = prd.getAreaList();
			if (areaList.size() > 0){
				query = "delete from ttr_tr_area where tagn_id =? and prd_no = ?";
				PreparedStatement deletePstmt = conn.prepareStatement(query);
				deletePstmt.setString(1, prd.getTagnId());
				deletePstmt.setString(2, prd.getPrdNo());
				deletePstmt.executeUpdate();
				deletePstmt.clearParameters();
				deletePstmt.close();
				deletePstmt = null;
			}
			
			
			for (TtrTrArea area : areaList){
				query = "insert into ttr_tr_area (tagn_id, prd_no, tr_area_seq, tr_cntt, tr_nt_cd, tr_city_cd, tr_site_cd) "
						+ "values (?, ?, ?, ?, ?, ?, ?)";
				
				PreparedStatement areaPstmt = conn.prepareStatement(query);
				areaPstmt.setString(1, prd.getTagnId());
				areaPstmt.setString(2, prd.getPrdNo());
				areaPstmt.setString(3, String.valueOf(index));
				areaPstmt.setString(4, area.getTrCntt());
				areaPstmt.setString(5, area.getTrNtCd());
				areaPstmt.setString(6, area.getTrCityCd());
				areaPstmt.setString(7, area.getSiteCd());
				areaPstmt.executeUpdate();
				areaPstmt.clearParameters();
				areaPstmt.close();
				areaPstmt = null;
				
				index++;
			}

			ArrayList<PrdDtl> prdDtlList = prd.getPrdDtlLst();
			for(PrdDtl prdDtl : prdDtlList){
				mergePrdDtl(conn, prdDtl);
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	/**
	 * T_PRD_DTL 자료를 merge (insert or update) 한다.
	 * @param conn Connection
	 * @param prdDtl PrdDtl
	 */
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
										+ (prdDtl.getPrdDtlNm().trim().length() > 0 ? "a.PRD_DTL_NM =b.PRD_DTL_NM" : "")
										+ (prdDtl.getDepDt().trim().length() > 0 ? ", a.DEP_DT = to_date(b.DEP_DT, 'yyyymmddhh24mi')" : "")
										+ (prdDtl.getArrDt().trim().length() > 0 ? ", a.ARR_DT = to_date(b.ARR_DT, 'yyyymmddhh24mi')" : "")
										+ (prdDtl.getDepDt().trim().length() > 0 && prdDtl.getArrDt().trim().length() > 0 ? ", a.TR_TERM = TRUNC(to_date(b.ARR_DT, 'yyyymmddhh24mi')) - TRUNC(to_date(b.DEP_DT, 'yyyymmddhh24mi')) + 1" : "")
										+ (prdDtl.getDepArpt().trim().length() > 0 ? ", a.DEP_ARPT =b.DEP_ARPT" : "")
										+ (prdDtl.getArrArpt().trim().length() > 0 ? ", a.ARR_ARPT =b.ARR_ARPT" : "")
										+ (prdDtl.getArlnId().trim().length() > 0 ? ", a.ARLN_ID =b.ARLN_ID" : "")
										+ (prdDtl.getPrdSt().trim().length() > 0 ? ", a.PRD_ST =b.PRD_ST" : "")
										+ (prdDtl.getPrdUrl().trim().length() > 0 ? ", a.PRD_URL =b.PRD_URL" : "")
										+ (Integer.parseInt(prdDtl.getPrdFeeAd()) > 0 ? ", a.PRD_FEE_AD =b.PRD_FEE_AD" : "")
										+ (Integer.parseInt(prdDtl.getPrdFeeCh()) > 0 ? ", a.PRD_FEE_CH =b.PRD_FEE_CH" : "")
										+ (Integer.parseInt(prdDtl.getPrdFeeBb()) > 0 ? ", a.PRD_FEE_BB =b.PRD_FEE_BB" : "")
//										+ (prdDtl.getCmpsSeat().trim().length() > 0 ? ", a.CMPS_SEAT =b.CMPS_SEAT" : "")
										+ (prdDtl.getExgDiv().trim().length() > 0 ? ", a.EXG_DIV =b.EXG_DIV" : "")
										+ ", a.SEL_DT =sysdate"
//										+ (prdDtl.getTrTermBak().trim().length() > 0 ? ", a.TR_TERM_BAK = b.TR_TERM_BAK" : "")
										+ (prdDtl.getDepDtYmd().trim().length() > 0 ? ", a.DEP_DT_YMD = b.DEP_DT_YMD" : "")
										+ (prdDtl.getDepDtHm().trim().length() > 0 ? ", a.DEP_DT_HM = b.DEP_DT_HM" : "")
										+ (prdDtl.getDepDtWd().trim().length() > 0 ? ", a.DEP_DT_WD = b.DEP_DT_WD" : "")
										+ (prdDtl.getArrDtYmd().trim().length() > 0 ? ", a.ARR_DT_YMD = b.ARR_DT_YMD" : "")
										+ (prdDtl.getArrDtHm().trim().length() > 0 ? ", a.ARR_DT_HM = b.ARR_DT_HM" : "")
										+ (prdDtl.getArrDtWd().trim().length() > 0 ? ", a.ARR_DT_WD = b.ARR_DT_WD" : "")
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
			pstmt.clearParameters();
			pstmt.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Prd, Prd와 연결된 PrdDtl의 값을 Scrap 하여 merge 한다.
	 * @param website Scrap할 website
	 * @param options until 또는 month로 입력된 option 값
	 */
	public void scrapPrd(Website website, HashMap<String, String> options){
		_TouristAgencyHandler handler = (_TouristAgencyHandler)website.getHandler();
		CloseableHttpClient httpclient = HttpClients.createDefault();
		
		try {
			if (!this.conn.isValid(10)) initConn();
			
			log(website.getId() + " scrapPrd Start ", "!!!!!!!!!!!!!!!!!!!!!!!!");
			
			ArrayList<Prd> prdList = handler.scrapPrdList(httpclient, website, options, null);
			log(website.getId() + " Scrap Prd ", prdList.size() + " Prds");
			
			int prdCnt = 0;
			if (prdList != null && prdList.size() > 0){
				for (Prd prd : prdList){
					if (prd == null) continue;
					mergePrd(this.conn, prd);
					prdCnt++;
					log(website.getId() + " Merge Prd ", prd.getPrdNo() + " (" + prdCnt + "/" + prdList.size() + ")");
				}
			}
			
//			if (prdList != null && prdList.size() > 0){
//				for (Prd prd : prdList){
//					log(website.getId() + "   Prd(" + prd.getPrdNo() + ")", "Start DTL scrap");
//					ArrayList<PrdDtl> prdDtlList = handler.scrapPrdDtlSmmry(httpclient, website, options, prd);
//					log(website.getId() + "   Prd(" + prd.getPrdNo() + ")", String.valueOf(prdDtlList.size()) + " Dtls");
//					for (PrdDtl prdDtl : prdDtlList){
//						mergePrdDtl(this.conn, prdDtl);
//					}
//				}
//			}
			
			log(website.getId() + " scrapPrd Finish ", "!!!!!!!!!!!!!!!!!!!!!!!!");
		} catch (SQLException e) {
			log(this.getClass().getName() + "-scrapPrd", e.toString());
		}
		
	}
	
	
	/**
	 * 이미 입력된 Prd를 조회해 이들의 PrdDtl을 merge 한다.
	 * @param website Scrap할 website
	 * @param options until 또는 month로 입력된 option 값
	 */
	public void scrapDtlSummary(Website website, HashMap<String, String> options){
		_TouristAgencyHandler handler = (_TouristAgencyHandler)website.getHandler();
		CloseableHttpClient httpclient = HttpClients.createDefault();
		
		try {
			if (!this.conn.isValid(10)) initConn();
			
			log(website.getId() + " scrapDtlSummary Start ", "!!!!!!!!!!!!!!!!!!!!!!!!");
			ArrayList<Prd> insPrds = getInsPrds(this.conn, website.getId());
			
			if (insPrds != null && insPrds.size() > 0){
				for (Prd prd : insPrds){
					log(website.getId() + "   Prd(" + prd.getPrdNo() + ")", "Start DTL scrap");
					ArrayList<PrdDtl> prdDtlList = handler.scrapPrdDtlSmmry(httpclient, website, options, prd);
					log(website.getId() + "   Prd(" + prd.getPrdNo() + ")", String.valueOf(prdDtlList.size()) + " Dtls");
					for (PrdDtl prdDtl : prdDtlList){
						mergePrdDtl(this.conn, prdDtl);
					}
				}
			}
			log(website.getId() + " scrapDtlSummary Finish ", "!!!!!!!!!!!!!!!!!!!!!!!!");
		} catch (SQLException e) {
			log(this.getClass().getName() + "-scrapDtlSummary", e.toString());
		}
	}
	
	
	
	
}
