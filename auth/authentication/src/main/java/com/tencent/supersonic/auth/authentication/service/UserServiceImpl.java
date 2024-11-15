package com.tencent.supersonic.auth.authentication.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tencent.supersonic.auth.api.authentication.pojo.Organization;
import com.tencent.supersonic.auth.api.authentication.pojo.UserToken;
import com.tencent.supersonic.auth.api.authentication.request.UserReq;
import com.tencent.supersonic.auth.api.authentication.service.UserService;
import com.tencent.supersonic.auth.api.authentication.utils.UserHolder;
import com.tencent.supersonic.auth.authentication.persistence.dataobject.DepartmentDO;
import com.tencent.supersonic.auth.authentication.persistence.dataobject.UserDepartmentDO;
import com.tencent.supersonic.auth.authentication.repository.UserDepartmentRepository;
import com.tencent.supersonic.auth.authentication.utils.ComponentFactory;
import com.tencent.supersonic.common.config.SystemConfig;
import com.tencent.supersonic.common.pojo.User;
import com.tencent.supersonic.common.service.SystemConfigService;
import io.jsonwebtoken.lang.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private SystemConfigService sysParameterService;
    @Autowired
    private UserDepartmentRepository userDepartmentRepository;
    @Autowired
    private DepartmentService departmentService;

    public UserServiceImpl(SystemConfigService sysParameterService) {
        this.sysParameterService = sysParameterService;
    }

    @Override
    public User getCurrentUser(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) {
        User user = UserHolder.findUser(httpServletRequest, httpServletResponse);
        if (user != null) {
            SystemConfig systemConfig = sysParameterService.getSystemConfig();
            if (!CollectionUtils.isEmpty(systemConfig.getAdmins())
                    && systemConfig.getAdmins().contains(user.getName())) {
                user.setIsAdmin(1);
            }
        }
        return user;
    }

    @Override
    public List<String> getUserNames() {
        return ComponentFactory.getUserAdaptor().getUserNames();
    }

    @Override
    public List<User> getUserList() {
        return ComponentFactory.getUserAdaptor().getUserList();
    }

    @Override
    public Set<String> getUserAllOrgId(String userName) {
        // 根据userName查询所属部门,然后递归的查询部门然后将所有的部门放进去
        HashSet<String> result = new HashSet<>();
        UserDepartmentDO byUserName = userDepartmentRepository.getByUserName(userName);

        // 根据id循环的去查找他的部门
        if (!Objects.isEmpty(byUserName) && byUserName.getDepartmentId() != null) {
            DepartmentDO departmentDO = departmentService.getById(byUserName.getDepartmentId());
            result.add(String.valueOf(departmentDO.getId()));
            while (!ObjectUtils.isEmpty(departmentDO) && departmentDO.getId() != null
                    && departmentDO.getParentId() != 0) {
                departmentDO = departmentService.getById(departmentDO.getParentId());
                if (!ObjectUtils.isEmpty(departmentDO)) {
                    result.add(String.valueOf(departmentDO.getId()));

                }
            }
        }
        return result;
    }

    @Override
    public List<User> getUserByOrg(String key) {
        return ComponentFactory.getUserAdaptor().getUserByOrg(key);
    }

    @Override
    public List<Organization> getOrganizationTree() {
        return ComponentFactory.getUserAdaptor().getOrganizationTree();
    }

    @Override
    public void register(UserReq userReq) {
        ComponentFactory.getUserAdaptor().register(userReq);
    }

    @Override
    public String login(UserReq userReq, HttpServletRequest request) {
        return ComponentFactory.getUserAdaptor().login(userReq, request);
    }

    @Override
    public String login(UserReq userReq, String appKey) {
        return ComponentFactory.getUserAdaptor().login(userReq, appKey);
    }

    @Override
    public String getPassword(String userName) {
        return ComponentFactory.getUserAdaptor().getPassword(userName);
    }

    @Override
    public void resetPassword(String userName, String password, String newPassword) {
        ComponentFactory.getUserAdaptor().resetPassword(userName, password, newPassword);
    }

    @Override
    public UserToken generateToken(String name, String userName, long expireTime) {
        return ComponentFactory.getUserAdaptor().generateToken(name, userName, expireTime);
    }

    @Override
    public List<UserToken> getUserTokens(String userName) {
        return ComponentFactory.getUserAdaptor().getUserTokens(userName);
    }

    @Override
    public UserToken getUserToken(Long id) {
        return ComponentFactory.getUserAdaptor().getUserToken(id);
    }

    @Override
    public void deleteUserToken(Long id) {
        ComponentFactory.getUserAdaptor().deleteUserToken(id);
    }

    @Override
    @Transactional
    public void deleteUserById(Long id) {
        ComponentFactory.getUserAdaptor().deleteUserById(id);
        // 删除在user_department表中的数据
        userDepartmentRepository.deleteByUserId(id);
        // 删除domain表中的viewer和admin 超级管理员应该是不能够删除

        departmentService.unbindUser(id);

    }

}
