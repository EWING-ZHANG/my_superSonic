package com.tencent.supersonic.chat.domain.dataobject;

import lombok.Data;

@Data
public class ChatDO {

    private long chatId;
    private String chatName;
    private String createTime;
    private String lastTime;
    private String creator;
    private String lastQuestion;
    private int isDelete;
    private int isTop;
}
