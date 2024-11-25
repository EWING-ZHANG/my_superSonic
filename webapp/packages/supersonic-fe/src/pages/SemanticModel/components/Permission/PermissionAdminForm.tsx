import React, { useState, useEffect, useRef } from 'react';
import { Form, Input, Switch, message } from 'antd';
import SelectPartner from '@/components/SelectPartner';
import SelectTMEPerson from '@/components/SelectTMEPerson';
import { useModel } from '@umijs/max';
import FormItemTitle from '@/components/FormHelper/FormItemTitle';
import { updateDomain, updateModel, getDomainDetail, getModelDetail } from '../../service';
import { getAllUser, GetAllUserRes } from '@/components/SelectTMEPerson/service';
import FormItem from 'antd/es/form/FormItem';

import styles from '../style.less';

type Props = {
  permissionTarget: 'model' | 'domain';
  onSubmit?: (data?: any) => void;
  onValuesChange?: (value, values) => void;
};

const PermissionAdminForm: React.FC<Props> = ({ permissionTarget, onValuesChange }) => {
  // 使用 useRef 来存储 myMap，使它能够在整个组件中共享
  const myMapRef = useRef(new Map());

  const [form] = Form.useForm();
  const [isOpenState, setIsOpenState] = useState<boolean>(true);
  const [classDetail, setClassDetail] = useState<any>({});
  const domainModel = useModel('SemanticModel.domainData');
  const modelModel = useModel('SemanticModel.modelData');
  const { selectDomainId } = domainModel;
  const { selectModelId: modelId } = modelModel;

  const fetchUserList = async () => {
    try {
      const users = await getAllUser();
      console.log("Fetched Users:", users);

      // 将用户数据存储到 myMapRef 中
      const array = users.data;
      myMapRef.current = new Map(array.map(user => [user.id.toString(), user.name]));
    } catch (error) {
      console.error("Error fetching users:", error);
      message.error("Failed to fetch user list.");
    }
  };

  useEffect(() => {
    fetchUserList();
  }, []);

  const queryClassDetail = async () => {
    const selectId = permissionTarget === 'model' ? modelId : selectDomainId;
    const { code, msg, data } = await (permissionTarget === 'model'
      ? getModelDetail
      : getDomainDetail)({ modelId: selectId });

    if (code === 200) {
      setClassDetail(data);
      const fieldsValue = {
        ...data,
      };
      // 使用 myMapRef.current 来获取值
      const adminNames = (fieldsValue?.admins || []).map(
        key => myMapRef.current.get(key?.toString()) || "Unknown"
      );
      const viewerNames = (fieldsValue?.viewers || []).map(
        key => myMapRef.current.get(key?.toString()) || "Unknown"
      );
      fieldsValue.admins = adminNames || [];
      fieldsValue.adminOrgs = fieldsValue.adminOrgs || [];
      fieldsValue.viewers = viewerNames || [];
      fieldsValue.viewOrgs = fieldsValue.viewOrgs || [];
      fieldsValue.isOpen = !!fieldsValue.isOpen;
      setIsOpenState(fieldsValue.isOpen);
      form.setFieldsValue(fieldsValue);
      return;
    }
    message.error(msg);
  };

  useEffect(() => {
    // 确保在 fetchUserList 执行完成后再调用 queryClassDetail
    const initializeDetails = async () => {
      await fetchUserList(); // 等待用户列表获取完成
      await queryClassDetail(); // 确保 myMap 已初始化后调用
    };
    initializeDetails();
  }, [modelId, selectDomainId]);

  const saveAuth = async () => {
    const values = await form.validateFields();
    const { admins, adminOrgs, isOpen, viewOrgs = [], viewers = [] } = values;
    const queryClassData = {
      ...classDetail,
      admins,
      adminOrgs,
      viewOrgs,
      viewers,
      isOpen: isOpen ? 1 : 0,
    };
    const { code, msg } = await (permissionTarget === 'model' ? updateModel : updateDomain)(
      queryClassData,
    );
    if (code === 200) {
      return;
    }
    message.error(msg);
  };

  return (
    <>
      <Form
        form={form}
        layout="vertical"
        onValuesChange={(value, values) => {
          const { isOpen } = value;
          if (isOpen !== undefined) {
            setIsOpenState(isOpen);
          }
          saveAuth();
          onValuesChange?.(value, values);
        }}
        className={styles.form}
      >
        <FormItem hidden={true} name="groupId" label="ID">
          <Input placeholder="groupId" />
        </FormItem>
        <FormItem
          name="admins"
          label={
            <FormItemTitle title={'管理员'} subTitle={'管理员将拥有主题域下所有编辑及访问权限'} />
          }
        >
          <SelectTMEPerson placeholder='请选择团队成员' />
        </FormItem>
        <FormItem name="adminOrgs" label="按组织">
          <SelectPartner
            type="selectedDepartment"
            treeSelectProps={{
              placeholder: '请选择需要授权的部门',
            }}
          />
        </FormItem>
        <Form.Item
          label={
            <FormItemTitle
              title={'设为公开'}
              subTitle={
                '公开后,所有用户将可使用主题域下低/中敏感度资源，高敏感度资源需通过资源列表进行授权'
              }
            />
          }
          name="isOpen"
          valuePropName="checked"
        >
          <Switch />
        </Form.Item>
        {!isOpenState && (
          <>
            <FormItem name="viewOrgs" label="按组织">
              <SelectPartner
                type="selectedDepartment"
                treeSelectProps={{
                  placeholder: '请选择需要授权的部门',
                }}
              />
            </FormItem>
            <FormItem name="viewers" label="按个人">
              <SelectTMEPerson placeholder="请选择需要授权的个人" />
            </FormItem>
          </>
        )}
      </Form>
    </>
  );
};

export default PermissionAdminForm;
