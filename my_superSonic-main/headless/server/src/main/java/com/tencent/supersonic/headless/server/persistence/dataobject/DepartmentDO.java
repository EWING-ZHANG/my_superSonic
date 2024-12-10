package com.tencent.supersonic.headless.server.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("s2_department")
public class DepartmentDO {
    private Long id;
    private String name;
    private Long parentId;
}
