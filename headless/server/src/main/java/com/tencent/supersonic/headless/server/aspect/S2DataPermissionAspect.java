package com.tencent.supersonic.headless.server.aspect;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.tencent.supersonic.auth.api.authentication.pojo.User;
import com.tencent.supersonic.auth.api.authorization.response.AuthorizedResourceResp;
import com.tencent.supersonic.common.pojo.Constants;
import com.tencent.supersonic.common.pojo.Filter;
import com.tencent.supersonic.common.pojo.enums.FilterOperatorEnum;
import com.tencent.supersonic.common.pojo.exception.InvalidArgumentException;
import com.tencent.supersonic.common.pojo.exception.InvalidPermissionException;
import com.tencent.supersonic.common.util.jsqlparser.SqlParserAddHelper;
import com.tencent.supersonic.headless.api.pojo.request.QuerySqlReq;
import com.tencent.supersonic.headless.api.pojo.request.QueryStructReq;
import com.tencent.supersonic.headless.api.pojo.request.SchemaFilterReq;
import com.tencent.supersonic.headless.api.pojo.request.SemanticQueryReq;
import com.tencent.supersonic.headless.api.pojo.response.DimensionResp;
import com.tencent.supersonic.headless.api.pojo.response.ModelResp;
import com.tencent.supersonic.headless.api.pojo.response.SemanticQueryResp;
import com.tencent.supersonic.headless.api.pojo.response.SemanticSchemaResp;
import com.tencent.supersonic.headless.server.pojo.MetaFilter;
import com.tencent.supersonic.headless.server.pojo.ModelFilter;
import com.tencent.supersonic.headless.server.service.DimensionService;
import com.tencent.supersonic.headless.server.service.ModelService;
import com.tencent.supersonic.headless.server.service.SchemaService;
import com.tencent.supersonic.headless.server.utils.QueryStructUtils;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import static com.tencent.supersonic.common.pojo.Constants.MINUS;

@Component
@Aspect
@Order(1)
@Slf4j
public class S2DataPermissionAspect extends AuthCheckBaseAspect {

    @Autowired
    private QueryStructUtils queryStructUtils;
    @Autowired
    private DimensionService dimensionService;
    @Autowired
    private ModelService modelService;
    @Value("${permission.data.enable:true}")
    private Boolean permissionDataEnable;
    @Autowired
    private SchemaService schemaService;

    @Pointcut("@annotation(com.tencent.supersonic.headless.server.annotation.S2DataPermission)")
    private void s2PermissionCheck() {
    }

    @Around("s2PermissionCheck()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("s2 permission check!");
        if (!permissionDataEnable) {
            log.info("not to check permission!");
            return joinPoint.proceed();
        }
        Object[] objects = joinPoint.getArgs();
        SemanticQueryReq queryReq = (SemanticQueryReq) objects[0];
        if (!queryReq.isNeedAuth()) {
            log.info("needAuth is false, there is no need to check permissions.");
            return joinPoint.proceed();
        }
        User user = (User) objects[1];
        if (Objects.isNull(user) || Strings.isNullOrEmpty(user.getName())) {
            throw new RuntimeException("please provide user information");
        }

        // determine whether admin of the model
        if (doModelAdmin(user, queryReq.getModelIds())) {
            return joinPoint.proceed();
        }
        // determine whether the subject field is visible
        doModelVisible(user, queryReq.getModelIds());

        if (queryReq instanceof QuerySqlReq) {
            return checkSqlPermission(joinPoint, (QuerySqlReq) queryReq);
        }
        if (queryReq instanceof QueryStructReq) {
            return checkStructPermission(joinPoint, (QueryStructReq) queryReq);
        }
        throw new InvalidArgumentException("queryReq is not Invalid:" + queryReq);
    }

