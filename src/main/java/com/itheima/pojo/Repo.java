package com.itheima.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("t_repo")
public class Repo implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String version;
    private String srcRepo;
    private String srcCommit;
    private String srcPath;

    private String requestedAt;
    private String comments;
    private String repoKeyPresent;
    private String existInPre;
    private String prUrl;

//	@Transient
//	private StringBuffer footnote = new StringBuffer();


}
