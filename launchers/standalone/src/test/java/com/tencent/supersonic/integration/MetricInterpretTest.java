package com.tencent.supersonic.integration;

import com.alibaba.fastjson.JSONObject;
import com.tencent.supersonic.StandaloneLauncher;
import com.tencent.supersonic.chat.query.llm.analytics.LLMAnswerResp;
import com.tencent.supersonic.chat.service.AgentService;
import com.tencent.supersonic.common.config.EmbeddingConfig;
import com.tencent.supersonic.common.util.embedding.EmbeddingUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = StandaloneLauncher.class)
@ActiveProfiles("local")
public class MetricInterpretTest {

    @MockBean
    private AgentService agentService;

    @MockBean
    private EmbeddingConfig embeddingConfig;

    @MockBean
    private EmbeddingUtils embeddingUtils;

    @Test
    public void testMetricInterpret() throws Exception {
        MockConfiguration.mockAgent(agentService);
        MockConfiguration.mockEmbeddingUrl(embeddingConfig);

        LLMAnswerResp lLmAnswerResp = new LLMAnswerResp();
        lLmAnswerResp.setAssistantMessage("alice最近在超音数的访问情况有增多");

        MockConfiguration.embeddingUtils(embeddingUtils, ResponseEntity.ok(JSONObject.toJSONString(lLmAnswerResp)));
    }

}
