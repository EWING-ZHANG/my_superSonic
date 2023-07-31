export type SearchRecommendItem = {
  complete: boolean;
  domainId: number;
  domainName: string;
  recommend: string;
  subRecommend: string;
  schemaElementType: string;
};

export type FieldType = {
  bizName: string;
  id: number;
  name: string;
  status: number;
  value: string;
};

export type DomainInfoType = {
  bizName: string;
  itemId: number;
  name: string;
  primaryEntityBizName: string;
  value: string;
  words: string[];
};

export type EntityInfoType = {
  domainInfo: DomainInfoType;
  dimensions: FieldType[];
  metrics: FieldType[];
  entityId: number;
};

export type DateInfoType = {
  dateList: any[];
  dateMode: number;
  period: string; 
  startDate: string;
  endDate: string;
  text: string;
  unit: number;
};

export type FilterItemType = {
  elementID: number;
  name: string;
  operator: string;
  type: string;
  value: string[];
};

export type ChatContextType = {
  aggType: string;
  domainId: number;
  domainName: string;
  dateInfo: DateInfoType;
  dimensions: FieldType[];
  metrics: FieldType[];
  entity: number;
  dimensionFilters: FilterItemType[];
};

export enum MsgValidTypeEnum {
  NORMAL = 0,
  SEARCH_EXCEPTION = 1,
  EMPTY = 2,
  INVALID = 3,
};

export type InstructionResonseType = {
  description: string;
  instructionConfig: {
    showElements: { elementId: string, params: any }[];
    showType: string;
    relaShowElements: { elementId: string, params: any }[];
    relaShowType: string;
  };
  instructionId: number;
  instructionType: string;
  name: string;
}

export type MetricInfoType = {
  date: string;
  name: string;
  statistics: any;
  value: string;
}

export type AggregateInfoType = {
  metricInfos: MetricInfoType[]
}

export type MsgDataType = {
  id: number;
  question: string;
  aggregateInfo: AggregateInfoType;
  chatContext: ChatContextType;
  entityInfo: EntityInfoType;
  queryAuthorization: any;
  queryColumns: ColumnType[];
  queryResults: any[];
  queryId: number;
  queryMode: string;
  queryState: string;
  response: InstructionResonseType;
};

export type QueryDataType = {
  queryColumns: ColumnType[];
  queryResults: any[];
};

export type ColumnType = {
  authorized: boolean;
  name: string;
  nameEn: string;
  showType: string;
  type: string;
  dataFormatType: string;
  dataFormat: {
    decimalPlaces: number;
    needmultiply100: boolean;
  };
};

export enum SemanticTypeEnum {
  DOMAIN = 'DOMAIN',
  DIMENSION = 'DIMENSION',
  METRIC = 'METRIC',
  VALUE = 'VALUE',
};

export const SEMANTIC_TYPE_MAP = {
  [SemanticTypeEnum.DOMAIN]: '主题域',
  [SemanticTypeEnum.DIMENSION]: '维度',
  [SemanticTypeEnum.METRIC]: '指标',
  [SemanticTypeEnum.VALUE]: '维度值',
};

export type SuggestionItemType = {
  domain: number;
  name: string;
  bizName: string
};

export type SuggestionType = {
  dimensions: SuggestionItemType[];
  metrics: SuggestionItemType[];
};

export type SuggestionDataType = {
  currentAggregateType: string,
  columns: ColumnType[],
  mainEntity: EntityInfoType,
  suggestions: SuggestionType,
};

export type HistoryMsgItemType = {
  questionId: number;
  queryText: string;
  queryResult: MsgDataType;
  chatId: number;
  createTime: string;
  feedback: string;
  score: number;
};

export type HistoryType = {
  hasNextPage: boolean;
  list: HistoryMsgItemType[];
};

export type DrillDownDimensionType = {
  id: number;
  domain: number;
  name: string;
  bizName: string;
}