package com.smarthome.common.result;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 总记录数 */
    private long total;

    /** 当前页数据 */
    private List<T> rows;

    public PageResult() {}

    public PageResult(long total, List<T> rows) {
        this.total = total;
        this.rows = rows;
    }
}
