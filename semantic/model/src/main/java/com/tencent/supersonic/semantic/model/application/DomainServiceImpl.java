package com.tencent.supersonic.semantic.model.application;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.tencent.supersonic.auth.api.authentication.pojo.User;
import com.tencent.supersonic.auth.api.authentication.service.UserService;
import com.tencent.supersonic.common.util.BeanMapper;
import com.tencent.supersonic.common.util.JsonUtil;
import com.tencent.supersonic.semantic.api.model.request.DomainReq;
import com.tencent.supersonic.semantic.api.model.request.DomainSchemaFilterReq;
import com.tencent.supersonic.semantic.api.model.request.DomainUpdateReq;
import com.tencent.supersonic.semantic.api.model.response.DatasourceResp;
import com.tencent.supersonic.semantic.api.model.response.DimSchemaResp;
import com.tencent.supersonic.semantic.api.model.response.DimensionResp;
import com.tencent.supersonic.semantic.api.model.response.DomainResp;
import com.tencent.supersonic.semantic.api.model.response.DomainSchemaResp;
import com.tencent.supersonic.semantic.api.model.response.MetricResp;
import com.tencent.supersonic.semantic.api.model.response.MetricSchemaResp;
import com.tencent.supersonic.semantic.model.domain.DatasourceService;
import com.tencent.supersonic.semantic.model.domain.DimensionService;
import com.tencent.supersonic.semantic.model.domain.DomainService;
import com.tencent.supersonic.semantic.model.domain.MetricService;
import com.tencent.supersonic.semantic.model.domain.dataobject.DomainDO;
import com.tencent.supersonic.semantic.model.domain.pojo.Domain;
import com.tencent.supersonic.semantic.model.domain.repository.DomainRepository;
import com.tencent.supersonic.semantic.model.domain.utils.DomainConvert;

import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;


@Service
@Slf4j
public class DomainServiceImpl implements DomainService {

    private final DomainRepository domainRepository;
    private final MetricService metricService;
    private final DimensionService dimensionService;
    private final DatasourceService datasourceService;
    private final UserService userService;


    public DomainServiceImpl(DomainRepository domainRepository, @Lazy MetricService metricService,
                             @Lazy DimensionService dimensionService, @Lazy DatasourceService datasourceService,
                             UserService userService) {
        this.domainRepository = domainRepository;
        this.metricService = metricService;
        this.dimensionService = dimensionService;
        this.datasourceService = datasourceService;
        this.userService = userService;
    }


    @Override
    public void createDomain(DomainReq domainReq, User user) {
        log.info("[create domain] cmd : {}", JSONObject.toJSONString(domainReq));
        Domain domain = DomainConvert.convert(domainReq);
        log.info("[create domain] object:{}", JSONObject.toJSONString(domainReq));

        saveDomain(domain, user);
    }


    @Override
    public void updateDomain(DomainUpdateReq domainUpdateReq, User user) {
        DomainDO domainDO = getDomainDO(domainUpdateReq.getId());
        domainDO.setUpdatedAt(new Date());
        domainDO.setUpdatedBy(user.getName());
        BeanMapper.mapper(domainUpdateReq, domainDO);
        domainDO.setAdmin(String.join(",", domainUpdateReq.getAdmins()));
        domainDO.setAdminOrg(String.join(",", domainUpdateReq.getAdminOrgs()));
        domainDO.setViewer(String.join(",", domainUpdateReq.getViewers()));
        domainDO.setViewOrg(String.join(",", domainUpdateReq.getViewOrgs()));
        domainDO.setEntity(JsonUtil.toString(domainUpdateReq.getEntity()));
        domainRepository.updateDomain(domainDO);
    }


    @Override

    public void deleteDomain(Long id) {
        checkDelete(id);
        domainRepository.deleteDomain(id);
    }

    private void checkDelete(Long id) {
        List<MetricResp> metricResps = metricService.getMetrics(id);
        List<DimensionResp> dimensionResps = dimensionService.getDimensions(id);
        List<DatasourceResp> datasourceResps = datasourceService.getDatasourceList(id);
        if (!CollectionUtils.isEmpty(metricResps) || !CollectionUtils.isEmpty(datasourceResps)
                || !CollectionUtils.isEmpty(dimensionResps)) {
            throw new RuntimeException("exist datasource, dimension or metric in this domain, please check");
        }
    }

    @Override
    public String getDomainBizName(Long id) {
        if (id == null) {
            return "";
        }
        DomainDO domainDO = getDomainDO(id);
        if (domainDO == null) {
            String message = String.format("domain with id:%s not exist", id);
            throw new RuntimeException(message);
        }
        return domainDO.getBizName();
    }


