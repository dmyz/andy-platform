// import type { MockMethod } from 'vite-plugin-mock'

// type Status = 0 | 1

// function now() {
//   return new Date().toISOString()
// }

// function paginate<T>(items: T[], pageNum = 1, pageSize = 10) {
//   const start = (pageNum - 1) * pageSize
//   return {
//     list: items.slice(start, start + pageSize),
//     total: items.length,
//     pageNum,
//     pageSize,
//   }
// }

// let orgList = [
//   { id: '10100', parentId: null as string | null, name: '总部', code: 'HQ', leader: '超级管理员', sort: 1, status: 1 as Status, remark: '总部节点' },
//   { id: '10110', parentId: '10100', name: '技术中心', code: 'TECH', leader: '技术总监', sort: 1, status: 1 as Status, remark: '技术团队' },
//   { id: '10111', parentId: '10110', name: '前端研发组', code: 'FE', leader: '前端负责人', sort: 1, status: 1 as Status, remark: '前端开发' },
//   { id: '10112', parentId: '10110', name: '后端研发组', code: 'BE', leader: '后端负责人', sort: 2, status: 1 as Status, remark: '后端开发' },
//   { id: '10120', parentId: '10100', name: '产品部', code: 'PRODUCT', leader: '产品负责人', sort: 2, status: 1 as Status, remark: '产品团队' },
//   { id: '10130', parentId: '10100', name: '运营部', code: 'OPERATION', leader: '运营负责人', sort: 3, status: 0 as Status, remark: '运营团队' },
// ]

// let permissionList = [
//   { id: '10501', name: '工作台查看', code: 'dashboard:view', type: 'UI_ACTION', resourceType: 'MENU', actionCode: 'view', moduleCode: 'dashboard', status: 1 as Status, remark: '查看工作台', updateTime: now(), category: '工作台' },
//   { id: '10502', name: '用户查看', code: 'system:user:view', type: 'UI_ACTION', resourceType: 'MENU', actionCode: 'view', moduleCode: 'system', status: 1 as Status, remark: '查看用户', updateTime: now(), category: '系统管理' },
//   { id: '10503', name: '用户新增', code: 'system:user:create', type: 'UI_ACTION', resourceType: 'BUTTON', actionCode: 'create', moduleCode: 'system', status: 1 as Status, remark: '新增用户', updateTime: now(), category: '系统管理' },
//   { id: '10504', name: '用户编辑', code: 'system:user:update', type: 'UI_ACTION', resourceType: 'BUTTON', actionCode: 'update', moduleCode: 'system', status: 1 as Status, remark: '编辑用户', updateTime: now(), category: '系统管理' },
//   { id: '10505', name: '角色查看', code: 'system:role:view', type: 'UI_ACTION', resourceType: 'MENU', actionCode: 'view', moduleCode: 'system', status: 1 as Status, remark: '查看角色', updateTime: now(), category: '系统管理' },
//   { id: '10506', name: '角色授权', code: 'system:role:permission:assign', type: 'UI_ACTION', resourceType: 'BUTTON', actionCode: 'assign', moduleCode: 'system', status: 1 as Status, remark: '角色授权', updateTime: now(), category: '系统管理' },
//   { id: '10507', name: '组织查看', code: 'system:org:view', type: 'UI_ACTION', resourceType: 'MENU', actionCode: 'view', moduleCode: 'system', status: 1 as Status, remark: '查看组织', updateTime: now(), category: '系统管理' },
//   { id: '10508', name: '导航查看', code: 'system:navigation:view', type: 'UI_ACTION', resourceType: 'MENU', actionCode: 'view', moduleCode: 'system', status: 1 as Status, remark: '查看导航', updateTime: now(), category: '系统管理' },
//   { id: '10509', name: '权限查看', code: 'system:permission:view', type: 'UI_ACTION', resourceType: 'MENU', actionCode: 'view', moduleCode: 'system', status: 1 as Status, remark: '查看权限', updateTime: now(), category: '系统管理' },
//   { id: '10510', name: '字典查看', code: 'system:dictionary:view', type: 'UI_ACTION', resourceType: 'MENU', actionCode: 'view', moduleCode: 'system', status: 1 as Status, remark: '查看字典', updateTime: now(), category: '系统管理' },
//   { id: '10511', name: '配置查看', code: 'system:setting:view', type: 'UI_ACTION', resourceType: 'MENU', actionCode: 'view', moduleCode: 'system', status: 1 as Status, remark: '查看配置', updateTime: now(), category: '系统管理' },
//   { id: '10512', name: '会话查看', code: 'auth:session:view', type: 'UI_ACTION', resourceType: 'MENU', actionCode: 'view', moduleCode: 'auth', status: 1 as Status, remark: '查看会话', updateTime: now(), category: '认证' },
// ]

