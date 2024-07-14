package com.tencent.supersonic.common.config;

import com.google.common.collect.Lists;
import com.tencent.supersonic.common.pojo.ChatModelConfig;
import com.tencent.supersonic.common.pojo.Parameter;
import dev.langchain4j.provider.OllamaModelFactory;
import dev.langchain4j.provider.OpenAiModelFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("ChatModelParameterConfig")
@Slf4j
public class ChatModelParameterConfig extends ParameterConfig {

    public static final Parameter CHAT_MODEL_PROVIDER =
            new Parameter("s2.chat.model.provider", OpenAiModelFactory.PROVIDER,
                    "接口协议", "",
                    "string", "对话模型配置",
                    Lists.newArrayList(OpenAiModelFactory.PROVIDER, OllamaModelFactory.PROVIDER));

    public static final Parameter CHAT_MODEL_BASE_URL =
            new Parameter("s2.chat.model.base.url", "",
                    "BaseUrl", "",
                    "string", "对话模型配置");

    public static final Parameter CHAT_MODEL_API_KEY =
            new Parameter("s2.chat.model.api.key", "",
                    "ApiKey", "",
                    "string", "对话模型配置");

    public static final Parameter CHAT_MODEL_NAME =
            new Parameter("s2.chat.model.name", "",
                    "ModelName", "",
                    "string", "对话模型配置");

    public static final Parameter CHAT_MODEL_TEMPERATURE =
            new Parameter("s2.chat.model.temperature", "0.0",
                    "Temperature", "",
                    "number", "对话模型配置");

    public static final Parameter CHAT_MODEL_TIMEOUT =
            new Parameter("s2.chat.model.timeout", "60",
                    "超时时间(秒)", "",
                    "number", "对话模型配置");

    @Override
    public List<Parameter> getSysParameters() {
        return Lists.newArrayList(
                CHAT_MODEL_PROVIDER, CHAT_MODEL_BASE_URL, CHAT_MODEL_API_KEY,
                CHAT_MODEL_NAME, CHAT_MODEL_TEMPERATURE, CHAT_MODEL_TIMEOUT
        );
    }

    public ChatModelConfig convert() {
        String chatModelProvider = getParameterValue(CHAT_MODEL_PROVIDER);
        String chatModelBaseUrl = getParameterValue(CHAT_MODEL_BASE_URL);
        String chatModelApiKey = getParameterValue(CHAT_MODEL_API_KEY);
        String chatModelName = getParameterValue(CHAT_MODEL_NAME);
        String chatModelTemperature = getParameterValue(CHAT_MODEL_TEMPERATURE);
        String chatModelTimeout = getParameterValue(CHAT_MODEL_TIMEOUT);

        return ChatModelConfig.builder()
                .provider(chatModelProvider)
                .baseUrl(chatModelBaseUrl)
                .apiKey(chatModelApiKey)
                .modelName(chatModelName)
                .temperature(Double.valueOf(chatModelTemperature))
                .timeOut(Long.valueOf(chatModelTimeout))
                .build();
    }
}
