package com.tencent.supersonic.auth.authentication.pojo;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Organization {
    private String id;
    private String parentId;
    private String name;
    private String fullName;
    private List<Organization> subOrganizations = Lists.newArrayList();
    private boolean isRoot;

    // 构造函数
    public Organization(Long id, Long parentId, String name) {
        this.id = String.valueOf(id);
        this.parentId = parentId != null ? String.valueOf(parentId) : null;
        this.name = name;
        this.isRoot = parentId == null; // 如果没有父节点，则为根节点
    }
}
