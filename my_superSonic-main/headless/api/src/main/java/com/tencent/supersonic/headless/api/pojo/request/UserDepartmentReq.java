package com.tencent.supersonic.headless.api.pojo.request;

import com.tencent.supersonic.common.pojo.RecordInfo;
import lombok.Data;
import org.apache.coyote.RequestInfo;

@Data
public class UserDepartmentReq extends RecordInfo {
    private Long userId;
    private Long departmentId;
    private String userName;
    private String departmentName;
}
