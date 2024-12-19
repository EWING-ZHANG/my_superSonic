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
import java.util.ArrayList;
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
    public IPage<UserDepartmentResp> getUserWithDepartment(int pageNum, int pageSize, String userName,String departmentName,List<Long> departmentIds) {
        IPage<UserDepartmentReq> page = new Page<>(pageNum, pageSize);
        //计算总行数total和总页数pages
        int total = userDepartmentMapper.countUserWithDepartment(userName,departmentName,departmentIds);
        int pages=total%pageSize==0?total/pageSize:total/pageSize+1;
        int offset = pageSize * (pageNum - 1);
        page.setCurrent(offset);
        IPage<UserDepartmentResp> res = userDepartmentMapper.selectPage(page,userName,departmentIds);
        List<UserDepartmentResp> records = res.getRecords();
        //将departmentIdsStr转为List<Long> departmentIds departmentNamesStr转为List<String> departmentNames
        for (UserDepartmentResp record : records) {
            // 获取字符串字段
            String departmentIdsStr = record.getDepartmentIdsStr();
            String departmentNamesStr = record.getDepartmentNamesStr();

            // 如果 departmentIdsStr 不为空，确保其格式正确并转换为 List<Long>
            List<Long> setDepartmentIds = null;
            if (departmentIdsStr != null && !departmentIdsStr.isEmpty()) {
                // 检查是否包含逗号（多项数据）
                if (departmentIdsStr.contains(",")) {
                    // 如果是逗号分隔的字符串，先包裹成合法的 JSON 数组格式
                    setDepartmentIds = com.alibaba.fastjson.JSON.parseArray("[" + departmentIdsStr + "]", Long.class);
                } else {
                    // 如果只有一个 ID，将其转换为单个元素的 List
                    setDepartmentIds = new ArrayList<>();
                    setDepartmentIds.add(Long.parseLong(departmentIdsStr));
                }
            }

            // 如果 departmentNamesStr 不为空，确保其格式正确并转换为 List<String>
            List<String> departmentNames = null;
            if (departmentNamesStr != null && !departmentNamesStr.isEmpty()) {
                // 检查是否包含逗号（多项数据）
                String[] split = departmentNamesStr.split(",");
                //转成List<String>
                departmentNames = new ArrayList<>();
                for (String s : split) {
                    departmentNames.add(s);
                }
            }

            // 设置转换后的 List 或 null（空值）
            record.setDepartmentIds(setDepartmentIds);
            record.setDepartmentNames(departmentNames);
        }


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