// let roleList = [
//   { id: '10301', name: '超级管理员', code: 'admin', dataScope: '全部', status: 1 as Status, remark: '平台最高权限', createTime: now(), permissionCodes: permissionList.map(item => item.code), users: ['1'] },
//   { id: '10302', name: '系统管理员', code: 'sys_admin', dataScope: '全部', status: 1 as Status, remark: '系统配置维护', createTime: now(), permissionCodes: permissionList.filter(item => item.moduleCode === 'system' || item.moduleCode === 'auth').map(item => item.code), users: ['2', '3'] },
//   { id: '10303', name: '普通用户', code: 'user', dataScope: '本人', status: 1 as Status, remark: '普通业务用户', createTime: now(), permissionCodes: ['dashboard:view'], users: ['4', '5'] },
// ]

// let navigationList = [
//   { id: '11001', parentId: null as string | null, name: '工作台', type: 'PAGE' as const, routePath: '/dashboard', componentPath: 'views/dashboard/index.vue', externalUrl: null as string | null, icon: 'dashboard', sortOrder: 1, visible: true, status: 1 as Status, permissionCodes: ['dashboard:view'] },
//   { id: '11002', parentId: null as string | null, name: '系统管理', type: 'GROUP' as const, routePath: '/system', componentPath: 'Layout', externalUrl: null as string | null, icon: 'setting', sortOrder: 2, visible: true, status: 1 as Status, permissionCodes: [] },
//   { id: '11021', parentId: '11002', name: '用户管理', type: 'PAGE' as const, routePath: '/system/user', componentPath: 'views/system/user/index.vue', externalUrl: null as string | null, icon: 'user', sortOrder: 1, visible: true, status: 1 as Status, permissionCodes: ['system:user:view'] },
//   { id: '11022', parentId: '11002', name: '角色管理', type: 'PAGE' as const, routePath: '/system/role', componentPath: 'views/system/role/index.vue', externalUrl: null as string | null, icon: 'usergroup-add', sortOrder: 2, visible: true, status: 1 as Status, permissionCodes: ['system:role:view'] },
//   { id: '11023', parentId: '11002', name: '导航管理', type: 'PAGE' as const, routePath: '/system/navigation', componentPath: 'views/system/navigation/index.vue', externalUrl: null as string | null, icon: 'menu-unfold', sortOrder: 3, visible: true, status: 1 as Status, permissionCodes: ['system:navigation:view'] },
//   { id: '11024', parentId: '11002', name: '组织管理', type: 'PAGE' as const, routePath: '/system/org', componentPath: 'views/system/organization/index.vue', externalUrl: null as string | null, icon: 'usergroup-add', sortOrder: 4, visible: true, status: 1 as Status, permissionCodes: ['system:org:view'] },
//   { id: '11025', parentId: '11002', name: '权限定义', type: 'PAGE' as const, routePath: '/system/permission', componentPath: 'views/system/permission/index.vue', externalUrl: null as string | null, icon: 'secured', sortOrder: 5, visible: true, status: 1 as Status, permissionCodes: ['system:permission:view'] },
//   { id: '11026', parentId: '11002', name: '字典管理', type: 'PAGE' as const, routePath: '/system/dictionary', componentPath: 'views/system/dictionary/index.vue', externalUrl: null as string | null, icon: 'catalog', sortOrder: 6, visible: true, status: 1 as Status, permissionCodes: ['system:dictionary:view'] },
//   { id: '11027', parentId: '11002', name: '系统配置', type: 'PAGE' as const, routePath: '/system/setting', componentPath: 'views/system/setting/index.vue', externalUrl: null as string | null, icon: 'setting', sortOrder: 7, visible: true, status: 1 as Status, permissionCodes: ['system:setting:view'] },
// ]

