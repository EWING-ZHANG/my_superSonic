package com.tencent.supersonic.auth.authentication.persistence.dataobject;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Data
@Getter
@Setter
public class UserDepartmentResp {
    private Long id;
    private Long userId;
    private Long departmentId;
    private String userName;
    private String departmentName;
    private String displayName;
    private List<Long> departmentIds;
    private List<String> departmentNames;
    private String departmentNamesStr;
    private String departmentIdsStr;
}
