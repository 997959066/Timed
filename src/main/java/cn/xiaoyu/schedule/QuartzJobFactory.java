package cn.xiaoyu.schedule;

import java.util.List;
import java.util.Map.Entry;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.stereotype.Component;

import cn.xiaoyu.common.Constants;
import cn.xiaoyu.entity.ScheduleJob;
import cn.xiaoyu.entity.WatchJobList;
import cn.xiaoyu.service.impl.QuartzScheduleServiceImpl;
import cn.xiaoyu.utils.QuartzUtils;

@Component
@DisallowConcurrentExecution
public class QuartzJobFactory { // 实现的是无状态的Job
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private QuartzScheduleServiceImpl quartzSchedule;

	@Autowired
	@Qualifier("Scheduler")
	private Scheduler scheduler;
	
	/**
	 * @Note : 扫面数据库,查看是否有计划任务的变动
	 */
	@Scheduled(cron = "0/30 * * * * ?")
	public void arrageScheduleJob() {
		try {
			// 从数据库中获取任务列表,查询开始前五分中 和结束后五分钟任务
			List<ScheduleJob> jobList = quartzSchedule.selectJob();
			if (!jobList.isEmpty()) {
				for (ScheduleJob job : jobList) {
					// Keys are composed of both a name and group, and the name
					// must be unique within the group
					TriggerKey triggerKey = TriggerKey.triggerKey(QuartzUtils.jobNameId(job), Constants.GROUP_NAME);
					// 获取trigger
					CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
					// 是否存在trigger
					if (null == trigger) { // 不存在
						if (job.getJobStatus() == 0 || job.getJobStatus() == 1) { // job状态正常
																					// 0-初始,1-启用,3-删除
							if (!QuartzUtils.isExpired(QuartzUtils.stringDate(job.getExpiredDate()))) {
								quartzSchedule.updateStatus(job.getId(), 1);
								createSheduler(scheduler, job);
							} else {
								// set job.status = 2
								quartzSchedule.updateStatus(job.getId(), 2);
								logger.info("set job.status = 2, 1:" + job.getId().toString());
							}
						} else {
							// set job.status = 2
							quartzSchedule.updateStatus(job.getId(), 2);
							logger.info("set job.status = 2, 2:" + job.getId().toString());
						}

					} else {// Trigger已存在，那么更新相应的定时设置
						this.updateMemoryTask(jobList, job, trigger);
						updateScheduler(scheduler, job, triggerKey, trigger);
					}
				}
			}
		} catch (SchedulerException s) {
		    if (logger.isErrorEnabled()) {
                logger.error(s.getMessage());
            }
		}  catch (Exception e) {
		    if (logger.isErrorEnabled()) {
                logger.error(e.getMessage());
            }
		}
	}
	
