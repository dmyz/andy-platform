import type { App, Directive, DirectiveBinding } from 'vue';
import { useUserStore } from '@/stores/user';

type AuthBindingValue = string | string[] | { all?: string[]; any?: string[] };

function hasAuth(value: AuthBindingValue | undefined) {
  if (!value) {
    return false;
  }
  const userStore = useUserStore();
  if (typeof value === 'string') {
    return userStore.hasPermission(value);
  }
  if (Array.isArray(value)) {
    return value.some((permission) => userStore.hasPermission(permission));
  }
  if (value.all?.length) {
    return value.all.every((permission) => userStore.hasPermission(permission));
  }
  if (value.any?.length) {
    return value.any.some((permission) => userStore.hasPermission(permission));
  }
  return true;
}

function applyAuth(el: HTMLElement, binding: DirectiveBinding<AuthBindingValue>) {
  if (!hasAuth(binding.value)) {
    el.parentNode?.removeChild(el);
  }
}

/**
 * 动作权限指令
 * 示例: <button v-auth="'system:user:create'">新增用户</button>
 * 支持多个权限点(或关系): <button v-auth="['system:user:create', 'system:user:update']">编辑</button>
 */
const authDirective: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding<AuthBindingValue>) {
    applyAuth(el, binding);
  },
  updated(el: HTMLElement, binding: DirectiveBinding<AuthBindingValue>) {
    applyAuth(el, binding);
  },
};

export function setupAuthDirective(app: App) {
  app.directive('auth', authDirective);
}
