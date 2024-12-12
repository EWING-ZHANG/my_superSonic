package com.tencent.supersonic.chat.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tencent.supersonic.auth.authentication.persistence.dataobject.DepartmentDO;
import com.tencent.supersonic.auth.authentication.persistence.dataobject.UserDepartmentDO;
import com.tencent.supersonic.auth.authentication.repository.UserDepartmentRepository;
import com.tencent.supersonic.auth.authentication.service.DepartmentService;
import com.tencent.supersonic.auth.authentication.utils.ComponentFactory;
import com.tencent.supersonic.chat.api.pojo.request.ChatMemoryFilter;
import com.tencent.supersonic.chat.api.pojo.request.ChatParseReq;
import com.tencent.supersonic.chat.server.agent.Agent;
import com.tencent.supersonic.chat.server.agent.VisualConfig;
import com.tencent.supersonic.chat.server.persistence.dataobject.AgentDO;
import com.tencent.supersonic.chat.server.persistence.dataobject.ChatMemoryDO;
import com.tencent.supersonic.chat.server.persistence.mapper.AgentDOMapper;
import com.tencent.supersonic.chat.server.service.AgentService;
import com.tencent.supersonic.chat.server.service.ChatQueryService;
import com.tencent.supersonic.chat.server.service.MemoryService;
import com.tencent.supersonic.common.config.ChatModel;
import com.tencent.supersonic.common.config.SystemConfig;
import com.tencent.supersonic.common.pojo.ChatApp;
import com.tencent.supersonic.common.pojo.User;
import com.tencent.supersonic.common.service.ChatModelService;
import com.tencent.supersonic.common.service.SystemConfigService;
import com.tencent.supersonic.common.util.JsonUtil;
import com.tencent.supersonic.headless.api.pojo.response.DomainResp;
import com.tencent.supersonic.headless.server.persistence.dataobject.DomainDO;
import com.tencent.supersonic.headless.server.service.DomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import sun.management.resources.agent;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AgentServiceImpl extends ServiceImpl<AgentDOMapper, AgentDO> implements AgentService {

    @Autowired
    private MemoryService memoryService;

    @Autowired
    private ChatQueryService chatQueryService;

    @Autowired
    private ChatModelService chatModelService;

    private ExecutorService executorService = Executors.newFixedThreadPool(1);

    @Autowired
    private SystemConfigService systemConfigService;
    @Autowired
    private AgentService agentService;


    @Autowired
    private DomainService domainService;

    @Autowired
    private UserDepartmentRepository userDepartmentRepository;

    @Autowired
    private DepartmentService departmentService;

    @Override
    public List<Agent> getAgents() {
        return getAgentDOList().stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public Agent createAgent(Agent agent, User user) {
        agent.createdBy(user.getName());
        AgentDO agentDO = convert(agent);
        save(agentDO);
        agent.setId(agentDO.getId());
        executeAgentExamplesAsync(agent);
        return agent;
    }

    @Override
    public Agent updateAgent(Agent agent, User user) {
        agent.updatedBy(user.getName());
        updateById(convert(agent));
        executeAgentExamplesAsync(agent);
        return agent;
    }

    @Override
    public Agent getAgent(Integer id) {
        if (id == null) {
            return null;
        }
        return convert(getById(id));
    }

    @Override
    public void deleteAgent(Integer id) {
        removeById(id);
    }

    @Override
    public void setAgentAuth(Agent agent) {
        AgentDO agentDO = new AgentDO();
        BeanUtils.copyProperties(agent, agentDO);
        updateById(agentDO);

    }

    /**
     *和userId绑定
     * @param  id
     */
    @Override
    @Transactional
    public void deleteAgentAuth(Long id) {
/*        SystemConfig systemConfig = systemConfigService.getSystemConfig();
        List<String> systemAdmin = systemConfig.getAdmins();
        systemAdmin.remove(id.toString());
        systemConfig.setAdmins(systemAdmin);
        systemConfigService.save(systemConfigService.getSystemConfig());*/

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

    @Override
    @Transactional
    public void deleteAgentOrgAndSub(Long id) {
        // 查询出部门的数据然后进行遍历
        List<DepartmentDO> list = departmentService.getDepartmentList();
        // 所有子部门的用户agent权限和所有子部门agent权限
        Set<Long> allChildrenIds = getAllChildrenIds(id, list);
        allChildrenIds.forEach(this::deleteAgentORg);
    }

    @Override
    public void deleteAgentORg(Long id) {
        List<AgentDO> adminOrgList = baseMapper.selectAdminOrg(id);
        //adminOrgList去重
        List<AgentDO> adminOrgs = adminOrgList.stream().distinct().collect(Collectors.toList());
        // 循环遍历 进行删除
        String aim = id.toString();
        adminOrgs.forEach(admin -> {
            String stringAdmin = removeAim(aim, admin.getAdminOrg());
            admin.setAdminOrg(stringAdmin);
        });
        List<AgentDO> viewerOrgList = baseMapper.selectViewOrg(id);
        List<AgentDO> viewerOrgs = viewerOrgList.stream().distinct().collect(Collectors.toList());
        viewerOrgs.forEach(viewer -> {
            String stringViewer = removeAim(aim, viewer.getViewOrg());
            viewer.setViewOrg(stringViewer);
        });
        // 只能批量更新
        if (!CollectionUtils.isEmpty(adminOrgs)) {
            baseMapper.batchUpdateAdminOrg(adminOrgs);
        }
        if (!CollectionUtils.isEmpty(viewerOrgs)) {
            baseMapper.batchUpdateViewOrg(viewerOrgs);
        }
    }


    public Set<Long> getAllChildrenIds(Long departmentId, List<DepartmentDO> departments) {
        Set<Long> childrenIds = new HashSet<>();
        findChildrenIds(departmentId, departments, childrenIds);
        return childrenIds;
    }

    private void findChildrenIds(Long departmentId, List<DepartmentDO> departments,
                                 Set<Long> childrenIds) {
        // 将当前节点的 id 添加到集合中
        childrenIds.add(departmentId);
        Stack<Long> stack = new Stack<>();
        stack.add(departmentId);
        while (!stack.isEmpty()) {
            // 查找当前节点的子节点
            departmentId = stack.pop();
            Long tempId = departmentId;
            List<DepartmentDO> collect = departments.stream()
                    .filter(dept -> dept.getParentId() == tempId).collect(Collectors.toList());
            for (int i = 0; i < collect.size(); i++) {
                stack.push(collect.get(i).getId());
                childrenIds.add(collect.get(i).getId());
            }

        }
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

    /**
     * the example in the agent will be executed by default, if the result is correct, it will be
     * put into memory as a reference for LLM
     *
     * @param agent
     */
    private void executeAgentExamplesAsync(Agent agent) {
        executorService.execute(() -> doExecuteAgentExamples(agent));
    }

    private synchronized void doExecuteAgentExamples(Agent agent) {
        if (!agent.containsDatasetTool() || !agent.enableMemoryReview()
                || CollectionUtils.isEmpty(agent.getExamples())) {
            return;
        }

        List<String> examples = agent.getExamples();
        ChatMemoryFilter chatMemoryFilter =
                ChatMemoryFilter.builder().agentId(agent.getId()).questions(examples).build();
        List<String> memoriesExisted = memoryService.getMemories(chatMemoryFilter).stream()
                .map(ChatMemoryDO::getQuestion).collect(Collectors.toList());
        for (String example : examples) {
            if (memoriesExisted.contains(example)) {
                continue;
            }
            try {
                chatQueryService
                        .parseAndExecute(ChatParseReq.builder().chatId(-1).agentId(agent.getId())
                                .queryText(example).user(User.getDefaultUser()).build());
            } catch (Exception e) {
                log.warn("agent:{} example execute failed:{}", agent.getName(), example);
            }
        }
    }

    private List<AgentDO> getAgentDOList() {
        return list();
    }

    private Agent convert(AgentDO agentDO) {
        if (agentDO == null) {
            return null;
        }
        Agent agent = new Agent();
        BeanUtils.copyProperties(agentDO, agent);
        agent.setToolConfig(agentDO.getToolConfig());
        agent.setExamples(JsonUtil.toList(agentDO.getExamples(), String.class));
        agent.setChatAppConfig(
                JsonUtil.toMap(agentDO.getChatModelConfig(), String.class, ChatApp.class));
        agent.setVisualConfig(JsonUtil.toObject(agentDO.getVisualConfig(), VisualConfig.class));
        agent.getChatAppConfig().values().forEach(c -> {
            ChatModel chatModel = chatModelService.getChatModel(c.getChatModelId());
            if (Objects.nonNull(chatModel)) {
                c.setChatModelConfig(chatModelService.getChatModel(c.getChatModelId()).getConfig());
            }
        });
        return agent;
    }

    private AgentDO convert(Agent agent) {
        AgentDO agentDO = new AgentDO();
        BeanUtils.copyProperties(agent, agentDO);
        agentDO.setToolConfig(agent.getToolConfig());
        agentDO.setExamples(JsonUtil.toString(agent.getExamples()));
        agentDO.setChatModelConfig(JsonUtil.toString(agent.getChatAppConfig()));
        agentDO.setVisualConfig(JsonUtil.toString(agent.getVisualConfig()));
        if (agentDO.getStatus() == null) {
            agentDO.setStatus(1);
        }
        return agentDO;
    }
    public String removeAim( String idToRemove,String original) {
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
