import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Organization, OrganizationTreeNode } from '@/types/entities/organization'
import * as organizationApi from '@/api/organization'

export const useOrganizationStore = defineStore('organization', () => {
  const organizations = ref<Organization[]>([])
  const loading = ref(false)

  const organizationTree = computed<OrganizationTreeNode[]>(() => {
    const map = new Map<string, OrganizationTreeNode>()
    const roots: OrganizationTreeNode[] = []

    organizations.value.forEach((org) => {
      map.set(org.id, { ...org, children: [] })
    })

    organizations.value.forEach((org) => {
      const node = map.get(org.id)!
      if (org.parentId && map.has(org.parentId)) {
        const parent = map.get(org.parentId)!
        if (!parent.children) {
          parent.children = []
        }
        parent.children.push(node)
      }
      else {
        roots.push(node)
      }
    })

    return roots
  })

  async function fetchOrganizations(force = false) {
    if (organizations.value.length > 0 && !force) {
      return organizations.value
    }

    loading.value = true
    try {
      const res = await organizationApi.getOrganizationList()
      organizations.value = res.data
      return res.data
    }
    finally {
      loading.value = false
    }
  }

  async function fetchOrganizationTree(force = false) {
    if (organizations.value.length > 0 && !force) {
      return organizationTree.value
    }

    loading.value = true
    try {
      const res = await organizationApi.getOrganizationTree()
      // 扁平化树形结构
      const flatList: Organization[] = []
      const flatten = (nodes: OrganizationTreeNode[]) => {
        nodes.forEach((node) => {
          const { children, ...org } = node
          flatList.push(org)
          if (children && children.length > 0) {
            flatten(children)
          }
        })
      }
      flatten(res.data)
      organizations.value = flatList
      return res.data
    }
    finally {
      loading.value = false
    }
  }

  async function createOrganization(data: {
    code: string
    name: string
    parentId?: string
    leaderUserId?: string
    sortOrder?: number
    status?: 'ACTIVE' | 'INACTIVE'
  }) {
    const res = await organizationApi.createOrganization(data)
    await fetchOrganizations(true)
    return res.data
  }

  async function updateOrganization(id: string, data: {
    name?: string
    leaderUserId?: string
    sortOrder?: number
    status?: 'ACTIVE' | 'INACTIVE'
  }) {
    await organizationApi.updateOrganization(id, data)
    await fetchOrganizations(true)
  }

  async function deleteOrganization(id: string) {
    await organizationApi.deleteOrganization(id)
    await fetchOrganizations(true)
  }

  function getOrganizationById(id: string): Organization | undefined {
    return organizations.value.find(org => org.id === id)
  }

  function getOrganizationByCode(code: string): Organization | undefined {
    return organizations.value.find(org => org.code === code)
  }

  function $reset() {
    organizations.value = []
    loading.value = false
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
  }
})
