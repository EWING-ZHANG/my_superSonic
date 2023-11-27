export type MetricOptionType = {
  id: string;
  metricId?: number;
  modelId?: number;
}

export enum AgentToolTypeEnum {
  RULE = 'RULE',
  LLM_S2SQL = 'LLM_S2SQL',
  PLUGIN = 'PLUGIN',
  INTERPRET = 'INTERPRET'
}

export const AGENT_TOOL_TYPE_LIST = [
  {
    label: '规则语义解析',
    value: AgentToolTypeEnum.RULE
  },
  {
    label: '大模型语义解析',
    value: AgentToolTypeEnum.LLM_S2SQL
  },
  {
    label: '大模型指标解读',
    value: AgentToolTypeEnum.INTERPRET
  },
  {
    label: '第三方插件',
    value: AgentToolTypeEnum.PLUGIN
  },
]

export enum QueryModeEnum {
  METRIC = 'METRIC',
  TAG = 'TAG'
}

export const QUERY_MODE_LIST = [
  {
    label: '指标模式',
    value: QueryModeEnum.METRIC
  },
  {
    label: '标签模式',
    value: QueryModeEnum.TAG
  }
];

export type AgentToolType = {
  id?: string;
  type: AgentToolTypeEnum;
  name: string;
  queryModes?: QueryModeEnum[];
  plugins?: number[];
  metricOptions?: MetricOptionType[];
  exampleQuestions?: string[];
  modelIds?: number[];
}

export type AgentConfigType = {
  tools: AgentToolType[];
}

export type AgentType = {
  id?: number;
  name?: string;
  description?: string;
  createdBy?: string;
  updatedBy?: string;
  createdAt?: string;
  updatedAt?: string;
  examples?: string[];
  status?: 0 | 1;
  enableSearch?: 0 | 1;
  agentConfig?: AgentConfigType;
}

export type ModelType = {
  id: number | string;
  parentId: number;
  name: string;
  bizName: string;
};

export type MetricType = {
  id: number;
  name: string;
  bizName: string;
};