    private Object checkSqlPermission(ProceedingJoinPoint joinPoint, QuerySqlReq querySqlReq)
            throws Throwable {
        Object[] objects = joinPoint.getArgs();
        User user = (User) objects[1];
        // fetch data permission meta information
        SchemaFilterReq filter = new SchemaFilterReq();
        filter.setModelIds(querySqlReq.getModelIds());
        filter.setViewId(querySqlReq.getViewId());
        SemanticSchemaResp semanticSchemaResp = schemaService.fetchSemanticSchema(filter);
        List<Long> modelIdInView = semanticSchemaResp.getModelResps().stream()
                .map(ModelResp::getId).collect(Collectors.toList());
        Set<String> res4Privilege = queryStructUtils.getResNameEnExceptInternalCol(querySqlReq, semanticSchemaResp);
        log.info("modelId:{}, res4Privilege:{}", modelIdInView, res4Privilege);

        Set<String> sensitiveResByModel = getHighSensitiveColsByModelId(semanticSchemaResp);
        Set<String> sensitiveResReq = res4Privilege.parallelStream()
                .filter(sensitiveResByModel::contains).collect(Collectors.toSet());

        // query user privilege info
        AuthorizedResourceResp authorizedResource = getAuthorizedResource(user, modelIdInView, sensitiveResReq);
        // get sensitiveRes that user has privilege
        Set<String> resAuthSet = getAuthResNameSet(authorizedResource, modelIdInView);

        // if sensitive fields without permission are involved in filter, thrown an exception
        doFilterCheckLogic(querySqlReq, resAuthSet, sensitiveResReq);

        // row permission pre-filter
        doRowPermission(querySqlReq, authorizedResource);

        // proceed
        SemanticQueryResp queryResultWithColumns = (SemanticQueryResp) joinPoint.proceed();

        if (CollectionUtils.isEmpty(sensitiveResReq) || allSensitiveResReqIsOk(sensitiveResReq, resAuthSet)) {
            // if sensitiveRes is empty
            log.info("sensitiveResReq is empty");
            return getQueryResultWithColumns(queryResultWithColumns, modelIdInView, authorizedResource);
        }

        // if the column has no permission, hit *
        Set<String> need2Apply = sensitiveResReq.stream().filter(req -> !resAuthSet.contains(req))
                .collect(Collectors.toSet());
        log.info("need2Apply:{},sensitiveResReq:{},resAuthSet:{}", need2Apply, sensitiveResReq, resAuthSet);
        SemanticQueryResp queryResultAfterDesensitization =
                desensitizationData(queryResultWithColumns, need2Apply);
        addPromptInfoInfo(modelIdInView, queryResultAfterDesensitization, authorizedResource, need2Apply);

        return queryResultAfterDesensitization;
    }

    private void doFilterCheckLogic(QuerySqlReq querySqlReq, Set<String> resAuthName,
            Set<String> sensitiveResReq) {
        Set<String> resFilterSet = queryStructUtils.getFilterResNameEnExceptInternalCol(querySqlReq);
        Set<String> need2Apply = resFilterSet.stream()
                .filter(res -> !resAuthName.contains(res) && sensitiveResReq.contains(res)).collect(Collectors.toSet());
        Set<String> nameCnSet = new HashSet<>();

        List<Long> modelIds = Lists.newArrayList(querySqlReq.getModelIds());
        ModelFilter modelFilter = new ModelFilter();
        modelFilter.setModelIds(modelIds);
        List<ModelResp> modelInfos = modelService.getModelList(modelFilter);
        String modelNameCn = Constants.EMPTY;
        if (!CollectionUtils.isEmpty(modelInfos)) {
            modelNameCn = modelInfos.get(0).getName();
        }
        MetaFilter metaFilter = new MetaFilter(modelIds);
        List<DimensionResp> dimensionDescList = dimensionService.getDimensions(metaFilter);
        String finalDomainNameCn = modelNameCn;
        dimensionDescList.stream().filter(dim -> need2Apply.contains(dim.getBizName()))
                .forEach(dim -> nameCnSet.add(finalDomainNameCn + MINUS + dim.getName()));

        if (!CollectionUtils.isEmpty(need2Apply)) {
            ModelResp modelResp = modelInfos.get(0);
            List<String> admins = modelService.getModelAdmin(modelResp.getId());
            log.info("in doFilterLogic, need2Apply:{}", need2Apply);
            String message = String.format("您没有以下维度%s权限, 请联系管理员%s开通", nameCnSet, admins);
            throw new InvalidPermissionException(message);
        }
    }

