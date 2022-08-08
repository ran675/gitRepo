package com.itheima.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("t_record")
public class Record implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer repoId;
    private Integer planId;
    private String name;
    private String version;
    private String srcRepo;
    private String srcCommit;

    private String directory;
    private String git;
    private String url;
    private String comments;
    private String time;
    private String privacy;
    /*
      `id` INT(11) NOT NULL AUTO_INCREMENT,
  `repoId` INT(11) NOT NULL,
  `name` VARCHAR(80) DEFAULT NULL,
  `version` VARCHAR(50) DEFAULT NULL,
  `srcRepo` VARCHAR(80) DEFAULT NULL,
  `srcCommit` VARCHAR(42) DEFAULT NULL,
  `directory` VARCHAR(80) DEFAULT NULL,
  `url` VARCHAR(100) DEFAULT NULL,
  `comments` VARCHAR(100) DEFAULT NULL,
  `time` DATETIME DEFAULT '2021-03-05 00:00:00',
            `private` VARCHAR(2) DEFAULT NULL,
*/

}

