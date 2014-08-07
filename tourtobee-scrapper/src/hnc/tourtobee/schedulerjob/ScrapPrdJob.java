package hnc.tourtobee.schedulerjob;

import static hnc.tourtobee.util.Util.log;
import hnc.tourtobee.scrapper.ScrappingEngine;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import jh.project.httpscrapper.ScrapItem;
import jh.project.httpscrapper.Scrapper;
import jh.project.httpscrapper.Website;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
/**
 * Prd와 PrdDtl을 입력하는 Job (Quratz)
 * @author purepleya
 *
 */
public class ScrapPrdJob implements Job{
	private int threadNo;
	private int noOfThreads;
	private int scrapPeriod;
	
	public int getThreadNo() {
		return threadNo;
	}
	public void setThreadNo(int threadNo) {
		this.threadNo = threadNo;
	}
	public int getNoOfThreads() {
		return noOfThreads;
	}
	public void setNoOfThreads(int noOfThreads) {
		this.noOfThreads = noOfThreads;
	}
	public int getScrapPeriod() {
		return scrapPeriod;
	}
	public void setScrapPeriod(int scrapPeriod) {
		this.scrapPeriod = scrapPeriod;
	}
	
	
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MONTH, this.scrapPeriod);
		String toMonth = String.format("%04d", c.get(Calendar.YEAR)) 
										+ String.format("%02d", c.get(Calendar.MONTH) + 1);
		HashMap<String, String> options = new HashMap<String, String>();
		options.put("until", toMonth);
		
		try {
			ScrappingEngine se = new ScrappingEngine();
			Scrapper sc = new Scrapper();
			ArrayList<ScrapItem> scItemList = sc.getScrapItem();
			
			for(ScrapItem scItem : scItemList){
				ArrayList<Website> websiteList = sc.getWebsite(scItem);
				for(int i = 0 ; i < websiteList.size() ; i++){
					if ((i % noOfThreads) != threadNo) continue;
					Website website = websiteList.get(i);
					log(website.getId(), "ScrapPrdJob Start !!!");
					se.scrapPrd(website, options);
					log(website.getId(), "ScrapPrdJob Finish !!!");
				}
			}
		
		} catch (Exception e) {
			throw new JobExecutionException(e.toString());
		}
	}

}
