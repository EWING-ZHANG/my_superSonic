package com.tencent.supersonic.headless.api.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class EntityInfo implements Serializable {

    private DataSetInfo dataSetInfo = new DataSetInfo();
    private List<DataInfo> dimensions = new ArrayList<>();
    private List<DataInfo> metrics = new ArrayList<>();
    private String entityId;
}
