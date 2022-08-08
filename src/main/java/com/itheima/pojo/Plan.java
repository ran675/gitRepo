package com.itheima.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("t_plan")
public class Plan implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String srcRepo;
    private String remark;

    private Integer start;
    private Integer end;
    private String executed = "N";
    private String time;


}