// let dictionaryTypes = [
//   { id: '11201', name: '用户状态', code: 'user_status', status: 1 as Status, remark: '用户启停状态', updateTime: now() },
//   { id: '11202', name: '公告类型', code: 'announcement_type', status: 1 as Status, remark: '公告类型枚举', updateTime: now() },
// ]
// let dictionaryItems = [
//   { id: '11301', dictionaryId: '11201', dictionaryName: '用户状态', dictionaryCode: 'user_status', name: '启用', value: 'ENABLED', sortOrder: 1, status: 1 as Status, remark: '启用状态', updateTime: now() },
//   { id: '11302', dictionaryId: '11201', dictionaryName: '用户状态', dictionaryCode: 'user_status', name: '禁用', value: 'DISABLED', sortOrder: 2, status: 1 as Status, remark: '禁用状态', updateTime: now() },
// ]

// let settingList = [
//   { id: '11401', settingKey: 'platform.login.captcha.enabled', settingName: '登录验证码开关', settingValue: 'false', valueType: 'BOOLEAN', scopeType: 'GLOBAL', scopeId: null as string | null, groupCode: 'LOGIN', secretFlag: false, effectiveMode: 'IMMEDIATE', status: 1 as Status, remark: '默认关闭', updateTime: now() },
//   { id: '11402', settingKey: 'platform.file.max-size-mb', settingName: '文件大小限制', settingValue: '10', valueType: 'NUMBER', scopeType: 'GLOBAL', scopeId: null as string | null, groupCode: 'FILE', secretFlag: false, effectiveMode: 'IMMEDIATE', status: 1 as Status, remark: '单文件 10MB', updateTime: now() },
// ]

// const relatedUsers = [
//   { id: '1', username: 'admin', realName: '超级管理员', orgName: '总部', status: 1 as Status },
//   { id: '2', username: 'zhangsan', realName: '张三', orgName: '技术中心', status: 1 as Status },
//   { id: '3', username: 'lisi', realName: '李四', orgName: '产品部', status: 1 as Status },
//   { id: '4', username: 'wangwu', realName: '王五', orgName: '前端研发组', status: 0 as Status },
// ]

// function buildOrgTree(parentId: string | null = null): any[] {
//   return orgList
//     .filter(item => item.parentId === parentId)
//     .sort((a, b) => a.sort - b.sort)
//     .map(item => ({
//       id: item.id,
//       parentId: item.parentId,
//       name: item.name,
//       code: item.code,
//       status: item.status,
//       sort: item.sort,
//       children: buildOrgTree(item.id),
//     }))
// }

// function findOrg(id: string) {
//   return orgList.find(item => item.id === id) || null
// }

// function orgLevel(id: string): number {
//   let level = 1
//   let current = findOrg(id)
//   while (current?.parentId) {
//     level += 1
//     current = findOrg(current.parentId)
//   }
//   return level
// }

// function buildNavigationTree(parentId: string | null = null): any[] {
//   return navigationList
//     .filter(item => item.parentId === parentId)
//     .sort((a, b) => a.sortOrder - b.sortOrder)
//     .map(item => ({
//       id: item.id,
//       parentId: item.parentId,
//       name: item.name,
//       type: item.type,
//       routePath: item.routePath,
//       componentPath: item.componentPath,
//       externalUrl: item.externalUrl,
//       icon: item.icon,
//       sortOrder: item.sortOrder,
//       visible: item.visible,
//       status: item.status,
//       children: buildNavigationTree(item.id),
//     }))
// }

