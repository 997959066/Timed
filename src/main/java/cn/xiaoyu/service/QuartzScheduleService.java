package cn.xiaoyu.service;

import java.util.List;

import cn.xiaoyu.entity.PageBean;
import cn.xiaoyu.entity.ScheduleJob;

 
public interface QuartzScheduleService{
    
   
   public List<ScheduleJob> selectJob();
   
   public int updateStatus(Integer id,Integer status);
   
   public int updateExecStatus(Integer id,Integer execStatus);
   
	//增加定时任务
   public int append(ScheduleJob scheduleJob);
   //删除任务
   public int delete(Integer id);
   //更新任务
   public int update(ScheduleJob scheduleJob);
   //根据条件查询任务
   PageBean<ScheduleJob> list(ScheduleJob scheduleJob,int pageNo, int pageSize);
}
