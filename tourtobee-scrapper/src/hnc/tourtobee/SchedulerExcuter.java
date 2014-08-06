package hnc.tourtobee;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;
import static hnc.tourtobee.util.Util.log;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import hnc.tourtobee.schedulerjob.ScrapPrdDtlSummaryJob;
import hnc.tourtobee.schedulerjob.ScrapPrdJob;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.xml.internal.ws.wsdl.writer.UsingAddressing;

public class SchedulerExcuter {
	public static void main(String[] args){
		
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			String fileName = new File(".").getCanonicalPath() + "/cfg/SchedulerConfig.xml";
			File xmlFile = new File(fileName);
			Document itemDoc = docBuilder.parse(xmlFile);
			Element noThreadsElement = (Element)itemDoc.getElementsByTagName("no-threads").item(0);
			Element scrapPeriodElement = (Element)itemDoc.getElementsByTagName("scrap-period").item(0);
			Element scrapSceduleElement = (Element)itemDoc.getElementsByTagName("scrap-schedule").item(0);
			Element scrapDtlSummaryPeriodElement = (Element)itemDoc.getElementsByTagName("prddtl-summary-period").item(0);
			Element scrapDtlSummaryScheduleElement = (Element)itemDoc.getElementsByTagName("prddtl-summary-schedule").item(0);
			
			int noThreads = Integer.parseInt(noThreadsElement.getTextContent());
			int scrapPeriod = Integer.parseInt(scrapPeriodElement.getTextContent());
			String scrapSchedule = scrapSceduleElement.getTextContent();
			int scrapDtlSummaryPeriod = Integer.parseInt(scrapDtlSummaryPeriodElement.getTextContent());
			String scrapDtlSummarySchedule = scrapDtlSummaryScheduleElement.getTextContent();
			
			
			SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
			Scheduler sched = schedFact.getScheduler();
			sched.start();
			
//			Prd와 PrdDtlSummary를 입력하는(ScrapPrd) Job 생성
			if (scrapSchedule != null & scrapSchedule.trim() != ""){
				JobBuilder prdJobBuilder= newJob(ScrapPrdJob.class)
											.usingJobData("noOfThreads", noThreads)
											.usingJobData("scrapPeriod", scrapPeriod);
				
				Trigger prdTrigger = newTrigger().withIdentity("ScrapPrd", "ScrapPrdGroup")
						.withSchedule(cronSchedule(scrapSchedule))
						.build();
				
				//XML 에서 설정한 thread 갯수만큼 job을 생성시켜 실행 한다.
				for(int i = 0; i < noThreads ; i++){
					prdJobBuilder.usingJobData("threadNo", i);
					JobDetail job = prdJobBuilder.withIdentity("ScrapPrd" + String.valueOf(i), "ScrapPrdGroup")
													.build();
					sched.scheduleJob(job, prdTrigger);
				}
			}
			
			
//			PrdDtlSummary를 입력하는(ScrapDtlSummary) Job 생성
			if (scrapDtlSummarySchedule != null & scrapDtlSummarySchedule.trim() != ""){
				JobBuilder prdDtlSummaryJobBuilder= newJob(ScrapPrdDtlSummaryJob.class)
						.usingJobData("noOfThreads", noThreads)
						.usingJobData("scrapPeriod", scrapDtlSummaryPeriod);
				
				Trigger prdDtlSummaryTrigger = newTrigger().withIdentity("ScrapDtlSummary", "ScrapDtlSummaryGroup")
															.withSchedule(cronSchedule(scrapSchedule))
															.build();
				//XML 에서 설정한 thread 갯수만큼 job을 생성시켜 실행 한다.
				for(int i = 0; i < noThreads ; i++){
					prdDtlSummaryJobBuilder.usingJobData("threadNo", i);
					JobDetail job = prdDtlSummaryJobBuilder.withIdentity("ScrapDtlSummary" + String.valueOf(i), "ScrapDtlSummaryGroup")
															.build();
					sched.scheduleJob(job, prdDtlSummaryTrigger);
				}
			}
			
		} catch (Exception e) {
			log("SchedulerExcuter", e.toString());
		}
			
	}
}
