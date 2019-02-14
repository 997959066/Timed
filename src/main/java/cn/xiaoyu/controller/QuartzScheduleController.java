package cn.xiaoyu.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cn.xiaoyu.entity.WatchJobList;

@RestController
@RequestMapping(value = "/job", produces = {"application/json;charset=UTF-8"})
public class QuartzScheduleController{

    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired @Qualifier("Scheduler")
    private Scheduler scheduler;
    
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public List<WatchJobList> getJobList() {
        try {
            List<WatchJobList> lst = new ArrayList<>();
            
            for (String groupName : scheduler.getTriggerGroupNames()) {
              
                for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.anyJobGroup())) {
                    WatchJobList watchJob = new WatchJobList();
                    watchJob.setJobName(jobKey.getName());
                    watchJob.setJobGroup(groupName);
                    
                    JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                    JobDataMap jobDataMap = jobDetail.getJobDataMap();
                    for (Entry<?, ?> entry : jobDataMap.entrySet()){
                        MethodInvokingJobDetailFactoryBean jobDetailBean = (MethodInvokingJobDetailFactoryBean) entry.getValue();
                        Object[] obj = jobDetailBean.getArguments();
                        if (obj[0] != null){
                            watchJob.setJobArguments(obj[0].toString());
                        }
                        if (obj[3] != null){
                        	watchJob.setFailCallbackUrl(obj[3].toString());
                        }
                        if (obj[4] != null){
                            watchJob.setRetryCount(Integer.valueOf(obj[4].toString()));
                        }
                        if (obj[5] != null){
                            watchJob.setId(Integer.valueOf(obj[5].toString()));
                        }
                     
                    }
                    lst.add(watchJob);
                }
            }
            return lst;
        } catch (Exception e) {
            
            logger.error(e.getMessage());
            return null;
        }
    }
    
    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public void deleteJob(String jobId, String jobGroup) {
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(jobId, jobGroup);
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            scheduler.pauseTrigger(triggerKey);// 停止触发器
            scheduler.unscheduleJob(triggerKey);// 移除触发器
            scheduler.deleteJob(trigger.getJobKey());// 删除任务
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
    
}
