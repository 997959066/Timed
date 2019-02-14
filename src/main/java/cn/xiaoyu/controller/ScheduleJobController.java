package cn.xiaoyu.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import cn.xiaoyu.entity.PageBean;
import cn.xiaoyu.entity.ScheduleJob;
import cn.xiaoyu.entity.ScheduleJobLog;
import cn.xiaoyu.service.QuartzScheduleService;
import cn.xiaoyu.service.ScheduleJobLogService;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(value = "/scheduleJob", produces = {"application/json;charset=UTF-8"})
public class ScheduleJobController{

    @Autowired
	private QuartzScheduleService scheduleJobService;
    @Autowired
    private ScheduleJobLogService scheduleJobLogService;
    
	@Value("${pim-timed-username}")
	private String username;
	@Value("${pim-timed-password}")
	private String password;
    
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public Boolean  login(String userName, String password) {
		Boolean b=false;
		if(userName.equals(username)){
			if(password.equals(password)){
				b=true;
				SerializerFeature[] featureArr = { SerializerFeature.WriteClassName };  
		        setSseeion("user", JSON.toJSONString(b, featureArr));
			}
		}
		return b;
	}
	public HttpServletRequest getRequest() {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getRequest();
		return request;
	}
	public void setSseeion(String key,String value) {
		 HttpSession session = getRequest().getSession();
		 session.setAttribute(key,value);
	}
    
	@RequestMapping(value = "/listByPage", method = RequestMethod.GET)
	public PageBean<ScheduleJob>  lists(ScheduleJob scheduleJob, int pageNo, int pageSize) {
		return scheduleJobService.list(scheduleJob,pageNo,pageSize);
	}
	
	@RequestMapping(value = "/listByPageLog", method = RequestMethod.GET)
	public PageBean<ScheduleJobLog>  list(Integer jobId, int pageNo, int pageSize) {
		return scheduleJobLogService.list(jobId,pageNo,pageSize);
	}
	
	@ApiOperation(value = "增加任务", notes = "增加任务")
	@RequestMapping(value = "/append", method = RequestMethod.POST)
	public int append(ScheduleJob scheduleJob) {
		scheduleJob.setTargetObject("cn.xiaoyu.schedule.HttpClient");
		return  scheduleJobService.append(scheduleJob);
	}
	
	@ApiOperation(value = "删除任务", notes = "删除任务")
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public int deletes(Integer id) {
		return scheduleJobService.delete(id);
	}
	
	@ApiOperation(value = "修改任务", notes = "修改任务")
	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public int update(ScheduleJob scheduleJob) {
		return scheduleJobService.update(scheduleJob);
	}
}
