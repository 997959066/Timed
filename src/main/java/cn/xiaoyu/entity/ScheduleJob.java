package cn.xiaoyu.entity;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScheduleJob  implements Serializable{

    private static final long serialVersionUID = 6744479448533033753L;

    //任务ID
    private Integer id;
    //任务名称
    private String jobName;
    //任务分组
    private String jobGroup;
    //任务状态 0禁用 1启用 2删除
    private Integer jobStatus;
    //任务描述
    private String jobDesc;
    //任务url
    private String jobUrl;
    //任务参数
    private String jobParam;
    //此处增加 header 处数据
    private String jobHeader;
    //任务运行时间表达式
    private String cronExpression;
    //是否并发 0禁用 1启用
    private Integer concurrent;
    //任务类
    private String targetObject;
    //任务方法
    private String targetMethod;
    //任务开始时间
    private String beginDate;
    //任务截至时间
    private String expiredDate;

    private String createDate;
    private String updateDate;
    
    //任务状态 1-正常 2-错误 3-异常
    private Integer execStatus;
    //失败后回调url
    private String failCallbackUrl;
    //失败后重试次数
    private Integer retryCount;
    
    //放入schedule中的job名称，防止重复
//    public String getJobNameId(){
//        StringBuilder sb = new StringBuilder();
//        sb.append(id.toString()).append("-").append(jobName);
//        return sb.toString();
//    }
}
