package com.tencent.supersonic.chat.parser.rule;

import com.tencent.supersonic.chat.api.component.SemanticParser;
import com.tencent.supersonic.chat.api.pojo.*;
import com.tencent.supersonic.chat.query.rule.RuleSemanticQuery;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QueryModeParser implements SemanticParser {

    @Override
    public void parse(QueryContext queryContext, ChatContext chatContext) {
        SchemaMapInfo mapInfo = queryContext.getMapInfo();
        // iterate all schemaElementMatches to resolve semantic query
        for (Long modelId : mapInfo.getMatchedModels()) {
            List<SchemaElementMatch> elementMatches = mapInfo.getMatchedElements(modelId);
            List<RuleSemanticQuery> queries = RuleSemanticQuery.resolve(elementMatches, queryContext);
            for (RuleSemanticQuery query : queries) {
                query.fillParseInfo(modelId, queryContext, chatContext);
                queryContext.getCandidateQueries().add(query);
            }
        }
    }

}
