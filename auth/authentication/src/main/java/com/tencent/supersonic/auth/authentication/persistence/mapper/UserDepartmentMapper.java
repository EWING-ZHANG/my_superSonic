package com.tencent.supersonic.auth.authentication.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tencent.supersonic.auth.authentication.persistence.dataobject.UserDepartmentDO;
import com.tencent.supersonic.auth.authentication.persistence.dataobject.UserDepartmentResp;
import com.tencent.supersonic.auth.authentication.request.UserDepartmentReq;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserDepartmentMapper extends BaseMapper<UserDepartmentDO> {
    List<UserDepartmentDO> getUserWithoutDepartment();

    Integer countUserWithDepartment(@Param("userName")String userName,@Param("departmentName") String departmentName,@Param("departmentIds") List<Long> departmentIds);

    List<UserDepartmentDO> searchByName(String searchName);
    List<UserDepartmentDO> getUserWithDepartment();
    IPage<UserDepartmentResp> selectPage(@Param("page") IPage<UserDepartmentReq> page, @Param("userName")String userName, @Param("departmentIds") List<Long> departmentIds);


    List<UserDepartmentDO> getUserListByDepartmentId(Long id);
}
