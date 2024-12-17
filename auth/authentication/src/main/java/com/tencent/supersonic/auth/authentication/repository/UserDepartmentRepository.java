package com.tencent.supersonic.auth.authentication.repository;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tencent.supersonic.auth.authentication.persistence.dataobject.UserDepartmentDO;
import com.tencent.supersonic.auth.authentication.request.DepartmentReq;
import com.tencent.supersonic.auth.authentication.request.UserDepartmentReq;
import com.tencent.supersonic.common.pojo.vo.UserVO;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public interface UserDepartmentRepository {
    void addUserDepartment(UserDepartmentReq userDepartmentReq)
            throws InvocationTargetException, IllegalAccessException;

    List<UserDepartmentDO> getUserWithoutDepartment();


    List<UserDepartmentDO> getByUserName(String userName);

    IPage<UserDepartmentDO> getUserWithDepartment(int pageNum, int pageSize);
    List<UserDepartmentDO> getUserWithDepartment();

    List<UserDepartmentDO> searchByName(String searchName);

    void deleteByUserId(Long userId);

    List<UserDepartmentDO> getUserListByDepartmentId(Long id);

    Boolean saveOrUpdateList(List<UserDepartmentDO> userDepartmentDOS);
}
