import React, { useEffect, useState } from 'react';
import { Table, Button, Select, Tag, message, Radio, Modal, Input, Tree, Typography, Layout, Col, Row, Space } from 'antd';
import axios from 'axios';

const { Option } = Select;
const { Title } = Typography;
const { Content } = Layout;
const token = localStorage.getItem('SUPERSONIC_TOKEN');

const UserDepartmentManagement: React.FC = () => {
    const [users, setUsers] = useState([]);
    const [departments, setDepartments] = useState([]);
    const [filteredDepartments, setFilteredDepartments] = useState([]);
    const [departmentTree, setDepartmentTree] = useState([]);
    const [filteredDepartmentTree, setFilteredDepartmentTree] = useState([]);
    const [loading, setLoading] = useState(false);
    const [filter, setFilter] = useState('all');
    const [isAddModalVisible, setAddModalVisible] = useState(false);
    const [newDepartmentName, setNewDepartmentName] = useState('');
    const [parentDepartmentId, setParentDepartmentId] = useState<number | null>(null);
    const [searchKeyword, setSearchKeyword] = useState(''); // 部门搜索关键字
    const [userSearchKeyword, setUserSearchKeyword] = useState(''); // 用户搜索关键字

    useEffect(() => {
        fetchUsers();
        fetchDepartments();
    }, []);

    // 获取用户数据
    const fetchUsers = async () => {
        setLoading(true);
        try {
            const response = await axios.get('http://localhost:9080/api/semantic/department/getUserWithDepartment', {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
                withCredentials: true,
            });
            setUsers(response.data.data);
        } catch (error) {
            message.error('获取用户数据失败');
        } finally {
            setLoading(false);
        }
    };

    // 获取部门数据并构建部门树
    const fetchDepartments = async () => {
        try {
            const response = await axios.get('http://localhost:9080/api/semantic/department/getDepartmentList', {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
                withCredentials: true,
            });
            setDepartments(response.data.data);
            setFilteredDepartments(response.data.data);
            buildDepartmentTree(response.data.data);
        } catch (error) {
            message.error('获取部门数据失败');
        }
    };

    // 构建部门树结构
    const buildDepartmentTree = (departments: any[]) => {
        const departmentMap: { [key: number]: any } = {};
        departments.forEach((dept) => {
            departmentMap[dept.id] = { ...dept, children: [] };
        });

        const tree: any[] = [];
        departments.forEach((dept) => {
            if (dept.parentId === 0) {
                tree.push(departmentMap[dept.id]);
            } else {
                const parent = departmentMap[dept.parentId];
                if (parent) {
                    parent.children.push(departmentMap[dept.id]);
                }
            }
        });

        setDepartmentTree(tree);
        setFilteredDepartmentTree(tree);
    };

    // 部门搜索
    const handleDepartmentSearch = (value: string) => {
        setSearchKeyword(value);
        if (!value) {
            setFilteredDepartmentTree(departmentTree);
        } else {
            const filteredTree = filterTreeByName(departmentTree, value);
            setFilteredDepartmentTree(filteredTree);
        }
    };

    // 递归过滤部门树
    const filterTreeByName = (tree: any[], keyword: string) => {
        return tree
            .map((node) => {
                const children = node.children ? filterTreeByName(node.children, keyword) : [];
                if (node.name.includes(keyword) || children.length > 0) {
                    return { ...node, children };
                }
                return null;
            })
            .filter((node) => node !== null);
    };

    // 用户模糊搜索
    const handleUserSearch = (value: string) => {
        setUserSearchKeyword(value);
    };

    // 处理部门选择的模糊搜索
    const handleDepartmentSelectSearch = (value: string) => {
        const filtered = value
            ? departments.filter((dept: any) =>
                dept.name.toLowerCase().includes(value.toLowerCase())
            )
            : departments;
        setFilteredDepartments(filtered);
    };

    const saveOrUpdateDepartment = async (userId: number, departmentId: string | number, userName: string) => {
        try {
            const selectedDepartment = departments.find((dept) => dept.id === departmentId);
            if (!selectedDepartment) {
                message.error(`未找到 ID 为 "${departmentId}" 的部门`);
                return;
            }

            await axios.post(
                'http://localhost:9080/api/semantic/department/saveOrUpdateDepartmentForUser',
                {
                    userId,
                    departmentId,
                    userName,
                    departmentName: selectedDepartment.name,
                },
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                    withCredentials: true,
                }
            );
            message.success('部门更新成功');
            fetchUsers();
        } catch (error) {
            message.error('部门更新失败');
        }
    };

    const deleteUser = async (userId: number) => {
        try {
            await axios.delete(`http://localhost:9080/api/auth/user/deleteUser/${userId}`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
                withCredentials: true,
            });
            message.success('用户删除成功');
            fetchUsers();
        } catch (error) {
            message.error('用户删除失败');
        }
    };

    const showAddDepartmentModal = (parentId: number) => {
        setParentDepartmentId(parentId);
        setAddModalVisible(true);
    };

    const handleAddDepartment = async () => {
        try {
            await axios.post(
                'http://localhost:9080/api/semantic/department/addDepartment',
                {
                    id: null,
                    parentId: parentDepartmentId,
                    name: newDepartmentName.trim(),
                },
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                    withCredentials: true,
                }
            );

            message.success('部门添加成功');
            fetchDepartments();
        } catch (error) {
            message.error('部门添加失败');
        } finally {
            setAddModalVisible(false);
            setNewDepartmentName('');
        }
    };

    const handleDeleteDepartment = async (departmentId: number) => {
        try {
            await axios.get(`http://localhost:9080/api/semantic/department/deleteDepartmentById/${departmentId}`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
                withCredentials: true,
            });

            message.success('部门删除成功');
            fetchDepartments();
        } catch (error) {
            message.error('部门删除失败');
        }
    };

    const filteredUsers = users.filter((user: any) => {
        const matchesFilter = filter === 'all' ||
            (filter === 'set' && user.departmentId !== null && user.departmentName !== null) ||
            (filter === 'unset' && (user.departmentId === null || user.departmentName === null));
        const matchesSearch = userSearchKeyword ? user.userName.includes(userSearchKeyword) : true;
        return matchesFilter && matchesSearch;
    });

    const userColumns = [
        {
            title: '用户名',
            dataIndex: 'userName',
            key: 'userName',
            render: (text: string, record: any) => {
                if (!record.departmentId || !record.departmentName) {
                    return <Tag color="red">{text}</Tag>;
                }
                return text;
            },
        },
        {
            title: '部门名称',
            dataIndex: 'departmentName',
            key: 'departmentName',
            render: (text: string) => (text ? text : <Tag color="orange">未设置部门</Tag>),
        },
        {
            title: '操作',
            key: 'action',
            render: (_: any, record: any) => (
                <Space>
                    <Select
                        defaultValue={record.departmentId || undefined}
                        style={{ width: 150 }}
                        showSearch
                        allowClear
                        onDropdownVisibleChange={(open) => {
                            if (open) setFilteredDepartments(departments); // 打开时重置部门列表
                        }}
                        onSearch={handleDepartmentSelectSearch}
                        onChange={(value) => {
                            const departmentId = value || ''; // 当 value 为 undefined 时传递空字符串
                            saveOrUpdateDepartment(record.userId, departmentId, record.userName);
                        }}
                        filterOption={false}
                    >
                        {filteredDepartments.map((dept: any) => (
                            <Option key={dept.id} value={dept.id}>
                                {dept.name}
                            </Option>
                        ))}
                    </Select>
                    <Button
                        type="link"
                        danger
                        onClick={() => deleteUser(record.userId)}
                    >
                        删除
                    </Button>
                </Space>
            ),
        },
    ];

    return (
        <Layout style={{ height: '100vh', padding: '20px' }}>
            <Row gutter={16} align="top">
                <Col span={10} style={{ backgroundColor: '#f0f2f5', padding: '20px' }}>
                    <Title level={2} style={{ marginBottom: '20px', fontSize: '24px' }}>部门管理</Title>
                    <Input
                        placeholder="搜索部门名称"
                        value={searchKeyword}
                        onChange={(e) => handleDepartmentSearch(e.target.value)}
                        style={{ marginBottom: '16px' }}
                    />
                    <Tree
                        treeData={filteredDepartmentTree}
                        defaultExpandAll={true}
                        titleRender={(nodeData) => (
                            <div style={{
                                position: 'relative',
                                display: 'flex',
                                alignItems: 'center',
                                padding: '4px 0',
                            }}>
                                <span
                                    style={{
                                        whiteSpace: 'nowrap',
                                        overflow: 'hidden',
                                        textOverflow: 'ellipsis',
                                        width: '200px',
                                    }}
                                >
                                    {nodeData.name}
                                </span>
                                <div style={{
                                    position: 'absolute',
                                    right: 0,
                                    display: 'flex',
                                    gap: '8px',
                                }}>
                                    <Button type="primary" size="small" onClick={() => showAddDepartmentModal(nodeData.id)}>
                                        增加子部门
                                    </Button>
                                    <Button type="danger" size="small" onClick={() => handleDeleteDepartment(nodeData.id)}>
                                        删除
                                    </Button>
                                </div>
                            </div>
                        )}
                    />
                </Col>
                <Col span={14} style={{ padding: '20px' }}>
                    <Content>
                        <Title level={2} style={{ marginBottom: '20px', fontSize: '24px' }}>用户管理</Title>
                        <Radio.Group value={filter} onChange={(e) => setFilter(e.target.value)} style={{ marginBottom: 16 }}>
                            <Radio.Button value="all">所有用户</Radio.Button>
                            <Radio.Button value="set">已设置部门的用户</Radio.Button>
                            <Radio.Button value="unset">未设置部门的用户</Radio.Button>
                        </Radio.Group>

                        <Input
                            placeholder="搜索用户名"
                            value={userSearchKeyword}
                            onChange={(e) => handleUserSearch(e.target.value)}
                            style={{ marginBottom: '16px' }}
                        />

                        <Table dataSource={filteredUsers} columns={userColumns} rowKey="userId" loading={loading} pagination={{ pageSize: 10 }} />

                        <Modal
                            title="添加部门"
                            visible={isAddModalVisible}
                            onOk={handleAddDepartment}
                            onCancel={() => setAddModalVisible(false)}
                        >
                            <Input
                                placeholder="请输入部门名称"
                                value={newDepartmentName}
                                onChange={(e) => setNewDepartmentName(e.target.value)}
                            />
                        </Modal>
                    </Content>
                </Col>
            </Row>
        </Layout>
    );
};

export default UserDepartmentManagement;
