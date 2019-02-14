package cn.xiaoyu.entity;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

 
@Getter
@Setter
public class ScheduleJobLog  implements Serializable{
    
    private static final long serialVersionUID = -7398700926813535996L;
    //id
    private Integer id;
    //job id
    private Integer jobId;
    //执行的url
    private String execUrl;
    //执行返回信息
    private String execMsg;
    //log状态 1-正常 2-错误 3-异常
    private Integer execStatus;
    //操作状态 0-未操作,1-已重试,2-忽略
    private Integer optStatus;
    //创建时间
    private String createDate;
    
    private String updateDate;

}
