package cn.xiaoyu.timed;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.alibaba.fastjson.JSON;

import cn.xiaoyu.entity.ScheduleJob;
import cn.xiaoyu.entity.ScheduleJobLog;
import cn.xiaoyu.entity.WatchJobList;
import cn.xiaoyu.service.impl.QuartzScheduleServiceImpl;
import cn.xiaoyu.service.impl.ScheduleJobLogServiceImpl;
import cn.xiaoyu.utils.QuartzUtils;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ApplicationTests {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private QuartzScheduleServiceImpl sJob;
    @Autowired
    private ScheduleJobLogServiceImpl sJobLog;
    
    private String url = "/job";
	
    @SuppressWarnings("unchecked")
    private String doRequest(String method, Object param,String type) throws Exception{
        String reqUrl =url+method;
        MvcResult mvcResult = null;
        String p = "";
        if(param != null){
            HashMap<String, Object> pa = JSON.parseObject(JSON.toJSONString(param), HashMap.class);
            if(pa != null && !pa.isEmpty()){
                for(Map.Entry<String, Object> entry : pa.entrySet()){
                    p=p+"&"+entry.getKey()+"="+entry.getValue();
                }
            }
        }
        if("get".equals(type)){
            mvcResult = mvc.perform(MockMvcRequestBuilders.get(reqUrl+p)
                      .accept(MediaType.APPLICATION_JSON)) // 断言返回结果是json
                      .andReturn();// 得到返回结果
        }
        if("post".equals(type)){
            mvcResult = mvc.perform(MockMvcRequestBuilders.post(reqUrl+p)
                    .accept(MediaType.APPLICATION_JSON)) // 断言返回结果是json
                    .andReturn();// 得到返回结果
        }
          
          MockHttpServletResponse response = mvcResult.getResponse();
          //拿到请求返回码
          int status = response.getStatus();
          //拿到结果
          String contentAsString = response.getContentAsString();
      
          System.err.println(status);
          System.err.println(contentAsString);
          
          return contentAsString;
    }
	
	@Test
    public void testGetJobList() {
        Map<String, String> param = new HashMap<>();
        //param.put("shopId", "10593");

        String method = "/list";
        String get;
        try {
            get = doRequest(method,param,"get");
        
            System.out.println(get);
            // add additional test code here
            @SuppressWarnings("unchecked")
            List<WatchJobList> list = (List<WatchJobList>) JSON.parseObject(get,WatchJobList.class);
            if (null != list && !list.isEmpty()) {
                for(WatchJobList j : list) {
                    System.out.println(j.getJobName());
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
	
	@Test
    public void testDeleteJob()  {
        Map<String, String> param = new HashMap<>();
        param.put("jobId", "100");
        param.put("jobGroup", "default");
        String method = "/delete";
        String get;
        try {
            get = doRequest(method,param,"get");
            System.out.println(get);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
	
	@Test
    public void getAllJob() throws Exception {
        List<ScheduleJob> lst = sJob.selectJob();
        if (null != lst && !lst.isEmpty()) {
            for(ScheduleJob sj : lst) {
                System.out.println(jobNameId(sj));
            }
        }
    }
	public String jobNameId(ScheduleJob job){
		 StringBuilder sb = new StringBuilder();
	     sb.append(job.getId().toString()).append("-").append(job.getJobName());
	     return sb.toString();
	}
	
	@Test
    public void updateStatus() throws Exception {
        int i = sJob.updateStatus(1, 1);
        System.out.println(i);
    }
	@Test
    public void updateExecStatus() throws Exception {
        int i = sJob.updateExecStatus(1, 1);
        System.out.println(i);
    }
	
	 
	@Test
    public void getQuartzUtil() throws Exception {
	    System.out.println(QuartzUtils.getScheduleJobName("test"));
	    System.out.println(QuartzUtils.isExpired(new Date()));
	    
        
    }
	
	@Test
    public void appendJobLog() throws Exception {
	    ScheduleJobLog sjl = new ScheduleJobLog();
	    sjl.setJobId(1);
	    sjl.setExecMsg("test");
	    sjl.setExecStatus(1);
	    sjl.setExecUrl("test");
	    sjl.setOptStatus(1);
        int i = sJobLog.append(sjl);
        System.out.println(i);
    }
	
}
