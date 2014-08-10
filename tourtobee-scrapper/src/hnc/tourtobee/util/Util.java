package hnc.tourtobee.util;

import static hnc.tourtobee.util.Util.getSystemDate;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;

import sun.org.mozilla.javascript.internal.json.JsonParser.ParseException;

public class Util {
	
	/**
	* 현재 시스템의 년월부터 지정한 년월 까지의 년월 목록을 구한다.
	* @param to 지정한 년월 (이때까지의 년월 목록)
	* @return 년월(yyyymm) set
	*/
	public static HashSet<String> getYearMonthSet(String to){
		HashSet<String> yearMonthSet = new HashSet<String>();
		Calendar c = Calendar.getInstance();
		String from = String.format("%04d", c.get(Calendar.YEAR)) + String.format("%02d", c.get(Calendar.MONTH) + 1);
		
		if(to.length() != 6 || Integer.parseInt(from) > Integer.parseInt(to)){
			return yearMonthSet;
		}
		
		String year = from.substring(0, 4);
		String month = from.substring(4, 6);
		
		while(true){
			yearMonthSet.add(year + month);
			if ((year + month).equals(to)) break;
			
			if (Integer.parseInt(month) >= 12){
				year = String.valueOf(Integer.parseInt(year) + 1);
				month = "01";
			}else{
				month = String.format("%02d", Integer.parseInt(month) + 1);
			}
				
		}
		
		return yearMonthSet;
	}
	
	
	/**
	* 현재 시스템의 날자를 반환한다
	* @return yyyymm
	*/
	public static String getSystemMonth(){
		Calendar c = Calendar.getInstance();
		return String.format("%04d", c.get(Calendar.YEAR)) 
				+ String.format("%02d", c.get(Calendar.MONTH) + 1);
	}
	