    private void doFilterCheckLogic(QueryStructReq queryStructReq, Set<String> resAuthName,
            Set<String> sensitiveResReq) {
        Set<String> resFilterSet = queryStructUtils.getFilterResNameEnExceptInternalCol(queryStructReq);
        Set<String> need2Apply = resFilterSet.stream()
                .filter(res -> !resAuthName.contains(res) && sensitiveResReq.contains(res)).collect(Collectors.toSet());
        Set<String> nameCnSet = new HashSet<>();

        Map<Long, ModelResp> modelRespMap = modelService.getModelMap();
        List<Long> modelIds = Lists.newArrayList(queryStructReq.getModelIds());
        List<DimensionResp> dimensionDescList = dimensionService.getDimensions(new MetaFilter(modelIds));
        dimensionDescList.stream().filter(dim -> need2Apply.contains(dim.getBizName()))
                .forEach(dim -> nameCnSet.add(modelRespMap.get(dim.getModelId()).getName() + MINUS + dim.getName()));

        if (!CollectionUtils.isEmpty(need2Apply)) {
            List<String> admins = modelService.getModelAdmin(modelIds.get(0));
            log.info("in doFilterLogic, need2Apply:{}", need2Apply);
            String message = String.format("您没有以下维度%s权限, 请联系管理员%s开通", nameCnSet, admins);
            throw new InvalidPermissionException(message);
        }
    }

    public Object checkStructPermission(ProceedingJoinPoint point, QueryStructReq queryStructReq) throws Throwable {
        Object[] args = point.getArgs();
        User user = (User) args[1];
        // fetch data permission meta information
        SchemaFilterReq filter = new SchemaFilterReq();
        filter.setModelIds(queryStructReq.getModelIds());
        filter.setViewId(queryStructReq.getViewId());
        SemanticSchemaResp semanticSchemaResp = schemaService.fetchSemanticSchema(filter);
        List<Long> modelIdInView = semanticSchemaResp.getModelResps().stream()
                .map(ModelResp::getId).collect(Collectors.toList());
        Set<String> res4Privilege = queryStructUtils.getResNameEnExceptInternalCol(queryStructReq);
        log.info("modelId:{}, res4Privilege:{}", modelIdInView, res4Privilege);

        Set<String> sensitiveResByModel = getHighSensitiveColsByModelId(semanticSchemaResp);
        Set<String> sensitiveResReq = res4Privilege.parallelStream()
                .filter(sensitiveResByModel::contains).collect(Collectors.toSet());
        log.info("this query domainId:{}, sensitiveResReq:{}", modelIdInView, sensitiveResReq);

        // query user privilege info
        AuthorizedResourceResp authorizedResource = getAuthorizedResource(user,
                modelIdInView, sensitiveResReq);
        // get sensitiveRes that user has privilege
        Set<String> resAuthSet = getAuthResNameSet(authorizedResource, queryStructReq.getModelIds());

        // if sensitive fields without permission are involved in filter, thrown an exception
        doFilterCheckLogic(queryStructReq, resAuthSet, sensitiveResReq);

        // row permission pre-filter
        doRowPermission(queryStructReq, authorizedResource);

        // proceed
        SemanticQueryResp queryResultWithColumns = (SemanticQueryResp) point.proceed();

        if (CollectionUtils.isEmpty(sensitiveResReq) || allSensitiveResReqIsOk(sensitiveResReq, resAuthSet)) {
            // if sensitiveRes is empty
            log.info("sensitiveResReq is empty");
            return getQueryResultWithColumns(queryResultWithColumns, modelIdInView, authorizedResource);
        }

        // if the column has no permission, hit *
        Set<String> need2Apply = sensitiveResReq.stream().filter(req -> !resAuthSet.contains(req))
                .collect(Collectors.toSet());
        SemanticQueryResp queryResultAfterDesensitization =
                desensitizationData(queryResultWithColumns, need2Apply);
        addPromptInfoInfo(modelIdInView, queryResultAfterDesensitization, authorizedResource, need2Apply);

        return queryResultAfterDesensitization;

    }

