package cn.xiaoyu.entity;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

 
@Getter
@Setter
public class WatchJobList  implements Serializable{

    private static final long serialVersionUID = 1L;

    //任务ID
    private Integer id;
    //任务名称
    private String jobName;
    //任务分组
    private String jobGroup;
    //任务参数
    private String jobArguments;
    //失败后回调url
    private String failCallbackUrl;
    //失败后重试次数
    private Integer retryCount;
    
}
