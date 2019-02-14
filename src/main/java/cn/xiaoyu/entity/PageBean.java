package cn.xiaoyu.entity;

/**
 * xiaoyu
 */

import java.util.List;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class PageBean<T> {
    // 当前页
    private Integer pageNo = 1;
    // 每页显示的总条数
    private Integer pageSize = 10;
    // 总条数
    private Integer rowCount;
    // 是否有下一页
    private Integer isMore;
    // 总页数
    private Integer pageCount;
    // 开始索引
    private Integer startIndex;
    // 分页结果
    private List<T> list;

    public PageBean() {
        super();
    }
    public PageBean(Integer pageNo, Integer pageSize, Integer rowCount) {
        super();
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.rowCount = rowCount;
        this.pageCount = (this.rowCount+this.pageSize-1)/this.pageSize;
        this.startIndex = (this.pageNo-1)*this.pageSize;
        this.isMore = this.pageNo >= this.pageCount?0:1;
    }
}