    public boolean allSensitiveResReqIsOk(Set<String> sensitiveResReq, Set<String> resAuthSet) {
        if (resAuthSet.containsAll(sensitiveResReq)) {
            return true;
        }
        log.info("sensitiveResReq:{}, resAuthSet:{}", sensitiveResReq, resAuthSet);
        return false;
    }

    private void doRowPermission(QuerySqlReq querySqlReq, AuthorizedResourceResp authorizedResource) {
        log.debug("start doRowPermission logic");
        StringJoiner joiner = new StringJoiner(" OR ");
        List<String> dimensionFilters = new ArrayList<>();
        if (!CollectionUtils.isEmpty(authorizedResource.getFilters())) {
            authorizedResource.getFilters().stream()
                    .forEach(filter -> dimensionFilters.addAll(filter.getExpressions()));
        }

        if (CollectionUtils.isEmpty(dimensionFilters)) {
            log.debug("dimensionFilters is empty");
            return;
        }

        dimensionFilters.stream().forEach(filter -> {
            if (StringUtils.isNotEmpty(filter) && StringUtils.isNotEmpty(filter.trim())) {
                joiner.add(" ( " + filter + " ) ");
            }
        });
        try {
            Expression expression = CCJSqlParserUtil.parseCondExpression(" ( " + joiner + " ) ");
            if (StringUtils.isNotEmpty(joiner.toString())) {
                String sql = SqlParserAddHelper.addWhere(querySqlReq.getSql(), expression);
                log.info("before doRowPermission, queryS2SQLReq:{}", querySqlReq.getSql());
                querySqlReq.setSql(sql);
                log.info("after doRowPermission, queryS2SQLReq:{}", querySqlReq.getSql());
            }
        } catch (JSQLParserException jsqlParserException) {
            log.info("jsqlParser has an exception:{}", jsqlParserException.toString());
        }

    }

    private void doRowPermission(QueryStructReq queryStructReq, AuthorizedResourceResp authorizedResource) {
        log.debug("start doRowPermission logic");
        StringJoiner joiner = new StringJoiner(" OR ");
        List<String> dimensionFilters = new ArrayList<>();
        if (!CollectionUtils.isEmpty(authorizedResource.getFilters())) {
            authorizedResource.getFilters().stream()
                    .forEach(filter -> dimensionFilters.addAll(filter.getExpressions()));
        }

        if (CollectionUtils.isEmpty(dimensionFilters)) {
            log.debug("dimensionFilters is empty");
            return;
        }

        dimensionFilters.stream().forEach(filter -> {
            if (StringUtils.isNotEmpty(filter) && StringUtils.isNotEmpty(filter.trim())) {
                joiner.add(" ( " + filter + " ) ");
            }
        });

        if (StringUtils.isNotEmpty(joiner.toString())) {
            log.info("before doRowPermission, queryStructReq:{}", queryStructReq);
            Filter filter = new Filter("", FilterOperatorEnum.SQL_PART, joiner.toString());
            List<Filter> filters = Objects.isNull(queryStructReq.getOriginalFilter()) ? new ArrayList<>()
                    : queryStructReq.getOriginalFilter();
            filters.add(filter);
            queryStructReq.setDimensionFilters(filters);
            log.info("after doRowPermission, queryStructReq:{}", queryStructReq);
        }

    }

}
