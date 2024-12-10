package com.tencent.supersonic.headless.server.pojo;

import com.tencent.supersonic.headless.server.persistence.dataobject.DepartmentDO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrganizationTreeBuilder {

    public static List<Organization> buildTree(List<DepartmentDO> departmentDOList) {
        // 1. 将 DepartmentDO 转换为 Organization
        Map<Long, Organization> organizationMap = departmentDOList.stream()
                .map(dept -> new Organization(dept.getId(), dept.getParentId(), dept.getName()))
                .collect(Collectors.toMap(org -> Long.valueOf(org.getId()), org -> org));

        // 2. 初始化根节点列表
        List<Organization> rootOrganizations = new ArrayList<>();

        // 3. 构建树形结构
        for (DepartmentDO dept : departmentDOList) {
            Organization currentOrg = organizationMap.get(dept.getId());

            // 如果当前部门有父部门，则将它加入到父部门的 subOrganizations 中
            if (dept.getParentId() != null && dept.getParentId() != 0) {
                Organization parentOrg = organizationMap.get(dept.getParentId());
                if (parentOrg != null) {
                    parentOrg.getSubOrganizations().add(currentOrg);
                }
            } else {
                // 没有父部门的部门即为根节点
                rootOrganizations.add(currentOrg);
            }
        }

        return rootOrganizations;
    }
}
