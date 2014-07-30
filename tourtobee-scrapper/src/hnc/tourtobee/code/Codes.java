package hnc.tourtobee.code;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class Codes {
	
	public static HashMap<String, String> CITY_NAME_CODE;
	public static HashMap<String, String> CITY_NAME_EN_CODE;
	public static HashMap<String, String> CITY_CODE_NATION_CODE;
	public static HashMap<String, String> NATION_NAME_NATION_CODE;
	public static HashMap<String, String> NATION_NAME_EN_NATION_CODE;
	public static HashMap<String, String> NATION_CODE_CONTINENT_CODE;
	public static HashMap<String, String> CONTINENT_NAME_CONTINENT_CODE;
	public static HashMap<String, String> CONTINENT_NAME_EN_CONTINENT_CODE;
	public static HashMap<String, String> ARPT_NAME_CODE;
	
	/**
	* 상품 구분
	* F	에어텔
	* D	국내
	* P	패키지
	* W	허니문
	* G	골프
	* C	크루즈
	*/
	public static HashMap<String, String> PRD_CLASS;
	
	/**
	 * 상품 상태
	 * RS	예약가능
	 * DF	출발확정
	 * WR	대기예약
	 * RF	예약마감
	 */
	public static HashMap<String, String> PRD_STATUS;
	
	public static final HashMap<String, String> WEEK_DAY_NUMBER;
	
	static
    {
		WEEK_DAY_NUMBER = new HashMap<String, String>();
		WEEK_DAY_NUMBER.put("일", "1");
		WEEK_DAY_NUMBER.put("월", "2");
		WEEK_DAY_NUMBER.put("화", "3");
		WEEK_DAY_NUMBER.put("수", "4");
		WEEK_DAY_NUMBER.put("목", "5");
		WEEK_DAY_NUMBER.put("금", "6");
		WEEK_DAY_NUMBER.put("토", "7");
    }
		
	/**
	* 초기 세팅이 필요한 변수들을 세팅한다.
	* 코드를 사용하려면 무조건 이 함수를 먼저 실행시켜줘야 한다.
	* @param con DB Connection
	*/
	public static void initCodes(Connection conn){
		Codes code = new Codes();
		try{
			if (conn != null){
				if (CITY_NAME_CODE == null || CITY_NAME_EN_CODE == null || CITY_CODE_NATION_CODE == null)
					code.setCityCodes(conn);
				
				if (NATION_NAME_NATION_CODE == null || NATION_NAME_EN_NATION_CODE == null || NATION_CODE_CONTINENT_CODE == null)
					code.setNationCodes(conn);
				
				if (CONTINENT_NAME_CONTINENT_CODE == null || CONTINENT_NAME_EN_CONTINENT_CODE == null)
					code.setContinentCodes(conn);
				
				if (PRD_CLASS == null)
					code.setPrdClassCodes(conn);
					
				if (PRD_STATUS == null)
					code.setPrdStatusCodes(conn);
				
				if (ARPT_NAME_CODE == null)
					code.setArptNameCode(conn);
				
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	* CITY_NAME_CODES, CITY_NAME_EN_CODES, CITY_CODES_NATION_CODE 변수를 세팅한다.
	* @param con DB Connection
	* @throws Exception
	*/
	public void setCityCodes(Connection conn) throws Exception{
		CITY_NAME_CODE = new HashMap<String, String>();
		CITY_NAME_EN_CODE = new HashMap<String, String>();
		CITY_CODE_NATION_CODE = new HashMap<String, String>();
		
		String query = "SELECT CITY_CD, CITY_NM, CITY_NM_EN, NT_CD"
						+ " FROM TMP_CITY";
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		
		while(rs.next()){
			CITY_NAME_CODE.put(rs.getString("CITY_NM"), rs.getString("CITY_CD"));
			CITY_NAME_EN_CODE.put(rs.getString("CITY_NM_EN"), rs.getString("CITY_CD"));
			CITY_CODE_NATION_CODE.put(rs.getString("CITY_CD"), rs.getString("NT_CD"));
		}
	}
	
	
	/**
	* NATION_NAME_NATION_CODE, NATION_NAME_EN_NATION_CODE, NATION_CODE_CONTINENT_CODE 변수를 세팅한다.
	* @param con DB Connection
	* @throws Exception
	*/
	public void setNationCodes(Connection conn) throws Exception{
		NATION_NAME_NATION_CODE = new HashMap<String, String>();
		NATION_NAME_EN_NATION_CODE = new HashMap<String, String>();
		NATION_CODE_CONTINENT_CODE = new HashMap<String, String>();
		
		String query = "SELECT NT_CD, NT_NM, NT_NM_EN, CNTT_CD"
						+ " FROM TMP_NT";
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		
		while(rs.next()){
			NATION_NAME_NATION_CODE.put(rs.getString("NT_NM"), rs.getString("NT_CD"));
			NATION_NAME_EN_NATION_CODE.put(rs.getString("NT_NM_EN"), rs.getString("NT_CD"));
			NATION_CODE_CONTINENT_CODE.put(rs.getString("NT_CD"), rs.getString("CNTT_CD"));
		}
	}
	
	
	/**
	* CONTINENT_NAME_CONTINENT_CODE, CONTINENT_NAME_EN_CONTINENT_CODE 변수를 세팅한다.
	* @param con DB Connection
	* @throws Exception 
	*/
	public void setContinentCodes(Connection conn) throws Exception{
		CONTINENT_NAME_CONTINENT_CODE = new HashMap<String, String>();
		CONTINENT_NAME_EN_CONTINENT_CODE = new HashMap<String, String>();
		
		String query = "SELECT CNTT_CD, CNTT_NM, CNTT_NM_EN"
						+ " FROM TMP_CNTT";
		
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		
		while(rs.next()){
			CONTINENT_NAME_CONTINENT_CODE.put(rs.getString("CNTT_NM"), rs.getString("CNTT_CD"));
			CONTINENT_NAME_EN_CONTINENT_CODE.put(rs.getString("CNTT_NM_EN"), rs.getString("CNTT_CD"));
		}
	}
	
	
	/**
	* PRD_CLASS 변수를 세팅한다.
	* @param con DB Connection
	* @throws Exception 
	*/
	public void setPrdClassCodes(Connection conn) throws Exception{
		PRD_CLASS = new HashMap<String, String>();
		
		String query = "SELECT ST_CD, ST_CD_NM"
						+ " FROM tcm_std_cd"
						+ " where ST_CD_GRP='PD'";
		
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		
		while(rs.next()){
			PRD_CLASS.put(rs.getString("ST_CD_NM"), rs.getString("ST_CD"));
		}
	}
	
	
	/**
	* PRD_STATUS 변수를 세팅한다.
	* @param con DB Connection
	* @throws Exception 
	*/
	public void setPrdStatusCodes(Connection conn) throws Exception{
		PRD_STATUS = new HashMap<String, String>();
		
		String query = "SELECT ST_CD, ST_CD_NM"
						+ " FROM tcm_std_cd"
						+ " where ST_CD_GRP='PS'";
		
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		
		while(rs.next()){
			PRD_STATUS.put(rs.getString("ST_CD_NM"), rs.getString("ST_CD"));
		}
	}
	
	
	/**
	* ARPT_NAME_CODE 변수를 세팅한다.
	* @param con DB Connection
	* @throws Exception 
	*/
	public void setArptNameCode(Connection conn) throws Exception{
		ARPT_NAME_CODE = new HashMap<String, String>();
		
		String query = "SELECT ARPT_NM, ARPT_CD"
						+ " FROM TCM_ARPT";
						
		
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		
		while(rs.next()){
			ARPT_NAME_CODE.put(rs.getString("ARPT_NM"), rs.getString("ARPT_CD"));
		}
	}
	
	
	/**
	* 도시코드를 이용하여 국가코드, 대륙 코드를 조회, 결과값을 반환한다.
	* @param con DB Connection
	* @param cityCode 도시코드
	* @return 도시코드/국가코드/대륙코드 
	*/
	public static String getAreaStringByCityCode(String cityCode){
		String nationCode = "";
		String continentCode = "";
		
		nationCode = CITY_CODE_NATION_CODE.get(cityCode) == null ? "" : CITY_CODE_NATION_CODE.get(cityCode);
		continentCode = NATION_CODE_CONTINENT_CODE.get(nationCode) == null ? "" : NATION_CODE_CONTINENT_CODE.get(nationCode);
		
		return cityCode + "/" + nationCode + "/" +  continentCode;
	}
	
	/**
	* 국가코드를 이용하여 국가코드, 대륙 코드를 조회, 결과값을 반환한다.
	* @param con DB Connection
	* @param nationCode 도시코드
	* @return /국가코드/대륙코드 
	*/
	public static String getAreaStringByNationCode(String nationCode){
		String continentCode = "";
		
		continentCode = NATION_CODE_CONTINENT_CODE.get(nationCode) == null ? "" : NATION_CODE_CONTINENT_CODE.get(nationCode);
		
		return "/" + nationCode + "/" +  continentCode;
	}
	
	
	/**
	* 문자열에 포함된 도시명을 검색해 도시 코드 목록을 반환한다.
	* @param str 검색하고자 하는 문자열
	* @return 문자열에 포함된 도시 코드 
	*/
	public static ArrayList<String> findCityCodeByName(String str){
		ArrayList<String> cityCodes = new ArrayList<String>();
		
		Set<String> cityNameSet = CITY_NAME_CODE.keySet();
		
		for (String cityName : cityNameSet){
			if (str.contains(cityName)) cityCodes.add(CITY_NAME_CODE.get(cityName));
		}
		
		return cityCodes;
	}
	
	
	/**
	* 문자열에 포함된 국가명을 검색해 국가 코드 목록을 반환한다.
	* @param str 검색하고자 하는 문자열
	* @return 문자열에 포함된 국가 코드 
	*/
	public static ArrayList<String> findNationCodeByName(String str){
		ArrayList<String> nationCodes = new ArrayList<String>();
		Set<String> nationNameSet = NATION_NAME_NATION_CODE.keySet();
		
		for (String nationName : nationNameSet){
			if (str.contains(nationName)) nationCodes.add(NATION_NAME_NATION_CODE.get(nationName));
		}
		
		return nationCodes;
	}
	
	
	/**
	* 문자열에 포함된 대륙명을 검색해 대륙 코드 목록을 반환한다.
	* @param str 검색하고자 하는 문자열
	* @return 문자열에 포함된 대륙 코드 
	*/
	public static ArrayList<String> findContinentCodeByName(String str){
		ArrayList<String> continentCodes = new ArrayList<String>();
		Set<String> continentNameSet = CONTINENT_NAME_CONTINENT_CODE.keySet();
		
		for (String continentName : continentNameSet){
			if (str.contains(continentName)) continentCodes.add(CONTINENT_NAME_CONTINENT_CODE.get(continentName));
		}
		
		return continentCodes;
	}
	
	
	/**
	* 문자열에 포함된 대륙명을 검색해 대륙 코드 목록을 반환한다.
	* @param str 검색하고자 하는 문자열
	* @return 문자열에 포함된 대륙 코드 
	*/
	public static ArrayList<String> findGetAreaString(String str){
		ArrayList<String> areaStringList = new ArrayList<String>();
		
		ArrayList<String> cityCodeList = Codes.findCityCodeByName(str);
		ArrayList<String> nationCodeList = Codes.findNationCodeByName(str);
		ArrayList<String> continentCodeList = Codes.findContinentCodeByName(str);
		
		for (String cityCode : cityCodeList){
			String cityNationCode = Codes.CITY_CODE_NATION_CODE.get(cityCode);
			
			int i = 0;
			while(true){
				if (i >= nationCodeList.size()) break;
				
				String nationCode = nationCodeList.get(i);
				if (cityNationCode.equals(nationCode)){
					nationCodeList.remove(i);
				}else{
					i++;
				}
			}
			
			String cityContinentCode = Codes.NATION_CODE_CONTINENT_CODE.get(cityNationCode);
			i = 0;
			while(true){
				if (i >= continentCodeList.size()) break;
				
				String continentCode = continentCodeList.get(i);
				if (cityContinentCode.equals(continentCode)){
					continentCodeList.remove(i);
				}else{
					i++;
				}
			}
			
			areaStringList.add(Codes.getAreaStringByCityCode(cityCode));
		}
		
		for (String nationCode : nationCodeList){
			String nationContinentCode = Codes.NATION_CODE_CONTINENT_CODE.get(nationCode);
			int i = 0;
			while(true){
				if (i >= continentCodeList.size()) break;
				
				String continentCode = continentCodeList.get(i);
				if (nationContinentCode.equals(continentCode)){
					continentCodeList.remove(i);
				}else{
					i++;
				}
			}
			areaStringList.add(Codes.getAreaStringByNationCode(nationCode));
		}
		
		for (String continentCode : continentCodeList){
			areaStringList.add("//" + continentCode);
		}
		
		return areaStringList;
	}
	
}
