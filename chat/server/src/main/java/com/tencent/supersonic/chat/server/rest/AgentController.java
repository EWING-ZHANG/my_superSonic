package com.tencent.supersonic.chat.server.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.tencent.supersonic.auth.api.authentication.utils.UserHolder;
import com.tencent.supersonic.auth.authentication.persistence.dataobject.UserDepartmentDO;
import com.tencent.supersonic.auth.authentication.repository.UserDepartmentRepository;
import com.tencent.supersonic.chat.server.agent.Agent;
import com.tencent.supersonic.chat.server.agent.AgentToolType;
import com.tencent.supersonic.chat.server.service.AgentService;
import com.tencent.supersonic.common.config.SystemConfig;
import com.tencent.supersonic.common.pojo.User;
import com.tencent.supersonic.common.service.SystemConfigService;
import com.tencent.supersonic.common.util.StringUtil;
import com.tencent.supersonic.headless.api.pojo.response.DomainResp;
import com.tencent.supersonic.headless.server.persistence.dataobject.DomainDO;
import com.tencent.supersonic.headless.server.service.DomainService;
import io.jsonwebtoken.lang.Collections;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/api/chat/agent", "/openapi/chat/agent"})
public class AgentController {

    @Autowired
    private AgentService agentService;
    @Autowired
    private UserDepartmentRepository userDepartmentRepository;
    @Autowired
    private SystemConfigService systemConfigService;
    @Autowired
    private DomainService domainService;

    @PostMapping
    public Agent createAgent(@RequestBody Agent agent, HttpServletRequest httpServletRequest,
                             HttpServletResponse httpServletResponse) {
        User user = UserHolder.findUser(httpServletRequest, httpServletResponse);
        //将agent中的string[] 转字符串 分隔符 ,
        Agent res = convertTOString(agent);

        return agentService.createAgent(res, user);
    }

    @PutMapping
    public Agent updateAgent(@RequestBody Agent agent, HttpServletRequest httpServletRequest,
                             HttpServletResponse httpServletResponse) {
        User user = UserHolder.findUser(httpServletRequest, httpServletResponse);
        //将agent中的string[] 转字符串 分隔符 ,
        Agent res = convertTOString(agent);


        return agentService.updateAgent(res, user);
    }

    //将agent对象中的admins的string[] 转字符串setAdmins 分隔符 ,
    public Agent convertTOString(Agent agent) {

        if (agent.getAdmins() != null) {
            String delimiter = ",";
            String admins = String.join(delimiter, agent.getAdmins());
            agent.setAdmin(admins);
        }

        if (agent.getViewers() != null) {
            String delimiter = ",";
            String viewers = String.join(delimiter, agent.getViewers());
            agent.setViewer(viewers);
        }

        if (agent.getViewOrgs() != null) {
            String delimiter = ",";
            String viewOrgs = String.join(delimiter, agent.getViewOrgs());
            agent.setViewOrg(viewOrgs);  // 假设 setViewOrgs 用于更新 viewOrgs 字段
        }

        if (agent.getAdminOrgs() != null) {
            String delimiter = ",";
            String adminOrgs = String.join(delimiter, agent.getAdminOrgs());
            agent.setAdminOrg(adminOrgs);  // 假设 setAdminOrgs 用于更新 adminOrgs 字段
        }
        return agent;

    }

    @DeleteMapping("/{id}")
    public boolean deleteAgent(@PathVariable("id") Integer id) {
        agentService.deleteAgent(id);
        return true;
    }

    @GetMapping("/{id}")
    public boolean getAgent(@PathVariable("id") Integer id) {
        agentService.getAgent(id);
        return true;
    }

    /**
     * agent表设置admin  admin_org viewer view_org
     * 后端根据agent表和user_department表来给两个list：admin viewer 。
     * 前端根据整个进行具体展示能否有编辑和删除按钮 *
     *
     * @return
     */

