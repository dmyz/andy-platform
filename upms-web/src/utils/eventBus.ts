/**
 * 事件监听器类型（接收事件数据）
 */
export type Listener<T = any> = (data: T) => void

/**
 * 通配符监听器类型（接收事件类型和数据）
 */
export type WildcardListener = (type: string, data: any) => void

/**
 * 事件总线类，基于原生 EventTarget 实现
 */
export class EventBus {
  private target: EventTarget
  private wildcardListeners: Set<WildcardListener>
  private wrappedListeners: Map<Listener, EventListener>

  constructor() {
    this.target = new EventTarget()
    this.wildcardListeners = new Set()
    this.wrappedListeners = new Map()
  }

  /**
   * 注册事件监听
   * @param type 事件类型，'*' 表示监听所有事件
   * @param handler 回调函数
   */
  on(type: string, handler: Listener): void
  on(type: '*', handler: WildcardListener): void
  on(type: string, handler: any): void {
    if (type === '*') {
      this.wildcardListeners.add(handler)
      return
    }

    // 包装用户回调，从 CustomEvent 中提取 detail 作为参数
    const wrapped: EventListener = (event: Event) => {
      handler((event as CustomEvent).detail)
    }
    this.wrappedListeners.set(handler, wrapped)
    this.target.addEventListener(type, wrapped)
  }

  /**
   * 移除事件监听
   * @param type 事件类型，'*' 表示移除通配符监听
   * @param handler 要移除的回调函数（若不传则移除该类型所有监听？mitt 需要指定函数，这里保持 mitt 风格，必须传 handler）
   */
  off(type: string, handler: Listener): void
  off(type: '*', handler: WildcardListener): void
  off(type: string, handler: any): void {
    if (type === '*') {
      this.wildcardListeners.delete(handler)
      return
    }

    const wrapped = this.wrappedListeners.get(handler)
    if (wrapped) {
      this.target.removeEventListener(type, wrapped)
      this.wrappedListeners.delete(handler)
    }
  }

  /**
   * 触发事件
   * @param type 事件类型
   * @param data 传递给监听器的数据
   */
  emit(type: string, data?: any): void {
    // 触发普通监听器
    const event = new CustomEvent(type, { detail: data })
    this.target.dispatchEvent(event)

    // 触发通配符监听器
    this.wildcardListeners.forEach(listener => listener(type, data))
  }

  /**
   * 注册一次性事件监听
   * @param type 事件类型，'*' 表示一次性监听所有事件
   * @param handler 回调函数
   */
  once(type: string, handler: Listener): void
  once(type: '*', handler: WildcardListener): void
  once(type: string, handler: any): void {
    if (type === '*') {
      // 通配符一次性：自己包装，触发后自动移除
      const wrapper: WildcardListener = (t, d) => {
        handler(t, d)
        this.off('*', wrapper)
      }
      this.on('*', wrapper)
      return
    }

    // 普通一次性：利用原生 once 选项，同时管理包装函数的映射
    const wrapped: EventListener = (event: Event) => {
      handler((event as CustomEvent).detail)
      // 原生 once 已移除监听器，这里清理映射
      this.wrappedListeners.delete(handler)
    }
    this.wrappedListeners.set(handler, wrapped)
    this.target.addEventListener(type, wrapped, { once: true })
  }
}

// default EventBus;
export default new EventBus()

/*
// 使用示例
const bus = new EventBus();

// 普通事件
bus.on('foo', data => {
  console.log('foo:', data);
});

// 通配符事件
bus.on('*', (type, data) => {
  console.log(`wildcard ${type}:`, data);
});

// 一次性事件
bus.once('bar', data => {
  console.log('bar (once):', data);
});

bus.emit('foo', 123);
// 输出：
// foo: 123
// wildcard foo: 123

bus.emit('bar', 'hello');
// 输出：
// bar (once): hello
// wildcard bar: hello

bus.emit('bar', 'world'); // 无输出（一次性已移除）

// 移除监听
const handler = data => console.log('baz:', data);
bus.on('baz', handler);
bus.off('baz', handler);
bus.emit('baz', 'test'); // 无输出
*/
