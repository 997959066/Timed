package cn.xiaoyu.service;

import cn.xiaoyu.entity.PageBean;
import cn.xiaoyu.entity.ScheduleJobLog;

/** 
* @author: lei.li1
* @version: 2017年9月27日 下午5:30:35 
* @description:
*/
public interface ScheduleJobLogService{
    
   public int append(ScheduleJobLog scheduleJobLog);
   
   //执行日志查询
   PageBean<ScheduleJobLog>  list(Integer jobId, int pageNo, int pageSize);
}