    @RequestMapping("/getAgentList")
    public List<Agent> getAgentList(HttpServletRequest httpServletRequest,
                                    HttpServletResponse httpServletResponse) {
        // 获取当前用户
        User user = UserHolder.findUser(httpServletRequest, httpServletResponse);

        List<Agent> viewerAgents = new ArrayList<>();
        List<Agent> adminAgents = new ArrayList<>();

        // 获取所有代理
        List<Agent> agents = agentService.getAgents();
        if (user.getIsAdmin().equals(1)) {
            agents.forEach(
                    agent -> agent.setAdminAuth(true)
            );

        }
        //agent中创建者和当前用户名字一样 也需要将agent加入到adminAgents
        List<Agent> collect = agents.stream().filter(agent -> agent.getCreatedBy().equals(user.getName())).collect(Collectors.toList());
        adminAgents.addAll(collect);


        // 获取用户部门信息
        List<UserDepartmentDO> userWithDepartment = userDepartmentRepository.getUserWithDepartment();

        // 获取当前用户的部门ID，默认值为 null
        String orgId;
        if (user != null && user.getId() != null) {
            orgId = userWithDepartment.stream()
                    .filter(userDepartmentDO -> user.getId().equals(userDepartmentDO.getUserId()))
                    .findFirst()
                    .map(userDepartmentDO -> userDepartmentDO.getDepartmentId() != null ? userDepartmentDO.getDepartmentId().toString() : null)
                    .orElse(null);
        } else {
            orgId = null;
        }

        // 过滤出属于当前用户部门的 Admin 类型代理
        List<Agent> orgAdminAgents = new ArrayList<>();
        if (orgId != null) {
            orgAdminAgents = agents.stream()
                    .filter(agent -> agent.getAdmin() != null && Arrays.asList(agent.getAdminOrg().split(",")).contains(orgId))
                    .collect(Collectors.toList());
        }

        // 过滤出属于当前用户部门的 Viewer 类型代理
        List<Agent> orgViewerAgents = new ArrayList<>();
        if (orgId != null) {
            orgViewerAgents = agents.stream()
                    .filter(agent -> agent.getViewer() != null && Arrays.asList(agent.getViewOrg().split(",")).contains(orgId))
                    .collect(Collectors.toList());
        }

        // 过滤出用户作为 Admin 的代理
        List<Agent> userAdminAgents = agents.stream()
                .filter(agent -> agent.getAdmin() != null && Arrays.asList(agent.getAdmin().split(",")).contains(user.getId().toString()))
                .collect(Collectors.toList());

        // 过滤出用户作为 Viewer 的代理
        List<Agent> userViewerAgents = agents.stream()
                .filter(agent -> agent.getViewer() != null && Arrays.asList(agent.getViewer().split(",")).contains(user.getId().toString()))
                .collect(Collectors.toList());

        // 合并管理员代理列表
        if (orgId != null) {
            adminAgents.addAll(orgAdminAgents);
        }
        adminAgents.addAll(userAdminAgents);

        // 合并查看者代理列表
        viewerAgents.addAll(userViewerAgents);
        if (orgId != null) {
            viewerAgents.addAll(orgViewerAgents);
        }
        //只读的进行去重和设置adminAuth为false
        viewerAgents = viewerAgents.stream().distinct().collect(Collectors.toList());
        adminAgents = adminAgents.stream().distinct().collect(Collectors.toList());

        // todo 合并admin权限和只读权限 如果有重复则保留一个并且设置adminAuth为true 对于来自viewerAgents 设置adminAuth为false adminAgents设置adminAuth为true
        adminAgents.forEach(agent -> agent.setAdminAuth(true));
        List<Agent> finalAdminAgents = adminAgents;
        viewerAgents.forEach(agent -> {
            if (!finalAdminAgents.contains(agent)) {
                agent.setAdminAuth(false);
                finalAdminAgents.add(agent);
            }
        });
        finalAdminAgents.forEach(agent -> {
            String[] adminsS = agent.getAdmin() != null ? agent.getAdmin().split(",") : new String[0];
            agent.setAdmins(adminsS);

            String[] views = agent.getViewer() != null ? agent.getViewer().split(",") : new String[0];
            agent.setViewers(views);

            String[] viewOrgs = agent.getViewOrg() != null ? agent.getViewOrg().split(",") : new String[0];
            agent.setViewOrgs(viewOrgs);

            String[] adminOrgs = agent.getAdminOrg() != null ? agent.getAdminOrg().split(",") : new String[0];
            agent.setAdminOrgs(adminOrgs);
        });

        return finalAdminAgents;
    }

