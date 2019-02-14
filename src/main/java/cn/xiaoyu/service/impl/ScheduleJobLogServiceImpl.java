package cn.xiaoyu.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;

import cn.xiaoyu.dao.ScheduleJobLogMapper;
import cn.xiaoyu.entity.PageBean;
import cn.xiaoyu.entity.ScheduleJobLog;
import cn.xiaoyu.service.ScheduleJobLogService;


@Service
public class ScheduleJobLogServiceImpl implements ScheduleJobLogService{
    
    @Autowired
    private ScheduleJobLogMapper scheduleJobLogMapper;
    
    @Override
    public int append(ScheduleJobLog scheduleJobLog){
        return scheduleJobLogMapper.append(scheduleJobLog);
    }

	@Override
	public PageBean<ScheduleJobLog> list(Integer jobId, int pageNo, int pageSize) {
		PageBean<ScheduleJobLog> pageData = new PageBean<ScheduleJobLog>(pageNo, pageSize, scheduleJobLogMapper.list(jobId).size());
		PageHelper.startPage(pageNo, pageSize);
		List<ScheduleJobLog> result = scheduleJobLogMapper.list(jobId);
		pageData.setList(result);
		return pageData;
	}

    
}