// export default [
//   {
//     url: '/api/admin/org/tree',
//     method: 'get',
//     response: ({ query }: any) => {
//       const keyword = (query.keyword || '').trim()
//       const tree = buildOrgTree()
//       if (!keyword)
//         return { code: 0, message: 'success', data: tree }
//       const filterTree = (nodes: any[]): any[] => nodes
//         .map(node => ({ ...node, children: filterTree(node.children || []) }))
//         .filter(node => node.name.includes(keyword) || node.code.includes(keyword) || node.children.length > 0)
//       return { code: 0, message: 'success', data: filterTree(tree) }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/org/[^/]+$'),
//     method: 'get',
//     response: ({ url }: any) => {
//       const id = url.split('/').pop()
//       const item = findOrg(id)
//       if (!item)
//         return { code: 404, message: '组织不存在', data: null }
//       const parent = item.parentId ? findOrg(item.parentId) : null
//       return {
//         code: 0,
//         message: 'success',
//         data: {
//           id: item.id,
//           parentId: item.parentId,
//           parentName: parent?.name || null,
//           name: item.name,
//           code: item.code,
//           leader: item.leader,
//           level: orgLevel(item.id),
//           sort: item.sort,
//           status: item.status,
//           remark: item.remark,
//         },
//       }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/org/[^/]+/members$'),
//     method: 'get',
//     response: ({ url, query }: any) => {
//       const id = url.split('/')[4]
//       const users = relatedUsers
//         .filter(item => (id === '10100' ? true : item.orgName.includes(findOrg(id)?.name || '')))
//         .filter(item => !query.displayName || item.realName.includes(query.displayName))
//         .filter(item => query.status === undefined || item.status === Number(query.status))
//         .map(item => ({
//           id: item.id,
//           username: item.username,
//           displayName: item.realName,
//           mobile: '13800000000',
//           email: `${item.username}@example.com`,
//           positionName: '成员',
//           status: item.status,
//         }))
//       return { code: 0, message: 'success', data: users }
//     },
//   },
//   {
//     url: '/api/admin/org',
//     method: 'post',
//     response: ({ body }: any) => {
//       const id = `${Date.now()}`
//       orgList.push({ id, parentId: body.parentId, name: body.name, code: body.code, leader: body.leader, sort: body.sort, status: body.status, remark: body.remark })
//       return {
//         code: 0,
//         message: 'success',
//         data: {
//           id,
//           parentId: body.parentId,
//           parentName: body.parentId ? findOrg(body.parentId)?.name || null : null,
//           name: body.name,
//           code: body.code,
//           leader: body.leader,
//           level: orgLevel(id),
//           sort: body.sort,
//           status: body.status,
//           remark: body.remark,
//         },
//       }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/org/[^/]+$'),
//     method: 'put',
//     response: ({ url, body }: any) => {
//       const id = url.split('/').pop()
//       const index = orgList.findIndex(item => item.id === id)
//       if (index < 0)
//         return { code: 404, message: '组织不存在', data: null }
//       orgList[index] = { ...orgList[index], ...body }
//       const item = orgList[index]
//       return {
//         code: 0,
//         message: 'success',
//         data: {
//           id: item.id,
//           parentId: item.parentId,
//           parentName: item.parentId ? findOrg(item.parentId)?.name || null : null,
//           name: item.name,
//           code: item.code,
//           leader: item.leader,
//           level: orgLevel(item.id),
//           sort: item.sort,
//           status: item.status,
//           remark: item.remark,
//         },
//       }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/org/[^/]+/status$'),
//     method: 'put',
//     response: ({ url, body }: any) => {
//       const id = url.split('/')[4]
//       const item = findOrg(id)
//       if (item)
//         item.status = body.status
//       return { code: 0, message: 'success', data: null }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/org/[^/]+$'),
//     method: 'delete',
//     response: ({ url }: any) => {
//       const id = url.split('/').pop()
//       const hasChildren = orgList.some(item => item.parentId === id)
//       if (hasChildren)
//         return { code: 400, message: '组织下仍有子组织，不能删除', data: null }
//       orgList = orgList.filter(item => item.id !== id)
//       return { code: 0, message: 'success', data: null }
//     },
//   },