	/**
	* 현재 시스템의 날자를 반환한다
	* @return yyyymmdd
	*/
	public static String getSystemDate(){
		Calendar c = Calendar.getInstance();
		return String.format("%04d", c.get(Calendar.YEAR)) 
				+ String.format("%02d", c.get(Calendar.MONTH) + 1)
				+ String.format("%02d", c.get(Calendar.DAY_OF_MONTH));
	}
	
	
	/**
	* 며칠 후 의 날자를 반환한다
	* @return yyyymmdd
	*/
	public static String getDateAfter(String amount){
		if (amount == null || amount.length() == 0) amount = "0";
		int intAmount = Integer.parseInt(amount);
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_MONTH, intAmount);
		return String.format("%04d", c.get(Calendar.YEAR)) 
				+ String.format("%02d", c.get(Calendar.MONTH) + 1)
				+ String.format("%02d", c.get(Calendar.DAY_OF_MONTH));
	}
	
	
	
	/**
	* 며칠 후 의 날자 요소 (년, 월, 일)을 반환한다
	* @param field Calendar.YEAR 또는 Calendar.MONTH 또는 Calendar.DAY_OF_MONTH
	* @param amount 며칠 후 를 조회 할 것인가
	* @return yyyy 또는 mm 또는 dd
	*/
	public static String getDateAfter(int field, String amount){
		if (amount == null || amount.length() == 0) amount = "0";
		int intAmount = Integer.parseInt(amount);
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_MONTH, intAmount);
		
		if (field == Calendar.YEAR){
			return String.format("%04d", c.get(Calendar.YEAR));
		}else if (field == Calendar.MONTH){
			return String.format("%02d", c.get(Calendar.MONTH) + 1);
		}else if (field == Calendar.DAY_OF_MONTH){
			return String.format("%02d", c.get(Calendar.DAY_OF_MONTH));
		}else{
			return String.format("%04d", c.get(Calendar.YEAR)) 
					+ String.format("%02d", c.get(Calendar.MONTH) + 1)
					+ String.format("%02d", c.get(Calendar.DAY_OF_MONTH));
		}
		
	}
	
	
	
	/**
	* 며칠 후 의 날자를 반환한다
	* @param separator 년, 월, 일 구분자
	* @param amount 며칠 후 를 조회 할 것인가
	* @return yyyy (separator) mm (separator) dd 의 형태(separator로 년,월,일 을 구분함)
	*/
	public static String getDateAfter(String separator, String amount){
		if (amount == null || amount.length() == 0) amount = "0";
		int intAmount = Integer.parseInt(amount);
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_MONTH, intAmount);
		return String.format("%04d", c.get(Calendar.YEAR)) 
				+ separator + String.format("%02d", c.get(Calendar.MONTH) + 1)
				+ separator + String.format("%02d", c.get(Calendar.DAY_OF_MONTH));
	}
	
	
	/**
	* 현재 시스템의 시간을 반환한다
	* @return hh24miss
	*/
	public static String getSystemTime(){
		Calendar c = Calendar.getInstance();
		return String.format("%02d", c.get(Calendar.HOUR_OF_DAY))
				+ String.format("%02d", c.get(Calendar.MINUTE))
				+ String.format("%02d", c.get(Calendar.SECOND));
	}
	
	
	/**
	* 현재 시스템의 날자와 시간을 반환한다
	* @return yyyymmddhh24miss
	*/
	public static String getSystemDateTime(){
		Calendar c = Calendar.getInstance();
		return String.format("%04d", c.get(Calendar.YEAR)) 
				+ String.format("%02d", c.get(Calendar.MONTH) + 1)
				+ String.format("%02d", c.get(Calendar.DAY_OF_MONTH))
				+ String.format("%02d", c.get(Calendar.HOUR_OF_DAY))
				+ String.format("%02d", c.get(Calendar.MINUTE))
				+ String.format("%02d", c.get(Calendar.SECOND));
	}
	
	
	/**
	* 로그를 출력한다.
	* @param title 타이틀
	* @param content 내용
	* @return "[시간] 타이틀 : 내용" 형식의 로그를 출력한다.
	*/
	public static void log(String title, String content){
		Calendar c = Calendar.getInstance();
		String now = String.format("%04d", c.get(Calendar.YEAR)) 
				+ "/" + String.format("%02d", c.get(Calendar.MONTH) + 1)
				+ "/" + String.format("%02d", c.get(Calendar.DAY_OF_MONTH))
				+ " " + String.format("%02d", c.get(Calendar.HOUR_OF_DAY))
				+ ":" + String.format("%02d", c.get(Calendar.MINUTE))
				+ ":" + String.format("%02d", c.get(Calendar.SECOND));
		
		System.out.println("[" + now + "]" + title + " : " + content);
		
		try {
		      ////////////////////////////////////////////////////////////////
			  BufferedWriter out = new BufferedWriter(new FileWriter("log.txt",true));
		      String s = "[" + now + "]" + title + " : " + content;

		      out.append(s);
		      out.newLine();

		      out.close();
		      ////////////////////////////////////////////////////////////////
		    } catch (IOException e) {
		        System.err.println(e); // 에러가 있다면 메시지 출력
		        System.exit(1);
		    }
	}
	
	
	/**
	* 특정문자열을 정규식으로 사용할경우 특수문자 (., ?, [, ...) 가 문제가되는데 이를 정리해줌
	* @param ori 원 문자열
	* @return 정규식 문자열
	*/
	public static String makeRegex(String ori){
		String regex = ori;
		
		regex = regex.replaceAll("\\.", "\\\\\\.");
		regex = regex.replaceAll("\\\\", "\\\\\\\\");
		regex = regex.replaceAll("\\|", "\\\\\\|");
		regex = regex.replaceAll("\\^", "\\\\\\^");
		regex = regex.replaceAll("\\[", "\\\\\\[");
		regex = regex.replaceAll("\\]", "\\\\\\]");
		regex = regex.replaceAll("\\(", "\\\\\\(");
		regex = regex.replaceAll("\\)", "\\\\\\)");
		regex = regex.replaceAll("\\*", "\\\\\\*");
		regex = regex.replaceAll("\\+", "\\\\\\+");
		regex = regex.replaceAll("\\?", "\\\\\\?");
		regex = regex.replaceAll("\\{", "\\\\\\{");
		regex = regex.replaceAll("\\}", "\\\\\\}");
//		regex = regex.replaceAll("\\/", "\\\\/");
		
//		regex = regex.replaceAll("\\.", "\\.");
//		regex = regex.replaceAll("\\\\", "\\\\");
//		regex = regex.replaceAll("\\|", "\\|");
//		regex = regex.replaceAll("\\^", "\\^");
//		regex = regex.replaceAll("\\[", "\\[");
//		regex = regex.replaceAll("\\]", "\\]");
//		regex = regex.replaceAll("\\(", "\\(");
//		regex = regex.replaceAll("\\)", "\\)");
//		regex = regex.replaceAll("\\*", "\\*");
//		regex = regex.replaceAll("\\+", "\\+");
//		regex = regex.replaceAll("\\?", "\\?");
//		regex = regex.replaceAll("\\{", "\\{");
//		regex = regex.replaceAll("\\}", "\\}");
//		regex = regex.replaceAll("\\/", "\\/");
		return regex;
	}
	
	
	public static String setOperationDate(String inputDate, int value){
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		Date date = null;
		try{
			date = dateFormat.parse(inputDate);
		}catch(java.text.ParseException e){
			e.printStackTrace();
		}
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, value);
		return dateFormat.format(cal.getTime());
	}
}
