package cn.xiaoyu.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import cn.xiaoyu.entity.ScheduleJobLog;


@Mapper
public interface ScheduleJobLogMapper{
	//增加日志
    public int append(ScheduleJobLog scheduleJobLog);
    //执行日志查询
    List<ScheduleJobLog>  list(@Param("jobId")Integer jobId);
}
