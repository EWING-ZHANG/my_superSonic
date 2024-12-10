package com.tencent.supersonic.auth.authentication.repository;


import com.tencent.supersonic.auth.authentication.persistence.dataobject.UserDepartmentDO;
import com.tencent.supersonic.auth.authentication.request.UserDepartmentReq;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public interface UserDepartmentRepository {
    void addUserDepartment(UserDepartmentReq userDepartmentReq)
            throws InvocationTargetException, IllegalAccessException;

    List<UserDepartmentDO> getUserWithoutDepartment();


    UserDepartmentDO getByUserName(String userName);

    List<UserDepartmentDO> getUserWithDepartment();

    List<UserDepartmentDO> searchByName(String searchName);

    void deleteByUserId(Long userId);
}
