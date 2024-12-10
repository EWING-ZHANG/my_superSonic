import React, { useState, useEffect } from 'react';
import { Form, Input, Switch, message } from 'antd';
import SelectPartner from '@/components/SelectPartner';
import SelectTMEPerson from '@/components/SelectTMEPerson';
import { useModel } from '@umijs/max';
import FormItemTitle from '@/components/FormHelper/FormItemTitle';
import { updateDomain, updateModel, getDomainDetail, getModelDetail } from '../../service';

import styles from '../style.less';
import { getAgent, saveAgent } from '@/pages/Agent/service';
type Props = {
  permissionTarget: 'model' | 'domain' | 'agent';
  onSubmit?: (data?: any) => void;
  onValuesChange?: (value, values) => void;
};

const FormItem = Form.Item;

const PermissionAdminForm: React.FC<Props> = ({ permissionTarget, onValuesChange }) => {
  const [form] = Form.useForm();
  const [isOpenState, setIsOpenState] = useState<boolean>(true);
  const [classDetail, setClassDetail] = useState<any>({});
  const domainModel = useModel('SemanticModel.domainData');
  const modelModel = useModel('SemanticModel.modelData');
  const agentModel = useModel('SemanticModel.agentData');
  const { selectDomainId } = domainModel;
  const { selectModelId: modelId } = modelModel;
  const {selectAgentId} = agentModel;


  const queryClassDetail = async () => {
    let selectId =null;
    let queryFunc =null;
    if(permissionTarget=== 'model'){
        selectId = modelId;
        queryFunc=getModelDetail;
    }else if (permissionTarget==='domain'){
      selectId = selectDomainId;
      queryFunc=getDomainDetail;
    }else if (permissionTarget==='agent'){
      selectId = selectAgentId;
      queryFunc=getAgent;
    }
    const { code, msg, data } = await queryFunc?.({modelId: selectId});
    if (code === 200) {
      setClassDetail(data);
      const fieldsValue = {
        ...data,
      };
      //这个具体没弄懂
      fieldsValue.admins = fieldsValue.admins || [];
      fieldsValue.adminOrgs = fieldsValue.adminOrgs || [];
      fieldsValue.viewers = fieldsValue.viewers || [];
      fieldsValue.viewOrgs = fieldsValue.viewOrgs || [];
      fieldsValue.isOpen = !!fieldsValue.isOpen;
      setIsOpenState(fieldsValue.isOpen);
      form.setFieldsValue(fieldsValue);
      return;
    }
    message.error(msg);
  };

  useEffect(() => {
    queryClassDetail();
  }, [modelId, selectDomainId,selectAgentId]);

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
      // id: selectAgentId,
    };
    let updateFunc = null;
    if(permissionTarget=== 'model'){
      updateFunc=updateModel;
  }else if (permissionTarget==='domain'){
    updateFunc=updateDomain;
  }else if (permissionTarget==='agent'){
    updateFunc=saveAgent;
  }
  const {code,msg} = await updateFunc?.(queryClassData);
    
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
          <SelectTMEPerson placeholder="请邀请团队成员" />
        </FormItem>
        {/* {APP_TARGET === 'inner'} */}
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
            {/* {APP_TARGET === 'inner' && } */}
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
