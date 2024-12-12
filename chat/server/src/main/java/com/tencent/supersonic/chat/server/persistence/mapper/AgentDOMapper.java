package com.tencent.supersonic.chat.server.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tencent.supersonic.chat.server.persistence.dataobject.AgentDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AgentDOMapper extends BaseMapper<AgentDO> {
    List<AgentDO> selectAdminOrg(Long id);

    List<AgentDO> selectViewOrg(Long id);

    void batchUpdateAdminOrg(List<AgentDO> adminOrgs);

    void batchUpdateViewOrg(List<AgentDO> viewerOrgs);
}
