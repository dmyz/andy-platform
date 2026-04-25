// import type { MockMethod } from 'vite-plugin-mock'

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

// const orgMap: Record<string, string> = {
//   '10100': '总部',
//   '10110': '技术中心',
//   '10111': '前端研发组',
//   '10112': '后端研发组',
//   '10120': '产品部',
//   '10130': '运营部',
// }

// let userList = [
//   { id: '1', username: 'admin', realName: '超级管理员', jobNumber: 'ADMIN001', mobile: '13800138000', email: 'admin@example.com', gender: 'UNKNOWN', orgId: '10100', orgName: '总部', position: '平台管理员', status: 1, remark: '默认管理员', passwordResetRequired: false, lastLoginTime: now(), createTime: now() },
//   { id: '2', username: 'zhangsan', realName: '张三', jobNumber: 'TECH001', mobile: '13800000001', email: 'zhangsan@example.com', gender: 'MALE', orgId: '10110', orgName: '技术中心', position: '架构师', status: 1, remark: '', passwordResetRequired: false, lastLoginTime: now(), createTime: now() },
//   { id: '3', username: 'lisi', realName: '李四', jobNumber: 'PROD001', mobile: '13800000002', email: 'lisi@example.com', gender: 'FEMALE', orgId: '10120', orgName: '产品部', position: '产品经理', status: 1, remark: '', passwordResetRequired: false, lastLoginTime: null, createTime: now() },
//   { id: '4', username: 'wangwu', realName: '王五', jobNumber: 'FE001', mobile: '13800000003', email: 'wangwu@example.com', gender: 'MALE', orgId: '10111', orgName: '前端研发组', position: '前端工程师', status: 0, remark: '', passwordResetRequired: true, lastLoginTime: null, createTime: now() },
// ]

// let userRoles: Record<string, Array<{ code: string, name: string }>> = {
//   '1': [{ code: 'admin', name: '超级管理员' }],
//   '2': [{ code: 'sys_admin', name: '系统管理员' }],
//   '3': [{ code: 'user', name: '普通用户' }],
//   '4': [{ code: 'user', name: '普通用户' }],
// }

// const roleCatalog = {
//   admin: '超级管理员',
//   sys_admin: '系统管理员',
//   user: '普通用户',
// }

// export default [
//   {
//     url: '/api/admin/user/page',
//     method: 'get',
//     response: ({ query }: any) => {
//       const pageNum = Number(query.pageNum || query.current || 1)
//       const pageSize = Number(query.pageSize || 10)
//       const list = userList
//         .filter(item => !query.username || item.username.includes(query.username))
//         .filter(item => !query.realName || item.realName.includes(query.realName))
//         .filter(item => !query.mobile || item.mobile.includes(query.mobile))
//         .filter(item => !query.orgName || item.orgName.includes(query.orgName))
//         .filter(item => query.status === undefined || item.status === Number(query.status))
//         .map(({ gender, remark, passwordResetRequired, ...rest }) => rest)
//       return { code: 0, message: 'success', data: paginate(list, pageNum, pageSize) }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/user/[^/]+/roles$'),
//     method: 'get',
//     response: ({ url }: any) => {
//       const id = url.split('/')[4]
//       return { code: 0, message: 'success', data: userRoles[id] || [] }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/user/[^/]+/roles$'),
//     method: 'post',
//     response: ({ url, body }: any) => {
//       const id = url.split('/')[4]
//       userRoles[id] = (body.roleCodes || []).map((code: string) => ({ code, name: roleCatalog[code as keyof typeof roleCatalog] || code }))
//       return { code: 0, message: 'success', data: null }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/user/[^/]+/password/reset$'),
//     method: 'post',
//     response: ({ url }: any) => {
//       const id = url.split('/')[4]
//       const item = userList.find(user => user.id === id)
//       if (item)
//         item.passwordResetRequired = true
//       return { code: 0, message: 'success', data: null }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/user/[^/]+/offline$'),
//     method: 'post',
//     response: () => ({ code: 0, message: 'success', data: null }),
//   },
//   {
//     url: '/api/admin/user/import',
//     method: 'post',
//     response: () => ({ code: 0, message: 'success', data: { importedCount: 2, updatedCount: 1, skippedCount: 0 } }),
//   },
//   {
//     url: '/api/admin/user/export',
//     method: 'get',
//     rawResponse: async (_req, res) => {
//       res.setHeader('Content-Type', 'text/csv;charset=UTF-8')
//       res.setHeader('Content-Disposition', 'attachment; filename="user-export.csv"')
//       const content = ['username,realName,mobile,orgName,status', ...userList.map(item => `${item.username},${item.realName},${item.mobile},${item.orgName},${item.status}`)].join('\n')
//       res.end(content)
//     },
//   },
//   {
//     url: '/api/admin/user',
//     method: 'post',
//     response: ({ body }: any) => {
//       const item = {
//         id: `${Date.now()}`,
//         username: body.username,
//         realName: body.realName,
//         jobNumber: body.jobNumber || '',
//         mobile: body.mobile,
//         email: body.email || '',
//         gender: body.gender || 'UNKNOWN',
//         orgId: body.orgId,
//         orgName: orgMap[body.orgId] || '未知组织',
//         position: body.position || '',
//         status: body.status ?? 1,
//         remark: body.remark || '',
//         passwordResetRequired: !body.password,
//         lastLoginTime: null,
//         createTime: now(),
//       }
//       userList.unshift(item)
//       return { code: 0, message: 'success', data: item }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/user/[^/]+$'),
//     method: 'get',
//     response: ({ url }: any) => {
//       const id = url.split('/').pop()
//       const item = userList.find(user => user.id === id)
//       return { code: 0, message: 'success', data: item || null }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/user/[^/]+$'),
//     method: 'put',
//     response: ({ url, body }: any) => {
//       const id = url.split('/').pop()
//       const index = userList.findIndex(user => user.id === id)
//       if (index >= 0) {
//         userList[index] = {
//           ...userList[index],
//           ...body,
//           orgName: orgMap[body.orgId] || userList[index].orgName,
//         }
//       }
//       return { code: 0, message: 'success', data: userList[index] || null }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/user/[^/]+$'),
//     method: 'delete',
//     response: ({ url }: any) => {
//       const id = url.split('/').pop()
//       userList = userList.filter(user => user.id !== id)
//       delete userRoles[id || '']
//       return { code: 0, message: 'success', data: null }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/user/[^/]+/status$'),
//     method: 'put',
//     response: ({ url, body }: any) => {
//       const id = url.split('/')[4]
//       const item = userList.find(user => user.id === id)
//       if (item)
//         item.status = body.status
//       return { code: 0, message: 'success', data: null }
//     },
//   },
// ] as MockMethod[]
