package cn.xiaoyu.schedule;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cn.xiaoyu.entity.ScheduleJobLog;
import cn.xiaoyu.service.QuartzScheduleService;
import cn.xiaoyu.service.ScheduleJobLogService;
import cn.xiaoyu.utils.QuartzUtils;

@Component
public class HttpClient {

	static final Logger logger = LoggerFactory.getLogger("httpClient");

	@Autowired
	private QuartzScheduleService quartzScheduleService;

	@Autowired
	private ScheduleJobLogService jobLogService;

	private static HttpClient httpClient;

	@PostConstruct
	public void init() {
		httpClient = this;
		httpClient.quartzScheduleService = this.quartzScheduleService;
		httpClient.jobLogService = this.jobLogService;
	}

	protected enum ExecStatus {
		NORMAL(1), FAIL(2), ABNORMAL(3);

		private final int code;

		private ExecStatus(int code) {
			this.code = code;
		}

		public Integer code() {
			return code;
		}
	}

	// 指定post方法
	public void post(String jobUrl, String jobParam, String jobHeader, String callback, Integer retryCount,
			Integer jobId) {
		try {
			String retryDesc = "不需要重试，";
			JSONObject header=jobHeader !=null&& !jobHeader.equals("") ? JSONObject.parseObject(jobHeader):null;
			JSONObject body=jobParam !=null&& !jobParam.equals("") ? JSONObject.parseObject(jobParam):null;
			if (!sendRequestPost(jobUrl, header, body)) {
				boolean breturn = false;
				// 如果retryCount>0，则需要重试，
				// 重试失败后，判断callback，如果callback不为空，则进行调用
				if (retryCount != null && retryCount > 0) {
					retryDesc = "已重试，";
					while (retryCount > 0) {
						// 暂停100毫秒
						Thread.sleep(100);
						breturn = sendRequestPost(jobUrl, header, body);
						if (breturn) { // 如果成功
							// 改状态-异常
							httpClient.quartzScheduleService.updateExecStatus(jobId, ExecStatus.ABNORMAL.code());
							// 记录日志
							jobLogAppend(jobUrl, "重试执行成功", jobId, ExecStatus.ABNORMAL.code());

							break;
						}
						retryCount--;
					}
				}
				if (!breturn) { // 如果没有重试或重试后还是失败
					String callDesc = "，不需要执行回调";
					// 如果需要回调，则调用回调
					if (callback != null && callback.length() > 10) {
						// 调用回调
						sendRequestPost(callback, header, body);
						callDesc = "，已执行回调";
					}
					// 改状态-错误
					httpClient.quartzScheduleService.updateExecStatus(jobId, ExecStatus.FAIL.code());
					// 记录日志
					jobLogAppend(jobUrl, retryDesc + "执行失败" + callDesc, jobId, ExecStatus.FAIL.code());
				}
			} else {
				// 记录日志-正确
				jobLogAppend(jobUrl, "执行成功", jobId, ExecStatus.NORMAL.code());
			}
		} catch (Exception e) {
			if (logger.isErrorEnabled()) {
				logger.error("处理出错( {} ), error:{}", jobUrl, e.getMessage());
			}
		}
	}

