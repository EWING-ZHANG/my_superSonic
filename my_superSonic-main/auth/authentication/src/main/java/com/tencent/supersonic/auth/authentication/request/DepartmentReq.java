package com.tencent.supersonic.auth.authentication.request;

import lombok.Data;

@Data
public class DepartmentReq {
    private Long id;
    private String name;
    private Long parentId;
}