//   {
//     url: '/api/admin/role/page',
//     method: 'get',
//     response: ({ query }: any) => {
//       const pageNum = Number(query.pageNum || 1)
//       const pageSize = Number(query.pageSize || 10)
//       const list = roleList
//         .filter(item => !query.name || item.name.includes(query.name))
//         .filter(item => !query.code || item.code.includes(query.code))
//         .filter(item => query.status === undefined || item.status === Number(query.status))
//         .map(item => ({ ...item, permissionCount: item.permissionCodes.length }))
//       return { code: 0, message: 'success', data: paginate(list, pageNum, pageSize) }
//     },
//   },
//   {
//     url: '/api/admin/role/permission/catalog',
//     method: 'get',
//     response: () => ({
//       code: 0,
//       message: 'success',
//       data: permissionList.map(item => ({ code: item.code, name: item.name, type: item.type, category: item.category })),
//     }),
//   },
//   {
//     url: new RegExp('/api/admin/role/[^/]+$'),
//     method: 'get',
//     response: ({ url }: any) => {
//       const id = url.split('/').pop()
//       const item = roleList.find(role => role.id === id)
//       if (!item)
//         return { code: 404, message: '角色不存在', data: null }
//       return { code: 0, message: 'success', data: { id: item.id, name: item.name, code: item.code, dataScope: item.dataScope, status: item.status, remark: item.remark, createTime: item.createTime } }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/role/[^/]+/permissions$'),
//     method: 'get',
//     response: ({ url }: any) => {
//       const id = url.split('/')[4]
//       const item = roleList.find(role => role.id === id)
//       const data = permissionList
//         .filter(permission => item?.permissionCodes.includes(permission.code))
//         .map(permission => ({ code: permission.code, name: permission.name, type: permission.type, category: permission.category }))
//       return { code: 0, message: 'success', data }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/role/[^/]+/users$'),
//     method: 'get',
//     response: ({ url }: any) => {
//       const id = url.split('/')[4]
//       const role = roleList.find(item => item.id === id)
//       const data = relatedUsers.filter(user => role?.users.includes(user.id))
//       return { code: 0, message: 'success', data }
//     },
//   },
//   {
//     url: '/api/admin/role',
//     method: 'post',
//     response: ({ body }: any) => {
//       const item = { id: `${Date.now()}`, name: body.name, code: body.code, dataScope: body.dataScope, status: body.status, remark: body.remark, createTime: now(), permissionCodes: [] as string[], users: [] as string[] }
//       roleList.unshift(item)
//       return { code: 0, message: 'success', data: null }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/role/[^/]+$'),
//     method: 'put',
//     response: ({ url, body }: any) => {
//       const id = url.split('/').pop()
//       const index = roleList.findIndex(item => item.id === id)
//       if (index >= 0)
//         roleList[index] = { ...roleList[index], ...body }
//       return { code: 0, message: 'success', data: null }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/role/[^/]+/permissions$'),
//     method: 'post',
//     response: ({ url, body }: any) => {
//       const id = url.split('/')[4]
//       const role = roleList.find(item => item.id === id)
//       if (role)
//         role.permissionCodes = body.permissionCodes || []
//       return { code: 0, message: 'success', data: null }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/role/[^/]+/status$'),
//     method: 'put',
//     response: ({ url, body }: any) => {
//       const id = url.split('/')[4]
//       const role = roleList.find(item => item.id === id)
//       if (role)
//         role.status = body.status
//       return { code: 0, message: 'success', data: null }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/role/[^/]+$'),
//     method: 'delete',
//     response: ({ url }: any) => {
//       const id = url.split('/').pop()
//       roleList = roleList.filter(item => item.id !== id)
//       return { code: 0, message: 'success', data: null }
//     },
//   },