	/**
	 * 针对录入人员修改定时任务 表达式  + url
	 * 
	 */
	private void updateMemoryTask(List<ScheduleJob> jobList, ScheduleJob job, CronTrigger trigger) {
		try {
			for (String groupName : scheduler.getTriggerGroupNames()) {
				for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.anyJobGroup())) {
					WatchJobList watchJob = new WatchJobList();
					watchJob.setJobName(jobKey.getName());
					watchJob.setJobGroup(groupName);
					JobDetail jobDetail = scheduler.getJobDetail(jobKey);
					JobDataMap jobDataMap = jobDetail.getJobDataMap();
					for (Entry<?, ?> entry : jobDataMap.entrySet()) {
						MethodInvokingJobDetailFactoryBean jobDetailBean = (MethodInvokingJobDetailFactoryBean) entry
								.getValue();
						Object[] obj = jobDetailBean.getArguments();
						// 从数据库中获取任务列表
						if (jobList.size() != 0) {
							if (Integer.valueOf(obj[5].toString()).intValue() == job.getId()) {
								if (!job.getJobUrl().equalsIgnoreCase(obj[0].toString())
										|| !job.getCronExpression().equalsIgnoreCase(trigger.getCronExpression())
										) {
									// url改了之后删掉
									TriggerKey triggerKeyss = TriggerKey.triggerKey(jobKey.getName(), groupName);
									CronTrigger triggerss = (CronTrigger) scheduler.getTrigger(triggerKeyss);
									scheduler.pauseTrigger(triggerKeyss);// 停止触发器
									scheduler.unscheduleJob(triggerKeyss);// 移除触发器
									scheduler.deleteJob(triggerss.getJobKey());// 删除任务
									logger.info("删除内存中正在运行的任务======" + triggerss.toString() + ":" + triggerss.getJobKey());
									// 新建一个基于Spring的管理Job类
									MethodInvokingJobDetailFactoryBean jobDetailBeans = new MethodInvokingJobDetailFactoryBean();
									// 设置Job名称 要求唯一
									jobDetailBeans.setName(QuartzUtils.jobNameId(job));
									jobDetailBeans.setTargetObject(Class.forName(job.getTargetObject()).newInstance());
									// 设置任务方法
									jobDetailBeans.setTargetMethod(job.getTargetMethod());
									// 设置参数
									jobDetailBeans.setArguments(job.getJobUrl(), job.getFailCallbackUrl(),
											job.getRetryCount(), job.getId());
									// 将管理Job类提交到计划管理类
									jobDetailBeans.afterPropertiesSet();
									//并发设置
									jobDetailBeans.setConcurrent(job.getConcurrent() == 1 ? true : false);
									JobDetail jobDetails = jobDetailBeans.getObject();// 动态
									// 表达式调度构建器
									CronScheduleBuilder scheduleBuilder = CronScheduleBuilder
											.cronSchedule(job.getCronExpression());
									// 按新的cronExpression表达式构建一个新的trigger
									CronTrigger triggers = TriggerBuilder.newTrigger()
											.withIdentity(QuartzUtils.jobNameId(job), Constants.GROUP_NAME)
											.withSchedule(scheduleBuilder).build();
									scheduler.scheduleJob(jobDetails, triggers);// 注入到管理类
									logger.info("启动修改内存后任务======" + triggers.toString() + ":" + triggers.getJobKey());
								}
							}
						}
					}
				}
			}
		} catch (SchedulerException e1) {
			quartzSchedule.updateStatus(job.getId(), 2);
			logger.error("建立scheduleJob失败:" + job.getCronExpression() + "," + e1.getMessage());
		} catch (Exception e2) {
			quartzSchedule.updateStatus(job.getId(), 2);
			logger.error(e2.getMessage());
		}
	}

	/**
	 * 更新相应的定时设置 根据job_status做相应的处理
	 * 
	 * @param scheduler
	 * @param job
	 * @param triggerKey
	 * @param trigger
	 * @throws SchedulerException
	 */
	private void updateScheduler(Scheduler scheduler, ScheduleJob job, TriggerKey triggerKey, CronTrigger trigger)
			throws SchedulerException, Exception {
		try {
			if (job.getJobStatus() == 0 || job.getJobStatus() == 1) { // 0-初始,1-启用,3-删除
				if (!QuartzUtils.isExpired(QuartzUtils.stringDate(job.getExpiredDate()))) { // 没有超时
					if (!trigger.getCronExpression().equalsIgnoreCase(job.getCronExpression())) {//内存中正在运行的表达式比较 数据库中表达式
						// 表达式调度构建器
						CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression())
								.withMisfireHandlingInstructionDoNothing();
						// 按新的cronExpression表达式重新构建trigger
						trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder)
								.build();

						// 按新的trigger重新设置job执行
						scheduler.rescheduleJob(triggerKey, trigger);

						if (job.getJobStatus() == 0) {
							quartzSchedule.updateStatus(job.getId(), 1);
						}
					}
				} else { // 任务超过截至时间
							// delete trigger and set job.status = 2
					deleteScheduler(scheduler, triggerKey, trigger);
					quartzSchedule.updateStatus(job.getId(), 2);
					logger.info("set job.status = 2, 3:" + job.getId());
				}
			} else { // 任务状态为删除
						// delete trigger and set job.status = 2
				deleteScheduler(scheduler, triggerKey, trigger);
				quartzSchedule.updateStatus(job.getId(), 2);
				logger.info("set job.status = 2, 4:" + job.getId());
			}
		} catch (SchedulerException e1) {
			quartzSchedule.updateStatus(job.getId(), 2);
			logger.error("更新scheduleJob失败:" + job.getCronExpression() + "," + e1.getMessage());
		} catch (Exception e2) {
			quartzSchedule.updateStatus(job.getId(), 2);
			logger.error(e2.getMessage());
		}
	}

	/**
	 * 删除定时任务
	 * 
	 * @param scheduler
	 * @param triggerKey
	 * @param trigger
	 * 
	 */
	public void deleteScheduler(Scheduler scheduler, TriggerKey triggerKey, CronTrigger trigger)
			throws SchedulerException {
		scheduler.pauseTrigger(triggerKey);// 停止触发器
		scheduler.unscheduleJob(triggerKey);// 移除触发器
		scheduler.deleteJob(trigger.getJobKey());// 删除任务
		logger.info("删除定时任务=========:" + triggerKey.toString() + ":" + trigger.getJobKey());
	}

	/**
	 * 创建一个定时任务，并做安排
	 * 
	 * @param scheduler
	 * @param job
	 * @throws Exception
	 */
	public void createSheduler(Scheduler scheduler, ScheduleJob job)
			throws SchedulerException, Exception {
		try {
			// 在工作状态可用时,即jobStatus = 0 ,开始创建
			if (job.getJobStatus() == 0 || job.getJobStatus() == 1) {
				// 新建一个基于Spring的管理Job类
				MethodInvokingJobDetailFactoryBean jobDetailBean = new MethodInvokingJobDetailFactoryBean();
				// 设置Job名称 要求唯一
				jobDetailBean.setName(QuartzUtils.jobNameId(job));
				jobDetailBean.setTargetObject(Class.forName(job.getTargetObject()).newInstance());

				// 设置任务方法
				jobDetailBean.setTargetMethod(job.getTargetMethod());
				// 设置参数
				jobDetailBean.setArguments(job.getJobUrl(),job.getJobParam(),job.getJobHeader() ,job.getFailCallbackUrl(), job.getRetryCount(),
						job.getId());

				// 将管理Job类提交到计划管理类
				jobDetailBean.afterPropertiesSet();
				/** 并发设置 */
				jobDetailBean.setConcurrent(job.getConcurrent() == 1 ? true : false);

				JobDetail jobDetail = jobDetailBean.getObject();// 动态

				// jobNameId存入到队列 每隔一段时间就会扫描所以需要时检测
				// if(!QuartzUtil.jobNames.contains(job.getJobNameId())){
				// QuartzUtil.jobNames.add(job.getJobNameId());
				// }

				// 表达式调度构建器
				CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());
				// 按新的cronExpression表达式构建一个新的trigger
				CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(QuartzUtils.jobNameId(job), Constants.GROUP_NAME)
						.withSchedule(scheduleBuilder).build();
				scheduler.scheduleJob(jobDetail, trigger);// 注入到管理类
				logger.info("启动任务======" + trigger.toString() + ":" + trigger.getJobKey());
			}
		} catch (SchedulerException e1) {
			quartzSchedule.updateStatus(job.getId(), 2);
			logger.error("建立scheduleJob失败:" + job.getCronExpression() + "," + e1.getMessage());
		} catch (Exception e2) {
			quartzSchedule.updateStatus(job.getId(), 2);
			logger.error(e2.getMessage());
		}
	}

}
