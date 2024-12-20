package com.tencent.supersonic.chat.server.service;

import com.tencent.supersonic.chat.server.agent.Agent;
import com.tencent.supersonic.common.pojo.User;

import java.util.List;

public interface AgentService {

    List<Agent> getAgents();

    Agent createAgent(Agent agent, User user);

    Agent updateAgent(Agent agent, User user);

    Agent getAgent(Integer id);

    void deleteAgent(Integer id);

    void setAgentAuth(Agent agent);

    void deleteAgentAuth(Long id);

    void deleteAgentOrgAndSub(Long id);
    void deleteAgentORg(Long id);
}
