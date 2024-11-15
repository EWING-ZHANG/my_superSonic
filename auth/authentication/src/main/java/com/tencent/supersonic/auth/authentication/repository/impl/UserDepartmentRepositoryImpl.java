package com.tencent.supersonic.auth.authentication.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.tencent.supersonic.auth.authentication.persistence.dataobject.UserDepartmentDO;
import com.tencent.supersonic.auth.authentication.persistence.mapper.UserDepartmentMapper;
import com.tencent.supersonic.auth.authentication.repository.UserDepartmentRepository;
import com.tencent.supersonic.auth.authentication.request.UserDepartmentReq;
import io.jsonwebtoken.lang.Collections;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

@Repository
public class UserDepartmentRepositoryImpl implements UserDepartmentRepository {
    private final UserDepartmentMapper userDepartmentMapper;

    public UserDepartmentRepositoryImpl(UserDepartmentMapper userDepartmentMapper) {
        this.userDepartmentMapper = userDepartmentMapper;
    }

    @Override
    public void addUserDepartment(UserDepartmentReq userDepartmentReq)
            throws InvocationTargetException, IllegalAccessException {
        // 新增修改部门信息
        QueryWrapper<UserDepartmentDO> userDepartmentDOQueryWrapper = new QueryWrapper<>();
        userDepartmentDOQueryWrapper.lambda().eq(UserDepartmentDO::getUserId,
                userDepartmentReq.getUserId());
        UserDepartmentDO result = userDepartmentMapper.selectOne(userDepartmentDOQueryWrapper);
        if (ObjectUtils.isEmpty(result)) {
            UserDepartmentDO userDepartmentDO = new UserDepartmentDO();
            BeanUtils.copyProperties(userDepartmentDO, userDepartmentReq);
            userDepartmentMapper.insert(userDepartmentDO);
        } else {
            UpdateWrapper<UserDepartmentDO> updateWrapper = new UpdateWrapper<>();
            updateWrapper.lambda().eq(UserDepartmentDO::getUserId, userDepartmentReq.getUserId())
                    .set(UserDepartmentDO::getDepartmentId, userDepartmentReq.getDepartmentId())
                    .set(UserDepartmentDO::getDepartmentName,
                            userDepartmentReq.getDepartmentName());
            userDepartmentMapper.update(updateWrapper);
        }

    }

    @Override
    public List<UserDepartmentDO> getUserWithoutDepartment() {
        List<UserDepartmentDO> result = userDepartmentMapper.getUserWithoutDepartment();
        return result;
    }

    @Override
    public UserDepartmentDO getByUserName(String userName) {
        QueryWrapper<UserDepartmentDO> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(UserDepartmentDO::getUserName, userName);
        UserDepartmentDO userDepartmentDO = userDepartmentMapper.selectOne(wrapper);
        return userDepartmentDO;

    }

    @Override
    public List<UserDepartmentDO> getUserWithDepartment() {
        List<UserDepartmentDO> result = userDepartmentMapper.getUserWithDepartment();
        return result;
    }

    @Override
    public List<UserDepartmentDO> searchByName(String searchName) {
        return userDepartmentMapper.searchByName(searchName);
    }

    @Override
    public void deleteByUserId(Long userId) {
        QueryWrapper<UserDepartmentDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserDepartmentDO::getUserId,userId);
        userDepartmentMapper.delete(queryWrapper);
    }
}