//   {
//     url: '/api/admin/permission/page',
//     method: 'get',
//     response: ({ query }: any) => {
//       const pageNum = Number(query.pageNum || 1)
//       const pageSize = Number(query.pageSize || 10)
//       const list = permissionList.filter(item => !query.name || item.name.includes(query.name))
//         .filter(item => !query.code || item.code.includes(query.code))
//         .filter(item => !query.type || item.type === query.type)
//         .filter(item => query.status === undefined || item.status === Number(query.status))
//       return { code: 0, message: 'success', data: paginate(list, pageNum, pageSize) }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/permission/[^/]+$'),
//     method: 'get',
//     response: ({ url }: any) => {
//       const id = url.split('/').pop()
//       return { code: 0, message: 'success', data: permissionList.find(item => item.id === id) || null }
//     },
//   },
//   {
//     url: '/api/admin/permission',
//     method: 'post',
//     response: ({ body }: any) => {
//       permissionList.unshift({ id: `${Date.now()}`, ...body, updateTime: now(), category: body.moduleCode || '系统管理' })
//       return { code: 0, message: 'success', data: null }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/permission/[^/]+$'),
//     method: 'put',
//     response: ({ url, body }: any) => {
//       const id = url.split('/').pop()
//       const index = permissionList.findIndex(item => item.id === id)
//       if (index >= 0)
//         permissionList[index] = { ...permissionList[index], ...body, updateTime: now(), category: body.moduleCode || permissionList[index].category }
//       return { code: 0, message: 'success', data: null }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/permission/[^/]+$'),
//     method: 'delete',
//     response: ({ url }: any) => {
//       const id = url.split('/').pop()
//       permissionList = permissionList.filter(item => item.id !== id)
//       return { code: 0, message: 'success', data: null }
//     },
//   },

//   {
//     url: '/api/admin/navigation/tree',
//     method: 'get',
//     response: () => ({ code: 0, message: 'success', data: buildNavigationTree() }),
//   },
//   {
//     url: new RegExp('/api/admin/navigation/[^/]+$'),
//     method: 'get',
//     response: ({ url }: any) => {
//       const id = url.split('/').pop()
//       const item = navigationList.find(nav => nav.id === id)
//       const parent = item?.parentId ? navigationList.find(nav => nav.id === item.parentId) : null
//       return { code: 0, message: 'success', data: item ? { ...item, parentName: parent?.name || null } : null }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/navigation/[^/]+/permissions$'),
//     method: 'get',
//     response: ({ url }: any) => {
//       const id = url.split('/')[4]
//       const item = navigationList.find(nav => nav.id === id)
//       const data = permissionList.filter(permission => item?.permissionCodes.includes(permission.code)).map(permission => ({ code: permission.code, name: permission.name, type: permission.type, category: permission.category }))
//       return { code: 0, message: 'success', data }
//     },
//   },
//   {
//     url: '/api/admin/navigation',
//     method: 'post',
//     response: ({ body }: any) => {
//       navigationList.push({ id: `${Date.now()}`, ...body, permissionCodes: [] })
//       return { code: 0, message: 'success', data: null }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/navigation/[^/]+$'),
//     method: 'put',
//     response: ({ url, body }: any) => {
//       const id = url.split('/').pop()
//       const index = navigationList.findIndex(nav => nav.id === id)
//       if (index >= 0)
//         navigationList[index] = { ...navigationList[index], ...body }
//       return { code: 0, message: 'success', data: null }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/navigation/[^/]+/permissions$'),
//     method: 'post',
//     response: ({ url, body }: any) => {
//       const id = url.split('/')[4]
//       const item = navigationList.find(nav => nav.id === id)
//       if (item)
//         item.permissionCodes = body.permissionCodes || []
//       return { code: 0, message: 'success', data: null }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/navigation/[^/]+$'),
//     method: 'delete',
//     response: ({ url }: any) => {
//       const id = url.split('/').pop()
//       navigationList = navigationList.filter(item => item.id !== id && item.parentId !== id)
//       return { code: 0, message: 'success', data: null }
//     },
//   },

