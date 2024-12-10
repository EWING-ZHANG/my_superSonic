package com.tencent.supersonic.headless.api.pojo.request;

import lombok.Data;

@Data
public class DepartmentReq {
    private Long id;
    private String name;
    private Long parent;
}
