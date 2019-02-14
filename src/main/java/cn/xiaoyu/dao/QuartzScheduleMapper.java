package cn.xiaoyu.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import cn.xiaoyu.entity.ScheduleJob;


 
@Mapper
public interface QuartzScheduleMapper{
	//查询开始时间前五分钟后五分钟范围
    public List<ScheduleJob> selectJob();
    //更新任务状态
    public int updateStatus(@Param("id") Integer id,@Param("status") Integer status);
    //更新执行状态
    public int updateExecStatus(@Param("id") Integer id,@Param("execStatus") Integer execStatus);
    
    
    
    
	//增加定时任务
    public int append(ScheduleJob scheduleJob);
    //删除任务
    public int delete(@Param("id") Integer id);
    //更新任务
    public int update(ScheduleJob scheduleJob);
    //根据条件查询任务
    List<ScheduleJob> list(ScheduleJob scheduleJob);
    
}
