import { request } from 'umi';

export async function getUserByDeptid(id: any) {
  return request<any>(`${process.env.AUTH_API_BASE_URL}user/getUserByOrg/${id}`, {
    method: 'GET',
  });
}
export async function getOrganizationTree() {
  return request<any>(`${process.env.API_BASE_URL}department/getOrganizationTree`, {
    method: 'GET',
  });
}
