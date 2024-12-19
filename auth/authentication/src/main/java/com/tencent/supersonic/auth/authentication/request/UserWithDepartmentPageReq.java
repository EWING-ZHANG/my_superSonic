package com.tencent.supersonic.auth.authentication.request;

import lombok.Data;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import java.util.Set;

@Data
public class UserWithDepartmentPageReq {

    // 确保用户名不能为空
    private String userName;

    // 确保部门名称不能为空
    private String departmentName;

    // 设置默认值，并确保页码不能为空
    private Integer pageNum = 1;  // 默认值为 1

    // 设置默认值，并确保页大小不能为空
    private Integer pageSize = 10; // 默认值为 10

    private Set<Long> departmentIds;
}

