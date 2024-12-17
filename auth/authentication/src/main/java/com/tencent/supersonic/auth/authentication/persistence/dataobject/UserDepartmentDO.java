package com.tencent.supersonic.auth.authentication.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("s2_user_department")
public class UserDepartmentDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long departmentId;
    private String userName;
    private String departmentName;

}