	public static HttpPost returnHttpPost(HttpPost post,JSONObject header){
		// 添加头部信息
		if (null != header) {
			for (String key : header.keySet()) {
				post.addHeader(key, header.getString(key)); // 添加头信息
			}
		}
		return post;
	}
	/**
	 * send request with the URL
	 * 
	 * @param url
	 * @return
	 */
	private static boolean sendRequestPost(String url, JSONObject header, JSONObject body) {
		String errDesc = "处理出错( {} ), error:{}";
		boolean reVal = false;
		JSONObject response = null;
		HttpPost post = new HttpPost(url);
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			StringEntity s = new StringEntity(body.toString(), "UTF-8");
			s.setContentEncoding("UTF-8");
			// 发送json数据需要设置contentType
			s.setContentType("application/json;charset=UTF-8");
			post.setEntity(s);
			// 添加头部信息
			post=returnHttpPost(post, header);
			String code = "";
			String result = "";
			CloseableHttpResponse res = null;// Http响应
			res = client.execute(post);
			if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String results = EntityUtils.toString(res.getEntity());// 返回json格式：
				response = JSONObject.parseObject(results);
			}
			if (null != response) {
				code = response.getString("code");
				if ("0".equals(code)) {
					if (logger.isInfoEnabled()) {
						logger.info("处理完成：( {} ),result:{}", url, result);
					}
					reVal = true;
				} else {
					if (logger.isErrorEnabled()) {
						logger.error(errDesc, url, result);
					}
				}
			} else {
				if (logger.isErrorEnabled()) {
					logger.error("处理出错( {} ), error: 无返回对象。", url);
				}
			}
		} catch (ClientProtocolException cpe) {
			if (logger.isErrorEnabled()) {
				logger.error(errDesc, url, cpe.getMessage());
			}
		} catch (IOException ioe) {
			if (logger.isErrorEnabled()) {
				logger.error(errDesc, url, ioe.getMessage());
			}
		} catch (Exception e) {
			if (logger.isErrorEnabled()) {
				logger.error(errDesc, url, e.getMessage());
			}
		}
		return reVal;
	}

	private static void jobLogAppend(String url, String msg, Integer jobId, Integer execStatus) {
		ScheduleJobLog jobLog = new ScheduleJobLog();
		jobLog.setExecUrl(url);
		jobLog.setJobId(jobId);
		jobLog.setExecMsg(msg);
		jobLog.setExecStatus(execStatus);
		httpClient.jobLogService.append(jobLog);
	}
	
	public void get(String jobUrl, String jobParam, String jobHeader, String callback, Integer retryCount,
			Integer jobId) {
		    
			try {
				String retryDesc = "不需要重试，";
				JSONObject header=jobHeader !=null&& !jobHeader.equals("") ? JSONObject.parseObject(jobHeader):null;
				JSONObject param=jobParam !=null&& !jobParam.equals("") ? JSONObject.parseObject(jobParam):null;
				if (!sendRequestGet(jobUrl,header,param)) {
				    boolean breturn = false;
					// 如果retryCount>0，则需要重试，
				    // 重试失败后，判断callback，如果callback不为空，则进行调用
					if (retryCount != null && retryCount > 0){
					    retryDesc = "已重试，";
					    while (retryCount > 0){
					        //暂停100毫秒
					        Thread.sleep(100);
					        
					        breturn = sendRequestGet(jobUrl,header,param);
					        if (breturn){ //如果成功
					            //改状态-异常
					            httpClient.quartzScheduleService.updateExecStatus(jobId, ExecStatus.ABNORMAL.code());
			                    //记录日志
					            jobLogAppend(jobUrl,"重试执行成功",jobId,ExecStatus.ABNORMAL.code());
					            
					            break;
					        }
					        retryCount --;
					    }
					}
					
					if (!breturn){ //如果没有重试或重试后还是失败
	                    String callDesc = "，不需要执行回调";
	                    //如果需要回调，则调用回调
	                    if (callback != null && callback.length() > 10){
	                        //调用回调
	                    	sendRequestGet(callback,header,param);
	                        callDesc = "，已执行回调";
	                    }
	                    //改状态-错误
	                    httpClient.quartzScheduleService.updateExecStatus(jobId, ExecStatus.FAIL.code());
	                    //记录日志
	                    jobLogAppend(jobUrl,retryDesc + "执行失败"+callDesc,jobId,ExecStatus.FAIL.code());
	                }
				}else{
				    //记录日志-正确
				    jobLogAppend(jobUrl,"执行成功",jobId,ExecStatus.NORMAL.code());
				}
			} catch (Exception e) {
			    if (logger.isErrorEnabled()) {
					logger.error("处理出错( {} ), error:{}", jobUrl, e.getMessage());
				}
			}
		}
	
		private static boolean sendRequestGet(String jobUrl,JSONObject header,JSONObject param) {
		    String errDesc = "处理出错( {} ), error:{}";
			boolean reVal = false;
			String url=QuartzUtils.getUrl(jobUrl, param);
			HttpGet httpRequst = new HttpGet(url);
			try (CloseableHttpResponse response = HttpClients.createDefault().execute(httpRequst)){
	
				RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(60000).setConnectTimeout(60000)
						.build();// 设置请求和传输超时时间
				httpRequst.setConfig(requestConfig);
				httpRequst=QuartzUtils.returnHttpGet(httpRequst, header);
				String code = "";
				String result = "";
				
				HttpEntity entity = response.getEntity();
				if (null != entity) {
					result = EntityUtils.toString(entity);
					JSONObject object = (JSONObject) JSON.parse(result);
					code = object.getString("code");
					if ("0".equals(code)) {
						if (logger.isInfoEnabled()) {
							logger.info("处理完成：( {} ),result:{}",url, result);
						}
						
						reVal = true;
					} else {
						if (logger.isErrorEnabled()) {
							logger.error(errDesc, url, result);
						}
					}
				} else {
					if (logger.isErrorEnabled()) {
						logger.error("处理出错( {} ), error: 无返回对象。",url);
					}
				}
			} catch (ClientProtocolException cpe) {
				if (logger.isErrorEnabled()) {
					logger.error(errDesc, url, cpe.getMessage());
				}
			} catch (IOException ioe) {
				if (logger.isErrorEnabled()) {
					logger.error(errDesc, url, ioe.getMessage());
				}
			} catch (Exception e) {
				if (logger.isErrorEnabled()) {
					logger.error(errDesc, url, e.getMessage());
				}
			}
			return reVal;
		}
}
