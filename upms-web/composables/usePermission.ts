import { useUserStore } from '@/stores/user';

export function usePermission() {
  const userStore = useUserStore();

  const normalizeRole = (roleCode: string) => roleCode.trim().toUpperCase();

  const hasRole = (roleCode: string): boolean => {
    const target = normalizeRole(roleCode).replace(/^ROLE_/, '');
    return (
      userStore.userInfo?.roles?.some((role) => {
        const current = normalizeRole(role).replace(/^ROLE_/, '');
        return current === target;
      }) ?? false
    );
  };

  const hasAnyRole = (roleCodes: string[]): boolean => {
    return roleCodes.some((code) => hasRole(code));
  };

  const hasAllRoles = (roleCodes: string[]): boolean => {
    return roleCodes.every((code) => hasRole(code));
  };

  const isAdmin = computed(() => hasRole('ADMIN') || hasRole('SUPER_ADMIN'));

  const hasPermission = (code: string): boolean => {
    return isAdmin.value || userStore.hasPermission(code);
  };

  const hasAnyPermission = (codes: string[]): boolean => {
    return isAdmin.value || codes.some((code) => userStore.hasPermission(code));
  };

  const hasAllPermissions = (codes: string[]): boolean => {
    return isAdmin.value || codes.every((code) => userStore.hasPermission(code));
  };

  return {
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
    hasRole,
    hasAnyRole,
    hasAllRoles,
    isAdmin,
  };
}
