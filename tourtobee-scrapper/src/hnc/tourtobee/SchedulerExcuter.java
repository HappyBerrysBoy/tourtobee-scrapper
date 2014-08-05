package hnc.tourtobee;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;
import hnc.tourtobee.schedulerjob.ScrapPrdPrdDtlSummary;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;

public class SchedulerExcuter {
	public static void main(String[] args){
		
		try {
			SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
			
			Scheduler sched = schedFact.getScheduler();
			
			sched.start();
			JobDetail job = newJob(ScrapPrdPrdDtlSummary.class).withIdentity("vesselScheduleJob", "vesselSchedule").build();
			Trigger trigger = newTrigger().withIdentity("vesselScheduleTrigger", "vesselSchedule").withSchedule(cronSchedule("0 0,30 * * * ?")).forJob("vesselScheduleJob", "vesselSchedule").build();
			
			sched.scheduleJob(job, trigger);
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
}
