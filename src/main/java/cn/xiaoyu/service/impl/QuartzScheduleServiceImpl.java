package cn.xiaoyu.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;

import cn.xiaoyu.dao.QuartzScheduleMapper;
import cn.xiaoyu.entity.PageBean;
import cn.xiaoyu.entity.ScheduleJob;
import cn.xiaoyu.service.QuartzScheduleService;


@Service
public class QuartzScheduleServiceImpl implements QuartzScheduleService{
    
    @Autowired
    private QuartzScheduleMapper quartzScheduleMapper;
    
    @Override
    public List<ScheduleJob> selectJob(){
        return quartzScheduleMapper.selectJob();
    }
    
    @Override
    public int updateStatus(Integer id,Integer status){
        return quartzScheduleMapper.updateStatus(id, status);
    }
    
    @Override
    public int updateExecStatus(Integer id,Integer execStatus){
        return quartzScheduleMapper.updateExecStatus(id, execStatus);
    }

	@Override
	public PageBean<ScheduleJob> list(ScheduleJob scheduleJob,int pageNo, int pageSize) {
		
		PageBean<ScheduleJob> pageData = new PageBean<ScheduleJob>(pageNo, pageSize, quartzScheduleMapper.list(scheduleJob).size());
		PageHelper.startPage(pageNo, pageSize);
		List<ScheduleJob> result = quartzScheduleMapper.list(scheduleJob);
		pageData.setList(result);
		return pageData;
	}

	@Override
	public int append(ScheduleJob scheduleJob) {
		return quartzScheduleMapper.append(scheduleJob);
	}

	@Override
	public int delete(Integer id) {
		return quartzScheduleMapper.delete(id);
	}

	@Override
	public int update(ScheduleJob scheduleJob) {
		return quartzScheduleMapper.update(scheduleJob);
	}
}
