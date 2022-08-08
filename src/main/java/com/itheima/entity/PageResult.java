package com.itheima.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PageResult implements Serializable {
    private Long total;//总记录数
    private List rows;//当前页结果

    public PageResult(Long total, List rows) {
        this.total = total;
        this.rows = rows;
    }


}