    @Override
    public List<DomainResp> getDomainList() {
        return convertList(domainRepository.getDomainList(), new HashMap<>(), new HashMap<>());
    }


    @Override
    public List<DomainResp> getDomainList(List<Long> domainIds) {
        return getDomainList().stream()
                .filter(domainDO -> domainIds.contains(domainDO.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<DomainResp> getDomainListForAdmin(String userName) {
        List<DomainDO> domainDOS = domainRepository.getDomainList();
        Set<String> orgIds = Sets.newHashSet();
        log.info("orgIds:{},userName:{}", orgIds, userName);
        Map<Long, List<MetricResp>> metricDomainMap = metricService.getMetrics().stream()
                .collect(Collectors.groupingBy(MetricResp::getDomainId));
        Map<Long, List<DimensionResp>> dimensionDomainMap = dimensionService.getDimensions().stream()
                .collect(Collectors.groupingBy(DimensionResp::getDomainId));
        return convertList(domainDOS, metricDomainMap, dimensionDomainMap).stream()
                .filter(domainDesc -> checkAdminPermission(orgIds, userName, domainDesc))
                .collect(Collectors.toList());
    }

    @Override
    public List<DomainResp> getDomainListForViewer(String userName) {
        List<DomainDO> domainDOS = domainRepository.getDomainList();
        Set<String> orgIds = Sets.newHashSet();
        log.info("orgIds:{},userName:{}", orgIds, userName);
        return convertList(domainDOS, new HashMap<>(), new HashMap<>()).stream()
                .filter(domainDesc -> checkViewerPermission(orgIds, userName, domainDesc))
                .collect(Collectors.toList());
    }


    @Override
    public DomainResp getDomain(Long id) {
        Map<Long, String> fullDomainPathMap = getDomainFullPathMap();
        return DomainConvert.convert(getDomainDO(id), fullDomainPathMap);
    }


    @Override
    public String getDomainFullPath(Long domainId) {
        if (domainId == null) {
            return "";
        }
        Map<Long, String> map = getDomainFullPathMap();
        return map.getOrDefault(domainId, "");
    }

    @Override
    public Map<Long, String> getDomainFullPath() {
        return getDomainFullPathMap();
    }

    //保存并获取自增ID
    private void saveDomain(Domain domain, User user) {
        DomainDO domainDO = DomainConvert.convert(domain, user);
        domainRepository.createDomain(domainDO);
        domain.setId(domainDO.getId());
    }


    private List<DomainResp> convertList(List<DomainDO> domainDOS, Map<Long, List<MetricResp>> metricDomainMap,
                                         Map<Long, List<DimensionResp>> dimensionDomainMap) {
        List<DomainResp> domainDescs = Lists.newArrayList();
        if (CollectionUtils.isEmpty(domainDOS)) {
            return domainDescs;
        }
        Map<Long, String> fullDomainPathMap = getDomainFullPath();

        return domainDOS.stream()
                .map(domainDO -> DomainConvert.convert(domainDO, fullDomainPathMap, dimensionDomainMap,
                        metricDomainMap))
                .collect(Collectors.toList());
    }


    @Override
    public Map<Long, DomainResp> getDomainMap() {
        return getDomainList().stream().collect(Collectors.toMap(DomainResp::getId, a -> a, (k1, k2) -> k1));
    }

    @Override
    public Set<DomainResp> getDomainChildren(List<Long> domainIds) {
        Set<DomainResp> childDomains = new HashSet<>();
        if (CollectionUtils.isEmpty(domainIds)) {
            return childDomains;
        }
        Map<Long, DomainResp> allDomainMap = getDomainMap();
        for (Long domainId : domainIds) {
            DomainResp domain = allDomainMap.get(domainId);
            if (domain != null) {
                childDomains.add(domain);
                Queue<DomainResp> queue = new LinkedList<>();
                queue.add(domain);
                while (!queue.isEmpty()) {
                    DomainResp currentDomain = queue.poll();
                    for (DomainResp child : allDomainMap.values()) {
                        if (Objects.equals(child.getParentId(), currentDomain.getId())) {
                            childDomains.add(child);
                            queue.add(child);
                        }
                    }
                }
            }
        }
        return childDomains;
    }

    public Map<Long, String> getDomainFullPathMap() {
        Map<Long, String> domainFullPathMap = new HashMap<>();
        List<DomainDO> domainDOList = domainRepository.getDomainList();
        Map<Long, DomainDO> domainDOMap = domainDOList.stream()
                .collect(Collectors.toMap(DomainDO::getId, a -> a, (k1, k2) -> k1));
        for (DomainDO domainDO : domainDOList) {
            final Long domainId = domainDO.getId();
            StringBuilder fullPath = new StringBuilder(domainDO.getBizName() + "/");
            Long parentId = domainDO.getParentId();
            while (parentId != null && parentId > 0) {
                domainDO = domainDOMap.get(parentId);
                if (domainDO == null) {
                    String message = String.format("get domain : %s failed", parentId);
                    throw new RuntimeException(message);
                }
                fullPath.insert(0, domainDO.getBizName() + "/");
                parentId = domainDO.getParentId();
            }
            domainFullPathMap.put(domainId, fullPath.toString());
        }
        return domainFullPathMap;
    }

    public List<DomainSchemaResp> fetchDomainSchema(DomainSchemaFilterReq filter, User user) {
        List<DomainSchemaResp> domainSchemaDescList = new ArrayList<>();
        List<Long> domainIdsReq = generateDomainIdsReq(filter);
        List<DomainResp> getDomainListByIds = getDomainList(domainIdsReq);
        getDomainListByIds.stream().forEach(domainDesc -> {
            domainSchemaDescList.add(fetchSingleDomainSchema(domainDesc));
        });
        return domainSchemaDescList;
    }


    protected DomainDO getDomainDO(Long id) {
        return domainRepository.getDomainById(id);
    }


    private DomainSchemaResp fetchSingleDomainSchema(DomainResp domainDesc) {
        Long domainId = domainDesc.getId();
        DomainSchemaResp domainSchemaDesc = new DomainSchemaResp();
        BeanUtils.copyProperties(domainDesc, domainSchemaDesc);

        domainSchemaDesc.setDimensions(generateDimSchema(domainId));
        domainSchemaDesc.setMetrics(generateMetricSchema(domainId));
        return domainSchemaDesc;
    }

    private List<MetricSchemaResp> generateMetricSchema(Long domainId) {
        List<MetricSchemaResp> metricSchemaDescList = new ArrayList<>();
        List<MetricResp> metricDescList = metricService.getMetrics(domainId);
        metricDescList.stream().forEach(metricDesc -> {
                    MetricSchemaResp metricSchemaDesc = new MetricSchemaResp();
                    BeanUtils.copyProperties(metricDesc, metricSchemaDesc);
                    metricSchemaDesc.setUseCnt(0L);
                    metricSchemaDescList.add(metricSchemaDesc);
                }
        );
        return metricSchemaDescList;

    }

    private List<DimSchemaResp> generateDimSchema(Long domainId) {
        List<DimSchemaResp> dimSchemaDescList = new ArrayList<>();
        List<DimensionResp> dimDescList = dimensionService.getDimensions(domainId);
        dimDescList.stream().forEach(dimDesc -> {
                    DimSchemaResp dimSchemaDesc = new DimSchemaResp();
                    BeanUtils.copyProperties(dimDesc, dimSchemaDesc);
                    dimSchemaDesc.setUseCnt(0L);
                    dimSchemaDescList.add(dimSchemaDesc);
                }
        );
        return dimSchemaDescList;
    }

    private List<Long> generateDomainIdsReq(DomainSchemaFilterReq filter) {
        if (Objects.nonNull(filter) && !CollectionUtils.isEmpty(filter.getDomainIds())) {
            return filter.getDomainIds();
        }
        return new ArrayList<>(getDomainMap().keySet());
    }


    private boolean checkAdminPermission(Set<String> orgIds, String userName, DomainResp domainDesc) {

        List<String> admins = domainDesc.getAdmins();
        List<String> adminOrgs = domainDesc.getAdminOrgs();
        if (admins.contains(userName) || domainDesc.getCreatedBy().equals(userName)) {
            return true;
        }
        if (CollectionUtils.isEmpty(adminOrgs)) {
            return false;
        }
        for (String orgId : orgIds) {
            if (adminOrgs.contains(orgId)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkViewerPermission(Set<String> orgIds, String userName, DomainResp domainDesc) {
        if (domainDesc.getIsOpen() == 1) {
            return true;
        }
        List<String> admins = domainDesc.getAdmins();
        List<String> viewers = domainDesc.getViewers();
        List<String> adminOrgs = domainDesc.getAdminOrgs();
        List<String> viewOrgs = domainDesc.getViewOrgs();
        if (admins.contains(userName) || viewers.contains(userName) || domainDesc.getCreatedBy().equals(userName)) {
            return true;
        }
        if (CollectionUtils.isEmpty(adminOrgs) && CollectionUtils.isEmpty(viewOrgs)) {
            return false;
        }
        for (String orgId : orgIds) {
            if (adminOrgs.contains(orgId)) {
                return true;
            }
        }
        for (String orgId : orgIds) {
            if (viewOrgs.contains(orgId)) {
                return true;
            }
        }
        return false;
    }
}
