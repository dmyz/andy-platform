import { computed } from 'vue'
import { usePermissionStore } from '@/stores/permission'
import { useUserStore } from '@/stores/user'

export function usePermission() {
  const permissionStore = usePermissionStore()
  const userStore = useUserStore()

  const hasPermission = (code: string): boolean => {
    return permissionStore.hasPermission(code)
  }

  const hasAnyPermission = (codes: string[]): boolean => {
    return permissionStore.hasAnyPermission(codes)
  }

  const hasAllPermissions = (codes: string[]): boolean => {
    return permissionStore.hasAllPermissions(codes)
  }

  const hasRole = (roleCode: string): boolean => {
    return userStore.userInfo?.roles?.some(r => r.code === roleCode) ?? false
  }

  const hasAnyRole = (roleCodes: string[]): boolean => {
    return roleCodes.some(code => hasRole(code))
  }

  const hasAllRoles = (roleCodes: string[]): boolean => {
    return roleCodes.every(code => hasRole(code))
  }

  const isAdmin = computed(() => hasRole('ADMIN') || hasRole('SUPER_ADMIN'))

  return {
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
    hasRole,
    hasAnyRole,
    hasAllRoles,
    isAdmin,
  }
}
