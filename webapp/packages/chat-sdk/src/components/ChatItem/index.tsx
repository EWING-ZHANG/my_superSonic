import { ChatContextType, MsgDataType, ParseStateEnum } from '../../common/type';
import { useEffect, useState } from 'react';
import { chatExecute, chatParse, switchEntity } from '../../service';
import { PARSE_ERROR_TIP, PREFIX_CLS, SEARCH_EXCEPTION_TIP } from '../../common/constants';
import IconFont from '../IconFont';
import ParseTip from './ParseTip';
import ExecuteItem from './ExecuteItem';
import { isMobile } from '../../utils/utils';
import classNames from 'classnames';
import Tools from '../Tools';

type Props = {
  msg: string;
  conversationId?: number;
  modelId?: number;
  agentId?: number;
  filter?: any[];
  isLastMessage?: boolean;
  msgData?: MsgDataType;
  isMobileMode?: boolean;
  isHistory?: boolean;
  triggerResize?: boolean;
  parseOptions?: ChatContextType[];
  onMsgDataLoaded?: (data: MsgDataType, valid: boolean) => void;
  onUpdateMessageScroll?: () => void;
};

const ChatItem: React.FC<Props> = ({
  msg,
  conversationId,
  modelId,
  agentId,
  filter,
  isLastMessage,
  isMobileMode,
  isHistory,
  triggerResize,
  msgData,
  parseOptions,
  onMsgDataLoaded,
  onUpdateMessageScroll,
}) => {
  const [data, setData] = useState<MsgDataType>();
  const [parseLoading, setParseLoading] = useState(false);
  const [parseInfo, setParseInfo] = useState<ChatContextType>();
  const [parseInfoOptions, setParseInfoOptions] = useState<ChatContextType[]>(parseOptions || []);
  const [parseTip, setParseTip] = useState('');
  const [executeLoading, setExecuteLoading] = useState(false);
  const [executeTip, setExecuteTip] = useState('');
  const [executeMode, setExecuteMode] = useState(false);
  const [entitySwitchLoading, setEntitySwitchLoading] = useState(false);

  const [chartIndex, setChartIndex] = useState(0);

  const prefixCls = `${PREFIX_CLS}-item`;

  const updateData = (res: Result<MsgDataType>) => {
    if (res.code === 401 || res.code === 412) {
      setExecuteTip(res.msg);
      return false;
    }
    if (res.code !== 200) {
      setExecuteTip(SEARCH_EXCEPTION_TIP);
      return false;
    }
    const { queryColumns, queryResults, queryState, queryMode, response } = res.data || {};
    if (queryState !== 'SUCCESS') {
      setExecuteTip(response && typeof response === 'string' ? response : SEARCH_EXCEPTION_TIP);
      return false;
    }
    if ((queryColumns && queryColumns.length > 0 && queryResults) || queryMode === 'WEB_PAGE') {
      setData(res.data);
      setExecuteTip('');
      return true;
    }
    setExecuteTip(SEARCH_EXCEPTION_TIP);
    return true;
  };

  const onExecute = async (
    parseInfoValue: ChatContextType,
    parseInfoOptions?: ChatContextType[]
  ) => {
    setExecuteMode(true);
    setExecuteLoading(true);
    try {
      const { data } = await chatExecute(msg, conversationId!, parseInfoValue);
      setExecuteLoading(false);
      const valid = updateData(data);
      if (onMsgDataLoaded) {
        let parseOptions: ChatContextType[] = parseInfoOptions || [];
        if (
          parseInfoOptions &&
          parseInfoOptions.length > 1 &&
          (parseInfoOptions[0].queryMode.includes('METRIC') ||
            parseInfoOptions[0].queryMode.includes('ENTITY'))
        ) {
          parseOptions = parseInfoOptions.filter(
            (item, index) =>
              index === 0 ||
              (!item.queryMode.includes('METRIC') && !item.queryMode.includes('ENTITY'))
          );
        }
        onMsgDataLoaded(
          {
            ...data.data,
            chatContext: parseInfoValue,
            parseOptions: parseOptions.length > 1 ? parseOptions.slice(1) : undefined,
          },
          valid
        );
      }
    } catch (e) {
      setExecuteLoading(false);
      setExecuteTip(SEARCH_EXCEPTION_TIP);
    }
  };

  const onSendMsg = async () => {
    setParseLoading(true);
    const { data: parseData } = await chatParse(msg, conversationId, modelId, agentId, filter);
    setParseLoading(false);
    const { code, data } = parseData || {};
    const { state, selectedParses, queryId } = data || {};
    if (
      code !== 200 ||
      state === ParseStateEnum.FAILED ||
      !selectedParses?.length ||
      (!selectedParses[0]?.properties?.type && !selectedParses[0]?.queryMode)
    ) {
      setParseTip(PARSE_ERROR_TIP);
      return;
    }
    if (onUpdateMessageScroll) {
      onUpdateMessageScroll();
    }
    const parseInfos = selectedParses.map(item => ({
      ...item,
      queryId,
    }));
    setParseInfoOptions(parseInfos || []);
    const parseInfoValue = parseInfos[0];
    setParseInfo(parseInfoValue);
    onExecute(parseInfoValue, parseInfos);
  };

  useEffect(() => {
    if (data !== undefined || parseOptions !== undefined || executeTip !== '') {
      return;
    }
    if (msgData) {
      setParseInfoOptions([msgData.chatContext]);
      setExecuteMode(true);
      updateData({ code: 200, data: msgData, msg: 'success' });
    } else if (msg) {
      onSendMsg();
    }
  }, [msg, msgData]);

  const onSwitchEntity = async (entityId: string) => {
    setEntitySwitchLoading(true);
    const res = await switchEntity(entityId, data?.chatContext?.modelId, conversationId || 0);
    setEntitySwitchLoading(false);
    setData(res.data.data);
    const { chatContext } = res.data.data;
    setParseInfo(chatContext);
    setParseInfoOptions([chatContext]);
  };

  const onChangeChart = () => {
    setChartIndex(chartIndex + 1);
  };

  const onSelectParseInfo = async (parseInfoValue: ChatContextType) => {
    setParseInfo(parseInfoValue);
    onExecute(parseInfoValue);
    if (onUpdateMessageScroll) {
      onUpdateMessageScroll();
    }
  };

  const contentClass = classNames(`${prefixCls}-content`, {
    [`${prefixCls}-content-mobile`]: isMobile,
  });

  const isMetricCard =
    (data?.queryMode === 'METRIC_DOMAIN' || data?.queryMode === 'METRIC_FILTER') &&
    data?.queryResults?.length === 1;

  return (
    <div className={prefixCls}>
      {!isMobile && <IconFont type="icon-zhinengsuanfa" className={`${prefixCls}-avatar`} />}
      <div className={isMobile ? `${prefixCls}-mobile-msg-card` : `${prefixCls}-msg-card`}>
        <div className={contentClass}>
          <ParseTip
            parseLoading={parseLoading}
            parseInfoOptions={parseOptions || parseInfoOptions.slice(0, 1)}
            parseTip={parseTip}
            currentParseInfo={parseInfo}
            optionMode={parseOptions !== undefined}
            onSelectParseInfo={onSelectParseInfo}
            onSwitchEntity={onSwitchEntity}
          />
          {executeMode && (
            <ExecuteItem
              question={msg}
              queryId={parseInfo?.queryId}
              executeLoading={executeLoading}
              entitySwitchLoading={entitySwitchLoading}
              executeTip={executeTip}
              chartIndex={chartIndex}
              data={data}
              isMobileMode={isMobileMode}
              triggerResize={triggerResize}
              onChangeChart={onChangeChart}
            />
          )}
        </div>
        {!isMetricCard && data && (
          <Tools data={data} scoreValue={undefined} isLastMessage={isLastMessage} />
        )}
      </div>
    </div>
  );
};

export default ChatItem;
