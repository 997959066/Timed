package cn.xiaoyu.log;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by kl on 2017/10/9.
 * Content :日志消息实体，注意，这里为了减少篇幅，省略了get,set代码
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
public class LoggerMessage{

    private String body;
    private String timestamp;
    private String threadName;
    private String className;
    private String level;
    private String exception;
    private String cause;
}