    @RequestMapping("/getToolTypes")
    public Map<AgentToolType, String> getToolTypes() {
        return AgentToolType.getToolTypes();
    }

    /**
     * 权限控制相关的接口
     * 根据id修改权限 四个字段
     */
    @PostMapping("/setAgentAuth")
    public void updateAgent(@RequestBody Agent agent) {
        //需要将当前的也添加进入 完整的数据
        agentService.setAgentAuth(agent);
    }
    //通过agentId获取到当前的viewOrgs admins viewers  adminOrgs

    /**
     * 根据id获取agent信息
     *
     * @param id
     * @return
     */
    @GetMapping("/getAgent/{id}")
    public Agent getAgentById(@PathVariable("id") Integer id) {
        Agent agent = agentService.getAgent(id);
        //将ageng的admin viewer viewOrgs adminOrgs设置到agent中
        return convertStringTOArray(agent);

    }

    public Agent convertStringTOArray(Agent agent) {
        if (StringUtils.isNotEmpty(agent.getAdmin())) {
            agent.setAdmins(agent.getAdmin().split(","));
        }
        if (StringUtils.isNotEmpty(agent.getViewer())) {
            agent.setViewers(agent.getViewer().split(","));
        }
        if (StringUtils.isNotEmpty(agent.getViewOrg())) {
            agent.setViewOrgs(agent.getViewOrg().split(","));
        }
        if (StringUtils.isNotEmpty(agent.getAdminOrg())) {
            agent.setAdminOrgs(agent.getAdminOrg().split(","));
        }
        return agent;
    }

    @GetMapping("/deleteAgentAuth/{id}")
    public void deleteAgentAuth(@PathVariable("id") Long id) {
        SystemConfig systemConfig = systemConfigService.getSystemConfig();
        List<String> systemAdmin = systemConfig.getAdmins();
        systemAdmin.remove(id.toString());
        systemConfig.setAdmins(systemAdmin);
        systemConfigService.save(systemConfigService.getSystemConfig());

        //查询出该用户有哪些Agent 然后将如果agent中有这个id 则删除 最后更新这个agent
        Agent agent = agentService.getAgent(id.intValue());
        List<Agent> agentList = getAgentList(id.intValue());
        List<Agent> list = agentList;
        list.forEach(temp -> {
            // 处理 Admin 字段
            String updatedAdmin = processIdRemoval(temp.getAdmin(), id.toString());
            temp.setAdmin(updatedAdmin);

            // 处理 Viewer 字段
            String updatedViewer = processIdRemoval(temp.getViewer(), id.toString());
            temp.setViewer(updatedViewer);

            // 更新授权
            agentService.setAgentAuth(temp);
        });



//        if (agent != null) {
//            String admin = agent.getAdmin();
//            admin.replace(id.toString(), "");
//            agent.setAdmin(admin);
//
//            String viewer = agent.getViewer();
//            viewer.replace(id.toString(), "");
//            agent.setViewer(viewer);
//
//            agentService.setAgentAuth(agent);
//        }
        //domain中权限设置的admin和viewer对应的id需要删除
        //查询有那些domain
        //更新domain
        List<DomainResp> domainList = domainService.getDomainList();
        //根据用户id 过滤出来admins和viewers中有这个id的数据
        List<DomainResp> collect = domainList.stream().filter(domain -> domain.getAdmins().contains(id.toString()) || domain.getViewers().contains(id.toString())
        ).collect(Collectors.toList());


        collect.forEach(domain -> {
            //list转string
            domain.getAdmins().remove(id.toString());
            domain.getViewers().remove(id.toString());
            String strAdmin = String.join(",", domain.getAdmins());
            String strViewer = String.join(",", domain.getViewers());
            DomainDO domainDO = new DomainDO();
            BeanUtils.copyProperties(domain, domainDO);
            domainDO.setAdmin(strAdmin);
            domainDO.setViewer(strViewer);
            domainService.setDomainAuth(domainDO);
        });
    }

