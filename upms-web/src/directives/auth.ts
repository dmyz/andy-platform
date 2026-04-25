import type { App, Directive, DirectiveBinding } from 'vue'
import { usePermissionStore } from '@/stores/permission'
import { usePermission } from '@/composables/usePermission'

/**
 * 动作权限指令
 * 示例: <button v-auth="'system:user:create'">新增用户</button>
 * 支持多个权限点(或关系): <button v-auth="['system:user:create', 'system:user:update']">编辑</button>
 */
const authDirective: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const { value } = binding
    const { hasAnyPermission, isAdmin } = usePermission()

    // 超级管理员跳过权限校验
    if (isAdmin.value) {
      return
    }

    if (value) {
      const requiredPermissions = Array.isArray(value) ? value : [value]
      const hasPermission = hasAnyPermission(requiredPermissions)

      if (!hasPermission) {
        el.parentNode?.removeChild(el)
      }
    }
    else {
      throw new Error(`需要提供权限码! 例如 v-auth="'system:user:view'"`)
    }
  },
}

export function setupAuthDirective(app: App) {
  app.directive('auth', authDirective)
}
