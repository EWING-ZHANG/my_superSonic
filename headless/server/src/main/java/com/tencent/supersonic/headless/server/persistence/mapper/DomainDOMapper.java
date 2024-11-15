package com.tencent.supersonic.headless.server.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tencent.supersonic.headless.server.persistence.dataobject.DomainDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DomainDOMapper extends BaseMapper<DomainDO> {
    List<DomainDO> selectAdminOrg(Long id);

    List<DomainDO> selectViewer(Long id);

    void batchUpdateAdmin(List<DomainDO> adminOrgList);

    void batchUpdateViewer(List<DomainDO> viewerOrgList);

    List<DomainDO> selectViewUser(Long userId);

    List<DomainDO> selectAdminUser(Long userId);

    void batchUpdateAdminUser(List<DomainDO> adminUserList);

    void batchUpdateViewerUser(List<DomainDO> viewerUserList);
}
