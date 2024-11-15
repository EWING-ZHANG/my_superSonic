package com.tencent.supersonic.auth.authentication.request;

import com.tencent.supersonic.common.pojo.RecordInfo;
import lombok.Data;

@Data
public class UserDepartmentReq extends RecordInfo {
    private Long userId;
    private Long departmentId;
    private String userName;
    private String departmentName;
}