    public List<Agent> getAgentList(Integer userId) {
        // 获取所有代理
        List<Agent> agents = agentService.getAgents();


        List<Agent> viewerAgents = new ArrayList<>();
        List<Agent> adminAgents = new ArrayList<>();


        // 获取用户部门信息
        List<UserDepartmentDO> userWithDepartment = userDepartmentRepository.getUserWithDepartment();

        // 获取当前用户的部门ID，默认值为 null
        String orgId;
        if (userId != null) {
            orgId = userWithDepartment.stream()
                    .filter(userDepartmentDO -> userId.equals(userDepartmentDO.getUserId()))
                    .findFirst()
                    .map(userDepartmentDO -> userDepartmentDO.getDepartmentId() != null ? userDepartmentDO.getDepartmentId().toString() : null)
                    .orElse(null);
        } else {
            orgId = null;
        }

        // 过滤出属于当前用户部门的 Admin 类型代理
        List<Agent> orgAdminAgents = new ArrayList<>();
        if (orgId != null) {
            orgAdminAgents = agents.stream()
                    .filter(agent -> agent.getAdmin() != null && Arrays.asList(agent.getAdminOrg().split(",")).contains(orgId))
                    .collect(Collectors.toList());
        }

        // 过滤出属于当前用户部门的 Viewer 类型代理
        List<Agent> orgViewerAgents = new ArrayList<>();
        if (orgId != null) {
            orgViewerAgents = agents.stream()
                    .filter(agent -> agent.getViewer() != null && Arrays.asList(agent.getViewOrg().split(",")).contains(orgId))
                    .collect(Collectors.toList());
        }

        // 过滤出用户作为 Admin 的代理
        List<Agent> userAdminAgents = agents.stream()
                .filter(agent -> agent.getAdmin() != null && Arrays.asList(agent.getAdmin().split(",")).contains(userId.toString()))
                .collect(Collectors.toList());

        // 过滤出用户作为 Viewer 的代理
        List<Agent> userViewerAgents = agents.stream()
                .filter(agent -> agent.getViewer() != null && Arrays.asList(agent.getViewer().split(",")).contains(userId.toString()))
                .collect(Collectors.toList());

        // 合并管理员代理列表
        if (orgId != null) {
            adminAgents.addAll(orgAdminAgents);
        }
        adminAgents.addAll(userAdminAgents);

        // 合并查看者代理列表
        viewerAgents.addAll(userViewerAgents);
        if (orgId != null) {
            viewerAgents.addAll(orgViewerAgents);
        }
        //只读的进行去重和设置adminAuth为false
        viewerAgents = viewerAgents.stream().distinct().collect(Collectors.toList());
        adminAgents = adminAgents.stream().distinct().collect(Collectors.toList());

        // todo 合并admin权限和只读权限 如果有重复则保留一个并且设置adminAuth为true 对于来自viewerAgents 设置adminAuth为false adminAgents设置adminAuth为true
        adminAgents.forEach(agent -> agent.setAdminAuth(true));
        List<Agent> finalAdminAgents = adminAgents;
        viewerAgents.forEach(agent -> {
            if (!finalAdminAgents.contains(agent)) {
                agent.setAdminAuth(false);
                finalAdminAgents.add(agent);
            }
        });
        finalAdminAgents.forEach(agent -> {
            String[] adminsS = agent.getAdmin() != null ? agent.getAdmin().split(",") : new String[0];
            agent.setAdmins(adminsS);

            String[] views = agent.getViewer() != null ? agent.getViewer().split(",") : new String[0];
            agent.setViewers(views);

            String[] viewOrgs = agent.getViewOrg() != null ? agent.getViewOrg().split(",") : new String[0];
            agent.setViewOrgs(viewOrgs);

            String[] adminOrgs = agent.getAdminOrg() != null ? agent.getAdminOrg().split(",") : new String[0];
            agent.setAdminOrgs(adminOrgs);
        });

        return finalAdminAgents;
    }

    private String processIdRemoval(String original, String idToRemove) {
        if (original == null || original.isEmpty()) {
            return original; // 如果字符串为空，直接返回
        }

        // 分割字符串为数组
        String[] parts = original.split(",");

        // 移除匹配的 ID，并去掉空白部分
        List<String> updatedParts = Arrays.stream(parts)
                .map(String::trim) // 去掉空格
                .filter(part -> !part.equals(idToRemove)) // 过滤掉匹配的 ID
                .collect(Collectors.toList());

        // 重新拼接字符串
        return String.join(",", updatedParts);
    }


}
