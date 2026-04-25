import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Role } from '@/types/entities/role'
import * as roleApi from '@/api/role'

export const useRoleStore = defineStore('role', () => {
  const roles = ref<Role[]>([])
  const loading = ref(false)

  async function fetchRoles(force = false) {
    if (roles.value.length > 0 && !force) {
      return roles.value
    }

    loading.value = true
    try {
      const res = await roleApi.getRoleList()
      roles.value = res.data
      return res.data
    }
    finally {
      loading.value = false
    }
  }

  async function createRole(data: {
    code: string
    name: string
    type: 'SYSTEM' | 'CUSTOM'
    description?: string
  }) {
    const res = await roleApi.createRole(data)
    await fetchRoles(true)
    return res.data
  }

  async function updateRole(id: string, data: {
    code?: string
    name?: string
    type?: 'SYSTEM' | 'CUSTOM'
    description?: string
  }) {
    await roleApi.updateRole(id, data)
    await fetchRoles(true)
  }

  async function deleteRole(id: string) {
    await roleApi.deleteRole(id)
    await fetchRoles(true)
  }

  async function assignPermissions(roleId: string, permissionIds: string[]) {
    await roleApi.assignPermissions(roleId, permissionIds)
  }

  async function getRolePermissions(roleId: string) {
    const res = await roleApi.getRolePermissions(roleId)
    return res.data
  }

  function getRoleById(id: string): Role | undefined {
    return roles.value.find(r => r.id === id)
  }

  function getRoleByCode(code: string): Role | undefined {
    return roles.value.find(r => r.code === code)
  }

  function $reset() {
    roles.value = []
    loading.value = false
  }

  return {
    roles,
    loading,
    fetchRoles,
    createRole,
    updateRole,
    deleteRole,
    assignPermissions,
    getRolePermissions,
    getRoleById,
    getRoleByCode,
    $reset,
  }
})
