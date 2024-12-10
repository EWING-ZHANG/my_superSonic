import { ISemantic } from '../data';
import { useState } from 'react';

export default function Agent() {
    const [selectAgent, setSelectAgent] = useState<ISemantic.IAgentItem>();



  return {
    selectAgent,
    selectAgentId: selectAgent?.id,
    selectAgentName: selectAgent?.name,
    setSelectAgent
  };
}
