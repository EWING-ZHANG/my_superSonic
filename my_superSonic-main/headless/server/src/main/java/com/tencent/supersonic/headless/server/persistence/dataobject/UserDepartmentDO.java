package com.tencent.supersonic.headless.server.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("s2_user_department")
public class UserDepartmentDO {
    private Long id;
    private Long userId;
    private Long departmentId;
    private String userName;
    private String departmentName;

}
