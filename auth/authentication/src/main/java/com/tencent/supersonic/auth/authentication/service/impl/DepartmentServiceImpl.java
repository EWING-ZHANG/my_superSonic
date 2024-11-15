package com.tencent.supersonic.auth.authentication.service.impl;

import ch.qos.logback.core.joran.util.beans.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.Update;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tencent.supersonic.auth.authentication.persistence.dataobject.DepartmentDO;
import com.tencent.supersonic.auth.authentication.persistence.dataobject.UserDepartmentDO;
import com.tencent.supersonic.auth.authentication.persistence.mapper.DepartmentMapper;
import com.tencent.supersonic.auth.authentication.persistence.mapper.UserDepartmentMapper;
import com.tencent.supersonic.auth.authentication.pojo.Organization;
import com.tencent.supersonic.auth.authentication.pojo.OrganizationTreeBuilder;
import com.tencent.supersonic.auth.authentication.request.DepartmentReq;
import com.tencent.supersonic.auth.authentication.service.DepartmentService;
import com.tencent.supersonic.common.util.BeanMapper;
import com.tencent.supersonic.headless.server.persistence.dataobject.DomainDO;
import com.tencent.supersonic.headless.server.persistence.mapper.DomainDOMapper;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, DepartmentDO>
        implements DepartmentService {
    @Autowired
    private UserDepartmentMapper userDepartmentMapper;
    @Autowired
    private DomainDOMapper domainDOMapper;

    @Override
    public Boolean SaveOrUpdate(DepartmentReq departmentReq) {
        QueryWrapper<DepartmentDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DepartmentDO::getId, departmentReq.getId());
        DepartmentDO departmentDO = getOne(queryWrapper);
        BeanMapper.mapper(departmentReq, departmentDO);
        return saveOrUpdate(departmentDO);

    }

    @Override
    public List<DepartmentDO> getDepartmentList() {
        return baseMapper.selectList(null);
    }

    @Override
    public List<Organization> getOrganizationTree() {
        List<DepartmentDO> list = baseMapper.selectList(null);
        // 得到部门树结构
        List<Organization> organizationTree = OrganizationTreeBuilder.buildTree(list);
        return organizationTree;
        // 打印或查看树形结构
    }

    @Override
    public DepartmentDO getById(Long departmentId) {
        return baseMapper.selectById(departmentId);
    }

    @Override
    public void addDepartment(DepartmentReq req)
            throws InvocationTargetException, IllegalAccessException {
        DepartmentDO departmentDO = new DepartmentDO();
        BeanUtils.copyProperties(departmentDO, req);
        baseMapper.insert(departmentDO);
    }

    @Transactional
    @Override
    public void deleteDepartmentById(Long id) {
        // 删除department表中的这条数据
        baseMapper.deleteById(id);
        // userDepartment 删除所有departmentId是这个id的所有记录
        QueryWrapper<UserDepartmentDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserDepartmentDO::getDepartmentId, id);
        userDepartmentMapper.delete(queryWrapper);
        // domain中的所有的权限绑定关系解除 domain域里面的对应关系使用一个String来弄的??? 那么会导致不能够感觉String字段里面进行查询
        //模块之间的这种调用,只能够模块依赖了
        //查询出admin的 将这个部门从这个字符串中删除
        List<DomainDO> adminOrgList = domainDOMapper.selectAdminOrg(id);
        //循环遍历 进行删除
        String aim = id.toString();
        adminOrgList.forEach(admin -> {
            String stringAdmin = removeAim(aim, admin);
            admin.setAdminOrg(stringAdmin);
        });
        List<DomainDO> viewerOrgList = domainDOMapper.selectViewer(id);
        viewerOrgList.forEach(viewer -> {
            String stringViewer = removeAim(aim, viewer);
            viewer.setViewOrg(stringViewer);
        });
        //只能批量更新
        if (!CollectionUtils.isEmpty(adminOrgList)) {
            domainDOMapper.batchUpdateAdmin(adminOrgList);
        }
        if (!CollectionUtils.isEmpty(viewerOrgList)) {
            domainDOMapper.batchUpdateViewer(viewerOrgList);
        }

    }

    @Override
    //todo 使用updateById 在一个事务里面应该是不会提交?
    public void unbindUser(Long id) {
        List<DomainDO> adminList = domainDOMapper.selectAdminUser(id);
        String aim = id.toString();
        adminList.forEach(admin -> {
            String stringAdmin = removeAim(aim, admin);
            admin.setAdmin(stringAdmin);
        });
        List<DomainDO> viewerList = domainDOMapper.selectViewUser(id);
        viewerList.forEach(viewer -> {
            String stringViewer = removeAim(aim, viewer);
            viewer.setViewer(stringViewer);
        });
        if (!CollectionUtils.isEmpty(adminList)) {
            domainDOMapper.batchUpdateAdminUser(adminList);
        }
        if (!CollectionUtils.isEmpty(viewerList)) {
            domainDOMapper.batchUpdateViewerUser(viewerList);
        }

    }

    @Transactional
    @Override
    public void deleteDepartmentAndSubById(Long id) {
        //查询出部门的数据然后进行遍历
        List<DepartmentDO> list = getDepartmentList();
        //递归地查询出数据的
        Set<Long> allChildrenIds = getAllChildrenIds(id, list);
        allChildrenIds.forEach(allId ->
                deleteDepartmentById(allId)
        );
    }

    public Set<Long> getAllChildrenIds(Long departmentId, List<DepartmentDO> departments) {
        Set<Long> childrenIds = new HashSet<>();
        findChildrenIds(departmentId, departments, childrenIds);
        return childrenIds;
    }

    private void findChildrenIds(Long departmentId, List<DepartmentDO> departments, Set<Long> childrenIds) {
        // 将当前节点的 id 添加到集合中
        childrenIds.add(departmentId);
        Stack<Long> stack = new Stack<>();
        stack.add(departmentId);
        while (!stack.isEmpty()) {
            // 查找当前节点的子节点
            departmentId = stack.pop();
            Long tempId = departmentId;
            List<DepartmentDO> collect = departments.stream()
                    .filter(dept -> dept.getParentId() ==
                            tempId).collect(Collectors.toList());
            for (int i = 0; i < collect.size(); i++) {
                stack.push(collect.get(i).getId());
                childrenIds.add(collect.get(i).getId());
            }

        }
    }


    /**
     * 删除
     *
     * @param aim
     * @param admin
     * @return
     */
    public String removeAim(String aim, DomainDO admin) {
        String temp = admin.getAdminOrg();
        String[] split = temp.split(",");
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < split.length; i++) {
            if (!split[i].equals(aim)) {
                stringBuilder.append(split[i]);
            }
            if (stringBuilder.length() > 0 && (i != stringBuilder.length() - 1)) {
                stringBuilder.append(",");
            }
        }
        return stringBuilder.toString();

    }
}
