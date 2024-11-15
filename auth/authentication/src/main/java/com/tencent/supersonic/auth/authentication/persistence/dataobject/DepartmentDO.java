package com.tencent.supersonic.auth.authentication.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("s2_department")
public class DepartmentDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long parentId;
}
