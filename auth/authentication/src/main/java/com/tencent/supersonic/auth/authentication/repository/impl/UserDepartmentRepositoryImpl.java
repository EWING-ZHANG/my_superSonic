package com.tencent.supersonic.auth.authentication.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tencent.supersonic.auth.authentication.persistence.dataobject.UserDepartmentDO;
import com.tencent.supersonic.auth.authentication.persistence.dataobject.UserDepartmentResp;
import com.tencent.supersonic.auth.authentication.persistence.mapper.UserDepartmentMapper;
import com.tencent.supersonic.auth.authentication.repository.UserDepartmentRepository;
import com.tencent.supersonic.auth.authentication.request.UserDepartmentReq;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
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
        return userDepartmentMapper.getUserWithoutDepartment();
    }

    @Override
    public List<UserDepartmentDO> getByUserName(String userName) {
        QueryWrapper<UserDepartmentDO> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(UserDepartmentDO::getUserName, userName);
        return userDepartmentMapper.selectList(wrapper);

    }

    @Override
    public IPage<UserDepartmentResp> getUserWithDepartment(int pageNum, int pageSize, String userName,String departmentName) {
        IPage<UserDepartmentReq> page = new Page<>(pageNum, pageSize);
        // wrapper不支持union 只能用xml实现
        //计算总行数total和总页数pages
        int total = userDepartmentMapper.countUserWithDepartment(userName,departmentName);
        int pages=total%pageSize==0?total/pageSize:total/pageSize+1;
        IPage<UserDepartmentResp> res = userDepartmentMapper.selectPage(page,userName);
        res.setTotal(total);
        res.setPages(pages);
        return res;
    }
    @Override
    public List<UserDepartmentDO> getUserWithDepartment() {
        return userDepartmentMapper.getUserWithDepartment();
    }

    @Override
    public List<UserDepartmentDO> searchByName(String searchName) {
        return userDepartmentMapper.searchByName(searchName);
    }

    @Override
    public void deleteByUserId(Long userId) {
        QueryWrapper<UserDepartmentDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserDepartmentDO::getUserId, userId);
        userDepartmentMapper.delete(queryWrapper);
    }

    @Override
    public List<UserDepartmentDO> getUserListByDepartmentId(Long id) {
        QueryWrapper<UserDepartmentDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserDepartmentDO::getDepartmentId, id);
        return userDepartmentMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public Boolean saveOrUpdateList(List<UserDepartmentDO> userDepartmentDOS) {
        userDepartmentMapper.insertOrUpdate(userDepartmentDOS);
        return true;
    }

    @Override
    @Transactional
    public Boolean saveOrUpdateUserList(List<UserDepartmentDO> userDepartmentDOS) {
        //先根据userid删除所有记录
        deleteByUserId(userDepartmentDOS.get(0).getUserId());
        return saveOrUpdateList(userDepartmentDOS);
    }

    @Override
    public Boolean saveOrUpdateDepartmentList(List<UserDepartmentDO> userDepartmentDOS) {
        //根据部门id删除所有数据
        Long departmentId = userDepartmentDOS.get(0).getDepartmentId();
        QueryWrapper<UserDepartmentDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserDepartmentDO::getDepartmentId, departmentId);
        userDepartmentMapper.delete(queryWrapper);
        return saveOrUpdateList(userDepartmentDOS);
    }

}
