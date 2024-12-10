package com.tencent.supersonic.auth.authentication.service;

import com.tencent.supersonic.auth.authentication.persistence.dataobject.DepartmentDO;
import com.tencent.supersonic.auth.authentication.pojo.Organization;
import com.tencent.supersonic.auth.authentication.request.DepartmentReq;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public interface DepartmentService {
    Boolean SaveOrUpdate(DepartmentReq departmentReq);

    List<DepartmentDO> getDepartmentList();

    List<Organization> getOrganizationTree();

    DepartmentDO getById(Long departmentId);

    void addDepartment(DepartmentReq req) throws InvocationTargetException, IllegalAccessException;

    void deleteDepartmentById(Long id);

    void unbindUser(Long id);

    void deleteDepartmentAndSubById(Long id);
}
