import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import * as permissionApi from '@/api/permission';
import type { PermissionItem } from '@/api/permission';

export const usePermissionStore = defineStore('permission', () => {
  const permissions = ref<PermissionItem[]>([]);
  const loading = ref(false);

  const permissionTree = computed(() => {
    const map = new Map<string, PermissionItem & { children: PermissionItem[] }>();
    const roots: (PermissionItem & { children: PermissionItem[] })[] = [];

    permissions.value.forEach((p) => {
      map.set(p.id, { ...p, children: [] });
    });

    permissions.value.forEach((p) => {
      const node = map.get(p.id)!;
      if (p.parentId && map.has(p.parentId)) {
        map.get(p.parentId)!.children.push(node);
      } else {
        roots.push(node);
      }
    });

    return roots;
  });

  async function fetchPermissions(force = false) {
    if (permissions.value.length > 0 && !force) {
      return permissions.value;
    }

    loading.value = true;
    try {
      const res = await permissionApi.getPermissionList();
      permissions.value = res;
      return res;
    } finally {
      loading.value = false;
    }
  }

  async function createPermission(data: {
    code: string;
    name: string;
    type: 'API' | 'UI_ACTION' | 'DATA' | 'NAV_ACCESS' | 'MENU' | 'BUTTON';
    resourceType: string;
    actionCode?: string | null;
    moduleCode: string;
    status: number;
    remark?: string | null;
  }) {
    const res = await permissionApi.createPermission(data);
    await fetchPermissions(true);
    return res;
  }

  async function updatePermission(
    id: string,
    data: {
      name: string;
      code: string;
      type: 'API' | 'UI_ACTION' | 'DATA' | 'NAV_ACCESS' | 'MENU' | 'BUTTON';
      resourceType: string;
      actionCode?: string | null;
      moduleCode: string;
      status: number;
      remark?: string | null;
    },
  ) {
    await permissionApi.updatePermission(id, data);
    await fetchPermissions(true);
  }

  async function deletePermission(id: string) {
    await permissionApi.deletePermission(id);
    await fetchPermissions(true);
  }

  function hasPermission(code: string): boolean {
    return permissions.value.some((p) => p.code === code);
  }

  function hasAnyPermission(codes: string[]): boolean {
    return codes.some((code) => hasPermission(code));
  }

  function hasAllPermissions(codes: string[]): boolean {
    return codes.every((code) => hasPermission(code));
  }

  function $reset() {
    permissions.value = [];
    loading.value = false;
  }

  return {
    permissions,
    permissionTree,
    loading,
    fetchPermissions,
    createPermission,
    updatePermission,
    deletePermission,
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
    $reset,
  };
});
