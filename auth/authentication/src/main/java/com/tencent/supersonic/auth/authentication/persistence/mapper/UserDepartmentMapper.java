package com.tencent.supersonic.auth.authentication.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tencent.supersonic.auth.authentication.persistence.dataobject.UserDepartmentDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserDepartmentMapper extends BaseMapper<UserDepartmentDO> {
    List<UserDepartmentDO> getUserWithoutDepartment();

    List<UserDepartmentDO> getUserWithDepartment();

    List<UserDepartmentDO> searchByName(String searchName);
}
