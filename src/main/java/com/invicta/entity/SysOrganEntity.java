package com.invicta.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_organ")
public class SysOrganEntity {
    @TableId(value = "org_id", type = IdType.AUTO)
    private Long orgId;
    private String orgName;
    private String orgEmail;
    private String orgPhone;
    private Integer orgType;
    private Long orgParentId;
    private Integer orgState;
    private LocalDateTime createTime;
    private Boolean isDelete;
}
