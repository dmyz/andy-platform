import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';

import * as organizationApi from '@/api/organization';
import * as permissionApi from '@/api/permission';
import * as roleApi from '@/api/role';
import { useOrganizationStore } from '@/stores/organization';
import { usePermissionStore } from '@/stores/permission';
import { useRoleStore } from '@/stores/role';

vi.mock('@/api/role', () => ({
  getRoleList: vi.fn(),
  createRole: vi.fn(),
  updateRole: vi.fn(),
  deleteRole: vi.fn(),
  assignPermissions: vi.fn(),
  getRolePermissions: vi.fn(),
}));

vi.mock('@/api/permission', () => ({
  getPermissionList: vi.fn(),
  createPermission: vi.fn(),
  updatePermission: vi.fn(),
  deletePermission: vi.fn(),
}));

vi.mock('@/api/organization', () => ({
  getOrganizationList: vi.fn(),
  getOrganizationTree: vi.fn(),
  createOrganization: vi.fn(),
  updateOrganization: vi.fn(),
  deleteOrganization: vi.fn(),
}));

describe('stores/catalog', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('ROLE-UNIT-001 角色列表默认使用缓存，强制刷新重新请求', async () => {
    vi.mocked(roleApi.getRoleList).mockResolvedValue({
      list: [{ id: 'r1', code: 'admin', name: '管理员', status: 1 }],
    });

    const store = useRoleStore();
    await store.fetchRoles();
    await store.fetchRoles();
    await store.fetchRoles(true);

    expect(roleApi.getRoleList).toHaveBeenCalledTimes(2);
    expect(store.getRoleByCode('admin')?.name).toBe('管理员');
  });

  it('PERM-UNIT-001 权限树按 parentId 组装并支持任一/全部权限判断', async () => {
    vi.mocked(permissionApi.getPermissionList).mockResolvedValue([
      {
        id: 'p1',
        code: 'system:user:view',
        name: '用户查看',
        type: 'NAV_ACCESS',
        resourceType: 'USER',
        moduleCode: 'system',
        status: 1,
      },
      {
        id: 'p2',
        parentId: 'p1',
        code: 'system:user:create',
        name: '用户新增',
        type: 'UI_ACTION',
        resourceType: 'USER',
        moduleCode: 'system',
        status: 1,
      },
    ]);

    const store = usePermissionStore();
    await store.fetchPermissions();

    expect(store.permissionTree).toHaveLength(1);
    expect(store.permissionTree[0]?.children).toHaveLength(1);
    expect(store.hasAnyPermission(['missing', 'system:user:view'])).toBe(true);
    expect(store.hasAllPermissions(['system:user:view', 'system:user:create'])).toBe(true);
  });

  it('ORG-UNIT-001 组织树可扁平化并按 id/code 查询', async () => {
    vi.mocked(organizationApi.getOrganizationTree).mockResolvedValue([
      {
        id: 'o1',
        code: 'HQ',
        name: '总部',
        parentId: null,
        sort: 1,
        status: 1,
        children: [
          {
            id: 'o2',
            code: 'RD',
            name: '研发部',
            parentId: 'o1',
            sort: 1,
            status: 1,
          },
        ],
      },
    ]);

    const store = useOrganizationStore();
    await store.fetchOrganizationTree();

    expect(store.organizations.map((item) => `${item.code}:${item.level}`)).toEqual([
      'HQ:1',
      'RD:2',
    ]);
    expect(store.getOrganizationById('o2')?.name).toBe('研发部');
    expect(store.getOrganizationByCode('HQ')?.name).toBe('总部');
  });
});