//   {
//     url: '/api/admin/dictionary/page',
//     method: 'get',
//     response: ({ query }: any) => {
//       const pageNum = Number(query.pageNum || 1)
//       const pageSize = Number(query.pageSize || 10)
//       const list = dictionaryTypes.filter(item => !query.name || item.name.includes(query.name)).filter(item => !query.code || item.code.includes(query.code)).filter(item => query.status === undefined || item.status === Number(query.status))
//       return { code: 0, message: 'success', data: paginate(list, pageNum, pageSize) }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/dictionary/[^/]+$'),
//     method: 'get',
//     response: ({ url }: any) => {
//       const id = url.split('/').pop()
//       return { code: 0, message: 'success', data: dictionaryTypes.find(item => item.id === id) || null }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/dictionary/[^/]+/items$'),
//     method: 'get',
//     response: ({ url, query }: any) => {
//       const id = url.split('/')[4]
//       const list = dictionaryItems.filter(item => item.dictionaryId === id).filter(item => !query.name || item.name.includes(query.name)).filter(item => query.status === undefined || item.status === Number(query.status)).sort((a, b) => a.sortOrder - b.sortOrder)
//       return { code: 0, message: 'success', data: list }
//     },
//   },
//   {
//     url: '/api/admin/dictionary',
//     method: 'post',
//     response: ({ body }: any) => {
//       dictionaryTypes.unshift({ id: `${Date.now()}`, ...body, updateTime: now() })
//       return { code: 0, message: 'success', data: null }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/dictionary/[^/]+$'),
//     method: 'put',
//     response: ({ url, body }: any) => {
//       const id = url.split('/').pop()
//       const index = dictionaryTypes.findIndex(item => item.id === id)
//       if (index >= 0)
//         dictionaryTypes[index] = { ...dictionaryTypes[index], ...body, updateTime: now() }
//       return { code: 0, message: 'success', data: null }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/dictionary/[^/]+$'),
//     method: 'delete',
//     response: ({ url }: any) => {
//       const id = url.split('/').pop()
//       dictionaryTypes = dictionaryTypes.filter(item => item.id !== id)
//       dictionaryItems = dictionaryItems.filter(item => item.dictionaryId !== id)
//       return { code: 0, message: 'success', data: null }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/dictionary/[^/]+/items$'),
//     method: 'post',
//     response: ({ url, body }: any) => {
//       const dictionaryId = url.split('/')[4]
//       const type = dictionaryTypes.find(item => item.id === dictionaryId)
//       dictionaryItems.push({ id: `${Date.now()}`, dictionaryId, dictionaryName: type?.name || '', dictionaryCode: type?.code || '', ...body, updateTime: now() })
//       return { code: 0, message: 'success', data: null }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/dictionary/item/[^/]+$'),
//     method: 'put',
//     response: ({ url, body }: any) => {
//       const itemId = url.split('/').pop()
//       const index = dictionaryItems.findIndex(item => item.id === itemId)
//       if (index >= 0)
//         dictionaryItems[index] = { ...dictionaryItems[index], ...body, updateTime: now() }
//       return { code: 0, message: 'success', data: null }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/dictionary/item/[^/]+$'),
//     method: 'delete',
//     response: ({ url }: any) => {
//       const itemId = url.split('/').pop()
//       dictionaryItems = dictionaryItems.filter(item => item.id !== itemId)
//       return { code: 0, message: 'success', data: null }
//     },
//   },

//   {
//     url: '/api/admin/setting/page',
//     method: 'get',
//     response: ({ query }: any) => {
//       const pageNum = Number(query.pageNum || 1)
//       const pageSize = Number(query.pageSize || 10)
//       const list = settingList.filter(item => !query.settingName || item.settingName.includes(query.settingName)).filter(item => !query.settingKey || item.settingKey.includes(query.settingKey)).filter(item => !query.groupCode || item.groupCode === query.groupCode).filter(item => query.status === undefined || item.status === Number(query.status))
//       return { code: 0, message: 'success', data: paginate(list, pageNum, pageSize) }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/setting/[^/]+$'),
//     method: 'get',
//     response: ({ url }: any) => {
//       const id = url.split('/').pop()
//       return { code: 0, message: 'success', data: settingList.find(item => item.id === id) || null }
//     },
//   },
//   {
//     url: '/api/admin/setting',
//     method: 'post',
//     response: ({ body }: any) => {
//       settingList.unshift({ id: `${Date.now()}`, ...body, updateTime: now() })
//       return { code: 0, message: 'success', data: null }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/setting/[^/]+$'),
//     method: 'put',
//     response: ({ url, body }: any) => {
//       const id = url.split('/').pop()
//       const index = settingList.findIndex(item => item.id === id)
//       if (index >= 0)
//         settingList[index] = { ...settingList[index], ...body, updateTime: now() }
//       return { code: 0, message: 'success', data: null }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/setting/[^/]+$'),
//     method: 'delete',
//     response: ({ url }: any) => {
//       const id = url.split('/').pop()
//       settingList = settingList.filter(item => item.id !== id)
//       return { code: 0, message: 'success', data: null }
//     },
//   },
// ] as MockMethod[]
