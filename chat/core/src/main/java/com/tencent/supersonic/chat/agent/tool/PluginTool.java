package com.tencent.supersonic.chat.agent.tool;


import lombok.Data;

import java.util.List;

@Data
public class PluginTool extends AgentTool {

    private List<Long> plugins;

}