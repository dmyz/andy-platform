import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { Organization, OrganizationTreeNode } from '@/types/entities/organization';
import * as organizationApi from '@/api/organization';

export const useOrganizationStore = defineStore('organization', () => {
  const organizations = ref<Organization[]>([]);
  const loading = ref(false);

  const organizationTree = computed<OrganizationTreeNode[]>(() => {
    const map = new Map<string, OrganizationTreeNode>();
    const roots: OrganizationTreeNode[] = [];

    organizations.value.forEach((org) => {
      const {
        parentName: _parentName,
        leader: _leader,
        level: _level,
        remark: _remark,
        ...node
      } = org;
      map.set(org.id, { ...node, children: [] });
    });

    organizations.value.forEach((org) => {
      const node = map.get(org.id)!;
      if (org.parentId && map.has(org.parentId)) {
        const parent = map.get(org.parentId)!;
        if (!parent.children) {
          parent.children = [];
        }
        parent.children.push(node);
      } else {
        roots.push(node);
      }
    });

    return roots;
  });

  function flattenTree(nodes: OrganizationTreeNode[], level = 1): Organization[] {
    return nodes.flatMap((node) => {
      const { children, ...org } = node;
      return [
        {
          ...org,
          level,
        },
        ...flattenTree(children || [], level + 1),
      ];
    });
  }

  async function fetchOrganizations(force = false) {
    if (organizations.value.length > 0 && !force) {
      return organizations.value;
    }

    loading.value = true;
    try {
      const res = await organizationApi.getOrganizationList();
      organizations.value = flattenTree(res);
      return organizations.value;
    } finally {
      loading.value = false;
    }
  }

  async function fetchOrganizationTree(force = false) {
    if (organizations.value.length > 0 && !force) {
      return organizationTree.value;
    }

    loading.value = true;
    try {
      const res = await organizationApi.getOrganizationTree();
      organizations.value = flattenTree(res);
      return res;
    } finally {
      loading.value = false;
    }
  }

  async function createOrganization(data: {
    code: string;
    name: string;
    parentId?: string | null;
    leader?: string | null;
    sort?: number;
    status?: number;
    remark?: string | null;
  }) {
    const res = await organizationApi.createOrganization({
      parentId: data.parentId,
      code: data.code,
      name: data.name,
      leader: data.leader,
      sort: data.sort ?? 1,
      status: data.status ?? 1,
      remark: data.remark,
    });
    await fetchOrganizations(true);
    return res;
  }

  async function updateOrganization(
    id: string,
    data: {
      parentId?: string | null;
      code?: string;
      name?: string;
      leader?: string | null;
      sort?: number;
      status?: number;
      remark?: string | null;
    },
  ) {
    const current = getOrganizationById(id);
    await organizationApi.updateOrganization(id, {
      parentId: data.parentId ?? current?.parentId,
      code: data.code || current?.code || '',
      name: data.name || current?.name || '',
      leader: data.leader ?? current?.leader,
      sort: data.sort ?? current?.sort ?? 1,
      status: data.status ?? current?.status ?? 1,
      remark: data.remark ?? current?.remark,
    });
    await fetchOrganizations(true);
  }

  async function deleteOrganization(id: string) {
    await organizationApi.deleteOrganization(id);
    await fetchOrganizations(true);
  }

  function getOrganizationById(id: string): Organization | undefined {
    return organizations.value.find((org) => org.id === id);
  }

  function getOrganizationByCode(code: string): Organization | undefined {
    return organizations.value.find((org) => org.code === code);
  }

  function $reset() {
    organizations.value = [];
    loading.value = false;
  }

  return {
    organizations,
    organizationTree,
    loading,
    fetchOrganizations,
    fetchOrganizationTree,
    createOrganization,
    updateOrganization,
    deleteOrganization,
    getOrganizationById,
    getOrganizationByCode,
    $reset,
  };
});
