package com.tencent.supersonic.semantic.api.query.request;

import java.util.Map;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class QueryDslReq {

    private Long modelId;

    private String sql;

    private Map<String, String> variables;

}
