package com.tencent.supersonic.auth.authentication.repository;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tencent.supersonic.auth.authentication.persistence.dataobject.UserDepartmentDO;
import com.tencent.supersonic.auth.authentication.persistence.dataobject.UserDepartmentResp;
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

    IPage<UserDepartmentResp> getUserWithDepartment(int pageNum, int pageSize, String userName, String departmentName,List<Long> departmentIds);
    List<UserDepartmentDO> getUserWithDepartment();

    List<UserDepartmentDO> searchByName(String searchName);

    void deleteByUserId(Long userId);

    List<UserDepartmentResp> getUserListByDepartmentId(Long id);

    Boolean saveOrUpdateList(List<UserDepartmentDO> userDepartmentDOS);

    /**
     *  一个用户下选择多个部门
     * @param userDepartmentDOS
     * @return
     */
    Boolean saveOrUpdateUserList(List<UserDepartmentDO> userDepartmentDOS);
    /**
     * 部门下面添加多个用户
     */

    Boolean saveOrUpdateDepartmentList(List<UserDepartmentDO> userDepartmentDOS);
}
