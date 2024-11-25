import { useState, useEffect } from 'react';
import type { FC } from 'react';
import { Select } from 'antd';
import type { UserItem } from './service';
import { useModel } from '@umijs/max';
import styles from './index.less';
import TMEAvatar from '../TMEAvatar';

interface Props {
  value?: string[];
  placeholder?: string;
  isMultiple?: boolean;
  onChange?: (owners: string | string[]) => void;
}

const SelectTMEPerson: FC<Props> = ({ placeholder, value = [], isMultiple = true, onChange }) => {
  const [userList, setUserList] = useState<UserItem[]>([]);
  const [displayNames, setDisplayNames] = useState<string[]>([]);
  const [displayNameMap, setDisplayNameMap] = useState<Record<string, string>>({});
  const allUserModel = useModel('allUserData');
  const { allUserList, MrefreshUserList } = allUserModel;

  const queryTmePersonData = async () => {
    const list = await MrefreshUserList();
    setUserList(list);
    createDisplayNameMap(list);
    mapIdsToDisplayNames(value, list);
  };

  // Create a map for quick ID-to-name lookup
  const createDisplayNameMap = (list: UserItem[]) => {
    const map = list.reduce((acc, user) => {
      acc[user.id] = user.name;
      return acc;
    }, {} as Record<string, string>);
    setDisplayNameMap(map);
  };

  // Map IDs to names for UI display only
  const mapIdsToDisplayNames = (ids: string[], list: UserItem[]) => {
    const names = ids.map(id => displayNameMap[id] || id);
    setDisplayNames(names);
  };

  useEffect(() => {
    if (Array.isArray(allUserList) && allUserList.length > 0) {
      setUserList(allUserList);
      createDisplayNameMap(allUserList);
      mapIdsToDisplayNames(value, allUserList);
    } else {
      queryTmePersonData();
    }
  }, [value, allUserList]);

  return (
    <Select
      value={value}
      placeholder={placeholder ?? '请选择用户名'}
      mode={isMultiple ? 'multiple' : undefined}
      allowClear
      showSearch
      onChange={(selectedIds) => {
        onChange?.(selectedIds); 
        mapIdsToDisplayNames(selectedIds, userList);
      }}
    >
      {userList.map((item) => (
        <Select.Option key={item.id} value={item.id}>
          <TMEAvatar size="small" staffName={item.name} />
          <span className={styles.userText}>{item.name}</span>
        </Select.Option>
      ))}
    </Select>
  );
};

export default SelectTMEPerson;
