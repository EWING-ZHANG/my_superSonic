package com.tencent.supersonic.headless.core.chat.parser.llm;

import com.tencent.supersonic.common.util.JsonUtil;
import com.tencent.supersonic.headless.core.chat.query.llm.s2sql.LLMReq;
import com.tencent.supersonic.headless.core.chat.query.llm.s2sql.LLMResp;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.output.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OnePassSCSqlGenStrategy extends SqlGenStrategy {

    @Override
    public LLMResp generate(LLMReq llmReq) {
        //1.retriever sqlExamples and generate exampleListPool
        keyPipelineLog.info("OnePassSCSqlGenStrategy llmReq:{}", llmReq);

        List<Map<String, String>> sqlExamples = exemplarManager.recallExemplars(llmReq.getQueryText(),
                optimizationConfig.getText2sqlExampleNum());

        List<List<Map<String, String>>> exampleListPool = promptGenerator.getExampleCombos(sqlExamples,
                optimizationConfig.getText2sqlFewShotsNum(), optimizationConfig.getText2sqlSelfConsistencyNum());

        //2.generator linking and sql prompt by sqlExamples,and parallel generate response.
        List<String> linkingSqlPromptPool = promptGenerator.generatePromptPool(llmReq, exampleListPool, true);
        List<String> llmResults = new CopyOnWriteArrayList<>();
        linkingSqlPromptPool.parallelStream().forEach(linkingSqlPrompt -> {
                    Prompt prompt = PromptTemplate.from(JsonUtil.toString(linkingSqlPrompt))
                            .apply(new HashMap<>());
                    keyPipelineLog.info("OnePassSCSqlGenStrategy reqPrompt:{}", prompt.toSystemMessage());
            ChatLanguageModel chatLanguageModel = getChatLanguageModel(llmReq.getLlmConfig());
            Response<AiMessage> response = chatLanguageModel.generate(prompt.toSystemMessage());
                    String result = response.content().text();
                    llmResults.add(result);
                    keyPipelineLog.info("OnePassSCSqlGenStrategy modelResp:{}", result);
                }
        );
        //3.format response.
        List<String> schemaLinkingResults = llmResults.stream()
                .map(llmResult -> OutputFormat.getSchemaLinks(llmResult)).collect(Collectors.toList());
        List<String> candidateSortedList = OutputFormat.formatList(schemaLinkingResults);
        Pair<String, Map<String, Double>> linkingMap = OutputFormat.selfConsistencyVote(candidateSortedList);
        List<String> sqlList = llmResults.stream()
                .map(llmResult -> OutputFormat.getSql(llmResult)).collect(Collectors.toList());

        Pair<String, Map<String, Double>> sqlMapPair = OutputFormat.selfConsistencyVote(sqlList);

        LLMResp result = new LLMResp();
        result.setQuery(llmReq.getQueryText());
        result.setSqlRespMap(OutputFormat.buildSqlRespMap(sqlExamples, sqlMapPair.getRight()));
        return result;
    }

    @Override
    public void afterPropertiesSet() {
        SqlGenStrategyFactory.addSqlGenerationForFactory(LLMReq.SqlGenType.ONE_PASS_AUTO_COT_SELF_CONSISTENCY, this);
    }
}
