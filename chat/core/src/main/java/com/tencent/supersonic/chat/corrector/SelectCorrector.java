package com.tencent.supersonic.chat.corrector;

import com.tencent.supersonic.chat.api.pojo.SemanticParseInfo;
import com.tencent.supersonic.chat.api.pojo.request.QueryReq;
import com.tencent.supersonic.common.util.jsqlparser.SqlParserSelectHelper;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

@Slf4j
public class SelectCorrector extends BaseSemanticCorrector {

    @Override
    public void work(QueryReq queryReq, SemanticParseInfo semanticParseInfo) {
        String logicSql = semanticParseInfo.getSqlInfo().getLogicSql();
        List<String> aggregateFields = SqlParserSelectHelper.getAggregateFields(logicSql);
        List<String> selectFields = SqlParserSelectHelper.getSelectFields(logicSql);
        // If the number of aggregated fields is equal to the number of queried fields, do not add fields to select.
        if (!CollectionUtils.isEmpty(aggregateFields)
                && !CollectionUtils.isEmpty(selectFields)
                && aggregateFields.size() == selectFields.size()) {
            return;
        }
        addFieldsToSelect(semanticParseInfo, logicSql);
    }
}
