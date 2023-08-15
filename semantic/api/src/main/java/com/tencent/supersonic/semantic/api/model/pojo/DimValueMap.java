package com.tencent.supersonic.semantic.api.model.pojo;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * @author: kanedai
 * @date: 2023/7/24
 */

@Data
public class DimValueMap {

    /**
     * dimension value in db
     */
    private String techName;

    /**
     * dimension value for result show
     */
    private String bizName;

    /**
     * dimension value for user query
     */
    private List<String> alias = new ArrayList<>();
}