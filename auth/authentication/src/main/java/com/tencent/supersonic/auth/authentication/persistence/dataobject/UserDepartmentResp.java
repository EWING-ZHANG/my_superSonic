package com.tencent.supersonic.auth.authentication.persistence.dataobject;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class UserDepartmentResp {
    private Long id;
    private Long userId;
    private Long departmentId;
    private String userName;
    private String departmentName;
    private String displayName;
    private Set<Long> departmentIds;
    private List<String> departmentNames;
    private String departmentNamesStr;
    private String departmentIdsStr;
}
