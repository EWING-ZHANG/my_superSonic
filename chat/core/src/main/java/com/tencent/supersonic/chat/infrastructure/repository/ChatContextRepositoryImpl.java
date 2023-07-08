package com.tencent.supersonic.chat.infrastructure.repository;

import com.google.gson.Gson;
import com.tencent.supersonic.chat.api.pojo.ChatContext;
import com.tencent.supersonic.chat.api.pojo.SemanticParseInfo;
import com.tencent.supersonic.chat.domain.dataobject.ChatContextDO;
import com.tencent.supersonic.chat.domain.repository.ChatContextRepository;
import com.tencent.supersonic.chat.infrastructure.mapper.ChatContextMapper;
import com.tencent.supersonic.common.util.json.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public class ChatContextRepositoryImpl implements ChatContextRepository {

    @Autowired(required = false)
    private final ChatContextMapper chatContextMapper;

    public ChatContextRepositoryImpl(ChatContextMapper chatContextMapper) {
        this.chatContextMapper = chatContextMapper;
    }

    @Override
    public ChatContext getOrCreateContext(int chatId) {
        ChatContextDO context = chatContextMapper.getContextByChatId(chatId);
        if (context == null) {
            ChatContext chatContext = new ChatContext();
            chatContext.setChatId(chatId);
            return chatContext;
        }
        return cast(context);
    }

    @Override
    public void updateContext(ChatContext chatCtx) {
        ChatContextDO context = cast(chatCtx);
        if (chatContextMapper.getContextByChatId(chatCtx.getChatId()) == null) {
            chatContextMapper.addContext(context);
        } else {
            chatContextMapper.updateContext(context);
        }
    }


    private ChatContext cast(ChatContextDO contextDO) {
        ChatContext chatContext = new ChatContext();
        chatContext.setChatId(contextDO.getChatId());
        chatContext.setUser(contextDO.getUser());
        chatContext.setQueryText(contextDO.getQueryText());
        if (contextDO.getSemanticParse() != null && !contextDO.getSemanticParse().isEmpty()) {
            SemanticParseInfo semanticParseInfo = JsonUtil.toObject(contextDO.getSemanticParse(),
                    SemanticParseInfo.class);
            chatContext.setParseInfo(semanticParseInfo);
        }
        return chatContext;
    }

    private ChatContextDO cast(ChatContext chatContext) {
        ChatContextDO chatContextDO = new ChatContextDO();
        chatContextDO.setChatId(chatContext.getChatId());
        chatContextDO.setQueryText(chatContext.getQueryText());
        chatContextDO.setUser(chatContext.getUser());
        if (chatContext.getParseInfo() != null) {
            Gson g = new Gson();
            chatContext.getParseInfo().getDimensions().stream().forEach(d -> {
                d.setUpdatedAt(null);
                d.setCreatedAt(null);
            });
            chatContext.getParseInfo().getMetrics().stream().forEach(d -> {
                d.setUpdatedAt(null);
                d.setCreatedAt(null);
            });
            chatContextDO.setSemanticParse(g.toJson(chatContext.getParseInfo()));
        }
        return chatContextDO;
    }
}
