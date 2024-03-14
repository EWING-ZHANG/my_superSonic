package com.tencent.supersonic.chat.server.plugin.build.webservice;

import com.alibaba.fastjson.JSON;
import com.tencent.supersonic.chat.server.plugin.Plugin;
import com.tencent.supersonic.chat.server.plugin.PluginParseResult;
import com.tencent.supersonic.chat.server.plugin.PluginQueryManager;
import com.tencent.supersonic.chat.server.plugin.build.ParamOption;
import com.tencent.supersonic.chat.server.plugin.build.PluginSemanticQuery;
import com.tencent.supersonic.chat.server.plugin.build.WebBase;
import com.tencent.supersonic.common.pojo.Constants;
import com.tencent.supersonic.common.pojo.QueryColumn;
import com.tencent.supersonic.common.util.ContextUtils;
import com.tencent.supersonic.common.util.JsonUtil;
import com.tencent.supersonic.headless.api.pojo.response.QueryResult;
import com.tencent.supersonic.headless.api.pojo.response.QueryState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Component
public class WebServiceQuery extends PluginSemanticQuery {

    public static String QUERY_MODE = "WEB_SERVICE";

    private RestTemplate restTemplate;

    public WebServiceQuery() {
        PluginQueryManager.register(QUERY_MODE, this);
    }

    @Override
    public QueryResult build() {
        QueryResult queryResult = new QueryResult();
        queryResult.setQueryMode(QUERY_MODE);
        Map<String, Object> properties = parseInfo.getProperties();
        PluginParseResult pluginParseResult = JsonUtil.toObject(
                JsonUtil.toString(properties.get(Constants.CONTEXT)), PluginParseResult.class);
        WebServiceResp webServiceResponse = buildResponse(pluginParseResult);
        Object object = webServiceResponse.getResult();
        // in order to show webServiceQuery result int frontend conveniently,
        // webServiceResponse result format is consistent with queryByStruct result.
        log.info("webServiceResponse result:{}", JsonUtil.toString(object));
        try {
            Map<String, Object> data = JsonUtil.toMap(JsonUtil.toString(object), String.class, Object.class);
            queryResult.setQueryResults((List<Map<String, Object>>) data.get("resultList"));
            queryResult.setQueryColumns((List<QueryColumn>) data.get("columns"));
            queryResult.setQueryState(QueryState.SUCCESS);
        } catch (Exception e) {
            log.info("webServiceResponse result has an exception:{}", e.getMessage());
        }
        return queryResult;
    }

    protected WebServiceResp buildResponse(PluginParseResult pluginParseResult) {
        WebServiceResp webServiceResponse = new WebServiceResp();
        Plugin plugin = pluginParseResult.getPlugin();
        WebBase webBase = fillWebBaseResult(JsonUtil.toObject(plugin.getConfig(), WebBase.class), pluginParseResult);
        webServiceResponse.setWebBase(webBase);
        List<ParamOption> paramOptions = webBase.getParamOptions();
        Map<String, Object> params = new HashMap<>();
        paramOptions.forEach(o -> params.put(o.getKey(), o.getValue()));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(JSON.toJSONString(params), headers);
        URI requestUrl = UriComponentsBuilder.fromHttpUrl(webBase.getUrl()).build().encode().toUri();
        ResponseEntity responseEntity = null;
        Object objectResponse = null;
        restTemplate = ContextUtils.getBean(RestTemplate.class);
        try {
            responseEntity = restTemplate.exchange(requestUrl, HttpMethod.POST, entity, Object.class);
            objectResponse = responseEntity.getBody();
            log.info("objectResponse:{}", objectResponse);
            Map<String, Object> response = JsonUtil.objectToMap(objectResponse);
            webServiceResponse.setResult(response);
        } catch (Exception e) {
            log.info("Exception:{}", e.getMessage());
        }
        return webServiceResponse;
    }

}
