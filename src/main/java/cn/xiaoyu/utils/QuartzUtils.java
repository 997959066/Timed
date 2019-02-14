package cn.xiaoyu.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.http.client.methods.HttpGet;

import com.alibaba.fastjson.JSONObject;

import cn.xiaoyu.entity.ScheduleJob;

public class QuartzUtils {

	// 主工作需要保持名称唯一
	protected static List<String> jobNames = new ArrayList<>();

	private QuartzUtils() {
		throw new IllegalStateException("Utility class");
	}

	// 循环队列取出目标工作对象
	public static String getScheduleJobName(String jobName) {
		if (jobNames.contains(jobName)) { // 如果队列中有id，说明其子任务需要执行
			return jobName;
		}

		return null;
	}

	// 判断job时间是否过期
	public static boolean isExpired(Date jobEndDate) {
		Calendar now = Calendar.getInstance();
		Date currentDate = now.getTime();

		if (currentDate.getTime() > jobEndDate.getTime()) {
			return true;
		}
		return false;
	}

	// 拼接任务名称，放在实体引起分页错误
	public static String jobNameId(ScheduleJob job) {
		StringBuilder sb = new StringBuilder();
		sb.append(job.getId().toString()).append("-").append(job.getJobName());
		return sb.toString();
	}

	// String转date
	public static Date stringDate(String string) throws ParseException {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(string);
	}
	//get url的拼接
	public static String getUrl(String jobUrl, JSONObject jobParam) {
		StringBuffer sb = new StringBuffer();
		sb.append(jobUrl);
		if (null != jobParam) {
			sb.append("?");
			for (String key : jobParam.keySet()) {
				sb.append(key);
				sb.append("=");
				sb.append(jobParam.getString(key));
				sb.append("&");
			}
		}
		String str = sb.toString();
		str = str.substring(0, str.length() - 1);
		return str;
	}
	// 添加头部信息
	public static HttpGet returnHttpGet(HttpGet get,JSONObject header){
		if (null != header) {
			for (String key : header.keySet()) {
				get.addHeader(key, header.getString(key)); // 添加头信息
			}
		}
		return get;
	} 
	
	
}
