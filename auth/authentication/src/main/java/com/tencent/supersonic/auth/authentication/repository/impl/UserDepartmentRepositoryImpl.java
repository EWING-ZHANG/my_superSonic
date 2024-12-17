package com.tencent.supersonic.auth.authentication.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tencent.supersonic.auth.authentication.persistence.dataobject.UserDepartmentDO;
import com.tencent.supersonic.auth.authentication.persistence.mapper.UserDepartmentMapper;
import com.tencent.supersonic.auth.authentication.repository.UserDepartmentRepository;
import com.tencent.supersonic.auth.authentication.request.UserDepartmentReq;
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
        return userDepartmentMapper.getUserWithoutDepartment();
    }

    @Override
    public List<UserDepartmentDO> getByUserName(String userName) {
        QueryWrapper<UserDepartmentDO> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(UserDepartmentDO::getUserName, userName);
        return userDepartmentMapper.selectList(wrapper);

    }

    @Override
    public IPage<UserDepartmentDO> getUserWithDepartment(int pageNum, int pageSize) {
        IPage<UserDepartmentDO> page = new Page<>(pageNum, pageSize);
        // wrapper不支持union 只能用xml实现
        //计算总行数total和总页数pages
        int total = userDepartmentMapper.countUserWithDepartment();
        int pages=total%pageSize==0?total/pageSize:total/pageSize+1;
        System.out.println("-------------pages-----------"+pages);
        IPage<UserDepartmentDO> res = userDepartmentMapper.selectPage(page);
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
    public Boolean saveOrUpdateList(List<UserDepartmentDO> userDepartmentDOS) {
        userDepartmentMapper.insertOrUpdate(userDepartmentDOS);
        return true;
    }
}
