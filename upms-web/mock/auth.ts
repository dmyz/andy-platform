// import type { MockMethod } from 'vite-plugin-mock'

// let mockPassword = 'admin'
// const bootstrapPhone = '13800138000'
// const bootstrapEmail = 'admin@example.com'
// const bootstrapCode = '123456'
// export let sessionList = [
//   {
//     sessionKey: 'mock-current-session',
//     username: 'admin',
//     displayName: '超级管理员',
//     orgName: '平台管理组',
//     ip: '127.0.0.1',
//     authType: 'PASSWORD',
//     status: 'ONLINE',
//     loginTime: '2026-04-01 10:00:00',
//     lastAccessTime: '2026-04-01 10:20:00',
//     currentSession: true,
//   },
// ]
// let issuedCodes: Array<{
//   targetType: 'MOBILE' | 'EMAIL'
//   targetValue: string
//   scene: 'LOGIN' | 'RESET_PASSWORD' | 'CHANGE_MOBILE' | 'CHANGE_EMAIL'
//   code: string
//   used: boolean
// }> = []

// export let mockProfile = {
//   id: '1',
//   username: 'admin',
//   realName: '超级管理员',
//   employeeNo: 'ADMIN001',
//   mobile: bootstrapPhone,
//   email: bootstrapEmail,
//   gender: 'UNKNOWN',
//   avatar: 'https://tdesign.gtimg.com/site/avatar.jpg',
//   orgName: '平台管理组',
//   positionName: '平台管理员',
//   remark: 'mock 环境管理员账号',
// }
// let passwordResetRequired = false
// export let loginAuditList = [
//   {
//     id: 'audit-1',
//     loginTime: '2026-04-01T10:00:00',
//     loginType: 'PASSWORD',
//     ip: '127.0.0.1',
//     browser: 'Chrome 134',
//     result: 'SUCCESS',
//   },
// ]
// export let messageList = [
//   {
//     id: '16001',
//     title: '系统升级通知',
//     type: 'SYSTEM',
//     publishTime: '2026-04-02T15:41:45.141',
//     read: false,
//     content: '今晚 23:00 至 23:30 进行平台升级维护，请提前保存数据。',
//   },
//   {
//     id: '16002',
//     title: '权限模型优化上线',
//     type: 'FEATURE',
//     publishTime: '2026-04-01T21:41:45.143',
//     read: true,
//     content: '导航访问规则与动作权限已解耦，角色授权后即时生效。',
//   },
// ]

// function buildLoginSuccess(authType: 'PASSWORD' | 'MOBILE_CODE' | 'EMAIL_CODE') {
//   const accessToken = `mock-token-${Date.now()}`
//   sessionList = sessionList.map(item => ({
//     ...item,
//     currentSession: false,
//   }))
//   sessionList.unshift({
//     sessionKey: accessToken,
//     username: 'admin',
//     displayName: '超级管理员',
//     orgName: '平台管理组',
//     ip: '127.0.0.1',
//     authType,
//     status: 'ONLINE',
//     loginTime: new Date().toISOString(),
//     lastAccessTime: new Date().toISOString(),
//     currentSession: true,
//   })
//   return {
//     code: 0,
//     message: 'success',
//     data: {
//       accessToken,
//       tokenType: 'Bearer',
//       expiresIn: 2592000,
//     },
//   }
// }

// export function resolveToken(headers: Record<string, string | undefined>) {
//   const authorization = headers.authorization || headers.Authorization || ''
//   if (typeof authorization === 'string' && authorization.startsWith('Bearer '))
//     return authorization.slice(7)
//   return ''
// }

// export function resolveSession(headers: Record<string, string | undefined>) {
//   const token = resolveToken(headers)
//   if (!token)
//     return null
//   return sessionList.find(item => item.sessionKey === token) || null
// }

// export function unauthorizedResponse() {
//   return {
//     code: 401,
//     message: '未登录或会话失效',
//     data: null,
//   }
// }

// function normalizeScene(scene: string | undefined) {
//   const resolved = scene || 'LOGIN'
//   if (['LOGIN', 'RESET_PASSWORD', 'CHANGE_MOBILE', 'CHANGE_EMAIL'].includes(resolved))
//     return resolved as 'LOGIN' | 'RESET_PASSWORD' | 'CHANGE_MOBILE' | 'CHANGE_EMAIL'
//   return null
// }

// function issueCode(targetType: 'MOBILE' | 'EMAIL', targetValue: string, scene: 'LOGIN' | 'RESET_PASSWORD' | 'CHANGE_MOBILE' | 'CHANGE_EMAIL') {
//   issuedCodes = issuedCodes.map(item =>
//     item.targetType === targetType && item.targetValue === targetValue && item.scene === scene
//       ? { ...item, used: true }
//       : item,
//   )
//   issuedCodes.unshift({
//     targetType,
//     targetValue,
//     scene,
//     code: bootstrapCode,
//     used: false,
//   })
// }

// function consumeCode(targetType: 'MOBILE' | 'EMAIL', targetValue: string, scene: 'LOGIN' | 'RESET_PASSWORD' | 'CHANGE_MOBILE' | 'CHANGE_EMAIL', code: string) {
//   const match = issuedCodes.find(item => item.targetType === targetType && item.targetValue === targetValue && item.scene === scene && !item.used)
//   if (!match || match.code !== code)
//     return false
//   match.used = true
//   return true
// }

// function maskMobile(mobile: string) {
//   return mobile.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
// }

// function maskEmail(email: string) {
//   const [name, domain] = email.split('@')
//   if (!name || !domain || name.length < 2)
//     return '***'
//   return `${name.slice(0, 1)}***${name.slice(-1)}@${domain}`
// }

// function syncSessionProfile() {
//   sessionList = sessionList.map(item => ({
//     ...item,
//     displayName: mockProfile.realName,
//     orgName: mockProfile.orgName,
//   }))
// }

// export function appendLoginAudit(loginType: string, result: 'SUCCESS' | 'FAIL', _reasonCode?: string | null) {
//   loginAuditList.unshift({
//     id: `audit-${Date.now()}`,
//     loginTime: new Date().toISOString(),
//     loginType,
//     ip: '127.0.0.1',
//     browser: 'Mock Browser',
//     result,
//   })
// }

// export function paginate<T>(items: T[], pageNum: number, pageSize: number) {
//   const start = (pageNum - 1) * pageSize
//   return {
//     list: items.slice(start, start + pageSize),
//     total: items.length,
//     pageNum,
//     pageSize,
//   }
// }

// export default [
//   // 统一登录入口
//   {
//     url: '/api/admin/auth/login',
//     method: 'post',
//     response: ({ body }: any) => {
//       switch (body.grantType) {
//         case 'PASSWORD':
//           if (body.username === 'admin' && body.password === mockPassword) {
//             appendLoginAudit('PASSWORD', 'SUCCESS')
//             return buildLoginSuccess('PASSWORD')
//           }
//           appendLoginAudit('PASSWORD', 'FAIL', '用户名或密码错误')
//           return {
//             code: 400,
//             message: '用户名或密码错误',
//             data: null,
//           }
//         case 'MOBILE_CODE':
//           if (body.mobile === mockProfile.mobile && consumeCode('MOBILE', body.mobile, 'LOGIN', body.code)) {
//             appendLoginAudit('MOBILE_CODE', 'SUCCESS')
//             return buildLoginSuccess('MOBILE_CODE')
//           }
//           appendLoginAudit('MOBILE_CODE', 'FAIL', '验证码错误')
//           return {
//             code: 400,
//             message: '验证码错误',
//             data: null,
//           }
//         case 'EMAIL_CODE':
//           if (body.email === mockProfile.email && consumeCode('EMAIL', body.email, 'LOGIN', body.code)) {
//             appendLoginAudit('EMAIL_CODE', 'SUCCESS')
//             return buildLoginSuccess('EMAIL_CODE')
//           }
//           appendLoginAudit('EMAIL_CODE', 'FAIL', '验证码错误')
//           return {
//             code: 400,
//             message: '验证码错误',
//             data: null,
//           }
//         default:
//           return {
//             code: 400,
//             message: '不支持的登录方式',
//             data: null,
//           }
//       }
//     },
//   },
//   {
//     url: '/api/admin/auth/code/mobile/send',
//     method: 'post',
//     response: ({ body }: any) => {
//       const scene = normalizeScene(body.scene)
//       if (!scene) {
//         return {
//           code: 400,
//           message: 'scene 不支持',
//           data: null,
//         }
//       }
//       if (body.mobile !== mockProfile.mobile) {
//         return {
//           code: 400,
//           message: '手机号未绑定可登录账号',
//           data: null,
//         }
//       }
//       issueCode('MOBILE', body.mobile, scene)
//       return {
//         code: 0,
//         message: 'success',
//         data: {
//           scene,
//           targetType: 'MOBILE',
//           maskedTarget: maskMobile(body.mobile),
//           expireSeconds: 300,
//           devCode: bootstrapCode,
//         },
//       }
//     },
//   },
//   {
//     url: '/api/admin/auth/code/email/send',
//     method: 'post',
//     response: ({ body }: any) => {
//       const scene = normalizeScene(body.scene)
//       if (!scene) {
//         return {
//           code: 400,
//           message: 'scene 不支持',
//           data: null,
//         }
//       }
//       if (body.email !== mockProfile.email) {
//         return {
//           code: 400,
//           message: '邮箱未绑定可登录账号',
//           data: null,
//         }
//       }
//       issueCode('EMAIL', body.email, scene)
//       return {
//         code: 0,
//         message: 'success',
//         data: {
//           scene,
//           targetType: 'EMAIL',
//           maskedTarget: maskEmail(body.email),
//           expireSeconds: 300,
//           devCode: bootstrapCode,
//         },
//       }
//     },
//   },
//   {
//     url: '/api/admin/auth/password/reset',
//     method: 'post',
//     response: ({ body }: any) => {
//       if (!['MOBILE', 'EMAIL'].includes(body.targetType)) {
//         return {
//           code: 400,
//           message: '不支持的目标类型',
//           data: null,
//         }
//       }
//       const expectedTarget = body.targetType === 'MOBILE' ? mockProfile.mobile : mockProfile.email
//       if (body.target !== expectedTarget) {
//         return {
//           code: 404,
//           message: body.targetType === 'MOBILE' ? '手机号未绑定用户' : '邮箱未绑定用户',
//           data: null,
//         }
//       }
//       if (!consumeCode(body.targetType, body.target, 'RESET_PASSWORD', body.code)) {
//         return {
//           code: 400,
//           message: '验证码错误',
//           data: null,
//         }
//       }
//       mockPassword = body.newPassword
//       return {
//         code: 0,
//         message: 'success',
//         data: null,
//       }
//     },
//   },
//   {
//     url: '/api/admin/auth/password/change',
//     method: 'post',
//     response: ({ headers, body }: any) => {
//       const currentSession = resolveSession(headers)
//       if (!currentSession)
//         return unauthorizedResponse()
//       if (body.oldPassword !== mockPassword) {
//         return {
//           code: 400,
//           message: '原密码错误',
//           data: null,
//         }
//       }
//       mockPassword = body.newPassword
//       return {
//         code: 0,
//         message: 'success',
//         data: null,
//       }
//     },
//   },
//   // 获取当前登录用户
//   {
//     url: '/api/admin/auth/current-user',
//     method: 'get',
//     response: ({ headers }: any) => {
//       const session = resolveSession(headers)
//       if (!session)
//         return unauthorizedResponse()
//       return {
//         code: 0,
//         message: 'success',
//         data: {
//           user: {
//             id: mockProfile.id,
//             username: mockProfile.username,
//             displayName: mockProfile.realName,
//             avatar: mockProfile.avatar,
//             roles: ['admin'],
//             passwordResetRequired,
//           },
//           permissions: [
//             'dashboard:view',
//             'system:navigation',
//             'system:user:view',
//             'system:user:create',
//             'system:user:update',
//             'system:user:delete',
//             'system:user:status',
//             'system:user:password:reset',
//             'system:user:role:assign',
//             'system:user:import',
//             'system:user:export',
//             'system:role:view',
//             'system:role:create',
//             'system:role:update',
//             'system:role:delete',
//             'system:role:status',
//             'system:role:permission:assign',
//             'system:role:user:view',
//             'system:org:view',
//             'system:org:create',
//             'system:org:update',
//             'system:org:delete',
//             'system:org:status',
//             'system:org:member:view',
//             'system:navigation:view',
//             'system:navigation:create',
//             'system:navigation:update',
//             'system:navigation:delete',
//             'system:navigation:access:assign',
//             'system:permission:view',
//             'system:permission:create',
//             'system:permission:update',
//             'system:permission:delete',
//             'system:dictionary:view',
//             'system:dictionary:create',
//             'system:dictionary:update',
//             'system:dictionary:delete',
//             'system:dictionary:item:manage',
//             'system:setting:view',
//             'system:setting:create',
//             'system:setting:update',
//             'system:setting:delete',
//             'auth:session:view',
//             'auth:session:offline',
//             'profile:view',
//             'profile:update',
//             'profile:avatar:update',
//             'profile:mobile:update',
//             'profile:email:update',
//             'profile:password:update',
//             'announcement:manage:view',
//             'announcement:manage:create',
//             'announcement:manage:update',
//             'announcement:manage:delete',
//             'announcement:manage:publish',
//             'announcement:manage:revoke',
//             'announcement:inbox:view',
//             'announcement:inbox:read',
//             'file:manage:view',
//             'file:manage:upload',
//             'file:manage:preview',
//             'file:manage:download',
//             'file:manage:delete',
//             'audit:login:view',
//             'audit:login:export',
//             'audit:operation:view',
//             'audit:operation:export',
//           ],
//           navigations: [
//             {
//               id: '1',
//               name: '工作台',
//               type: 'PAGE',
//               routePath: '/dashboard',
//               componentPath: 'views/dashboard/index.vue',
//               icon: 'dashboard',
//               sortOrder: 1,
//             },
//             {
//               id: '2',
//               name: '公告中心',
//               type: 'GROUP',
//               routePath: '/announcement',
//               componentPath: 'Layout',
//               icon: 'bell',
//               sortOrder: 2,
//               children: [
//                 {
//                   id: '21',
//                   name: '公告管理',
//                   type: 'PAGE',
//                   routePath: '/announcement/manage',
//                   componentPath: 'views/announcement/index.vue',
//                   icon: 'bell',
//                   sortOrder: 1,
//                 },
//                 {
//                   id: '22',
//                   name: '消息中心',
//                   type: 'PAGE',
//                   routePath: '/announcement/inbox',
//                   componentPath: 'views/announcement/inbox.vue',
//                   icon: 'bell',
//                   sortOrder: 2,
//                 },
//               ],
//             },
//             {
//               id: '3',
//               name: '文件中心',
//               type: 'PAGE',
//               routePath: '/file/manage',
//               componentPath: 'views/file/index.vue',
//               icon: 'file',
//               sortOrder: 3,
//             },
//             {
//               id: '4',
//               name: '审计中心',
//               type: 'GROUP',
//               routePath: '/audit',
//               componentPath: 'Layout',
//               icon: 'monitor',
//               sortOrder: 4,
//               children: [
//                 {
//                   id: '41',
//                   name: '登录审计',
//                   type: 'PAGE',
//                   routePath: '/audit/login',
//                   componentPath: 'views/audit/login/index.vue',
//                   icon: 'monitor',
//                   sortOrder: 1,
//                 },
//                 {
//                   id: '42',
//                   name: '操作审计',
//                   type: 'PAGE',
//                   routePath: '/audit/operation',
//                   componentPath: 'views/audit/operation/index.vue',
//                   icon: 'monitor',
//                   sortOrder: 2,
//                 },
//               ],
//             },
//             {
//               id: '5',
//               name: '系统管理',
//               type: 'GROUP',
//               routePath: '/system',
//               componentPath: 'Layout',
//               icon: 'setting',
//               sortOrder: 5,
//               children: [
//                 {
//                   id: '51',
//                   name: '用户管理',
//                   type: 'PAGE',
//                   routePath: '/system/user',
//                   componentPath: 'views/system/user/index.vue',
//                   icon: 'user',
//                   sortOrder: 1,
//                 },
//                 {
//                   id: '52',
//                   name: '角色管理',
//                   type: 'PAGE',
//                   routePath: '/system/role',
//                   componentPath: 'views/system/role/index.vue',
//                   icon: 'usergroup-add',
//                   sortOrder: 2,
//                 },
//                 {
//                   id: '53',
//                   name: '导航管理',
//                   type: 'PAGE',
//                   routePath: '/system/navigation',
//                   componentPath: 'views/system/navigation/index.vue',
//                   icon: 'menu-unfold',
//                   sortOrder: 3,
//                 },
//                 {
//                   id: '54',
//                   name: '组织管理',
//                   type: 'PAGE',
//                   routePath: '/system/org',
//                   componentPath: 'views/system/organization/index.vue',
//                   icon: 'usergroup-add',
//                   sortOrder: 4,
//                 },
//                 {
//                   id: '55',
//                   name: '权限定义',
//                   type: 'PAGE',
//                   routePath: '/system/permission',
//                   componentPath: 'views/system/permission/index.vue',
//                   icon: 'secured',
//                   sortOrder: 5,
//                 },
//                 {
//                   id: '56',
//                   name: '字典管理',
//                   type: 'PAGE',
//                   routePath: '/system/dictionary',
//                   componentPath: 'views/system/dictionary/index.vue',
//                   icon: 'catalog',
//                   sortOrder: 6,
//                 },
//                 {
//                   id: '57',
//                   name: '系统配置',
//                   type: 'PAGE',
//                   routePath: '/system/setting',
//                   componentPath: 'views/system/setting/index.vue',
//                   icon: 'setting',
//                   sortOrder: 7,
//                 },
//                 {
//                   id: '58',
//                   name: '在线会话',
//                   type: 'PAGE',
//                   routePath: '/system/session',
//                   componentPath: 'views/system/session/index.vue',
//                   icon: 'monitor',
//                   sortOrder: 8,
//                 },
//               ],
//             },
//           ],
//         },
//       }
//     },
//   },

//   {
//     url: '/api/admin/profile/me',
//     method: 'get',
//     response: ({ headers }: any) => {
//       const currentSession = resolveSession(headers)
//       if (!currentSession)
//         return unauthorizedResponse()
//       return {
//         code: 0,
//         message: 'success',
//         data: mockProfile,
//       }
//     },
//   },
//   {
//     url: '/api/admin/profile/me',
//     method: 'put',
//     response: ({ headers, body }: any) => {
//       const currentSession = resolveSession(headers)
//       if (!currentSession)
//         return unauthorizedResponse()
//       mockProfile = {
//         ...mockProfile,
//         realName: body.realName,
//         employeeNo: body.employeeNo || null,
//         gender: body.gender || null,
//         orgName: body.orgName || null,
//         positionName: body.positionName || null,
//         remark: body.remark || null,
//       }
//       syncSessionProfile()
//       return {
//         code: 0,
//         message: 'success',
//         data: mockProfile,
//       }
//     },
//   },
//   {
//     url: '/api/admin/profile/avatar',
//     method: 'post',
//     response: ({ headers, body }: any) => {
//       const currentSession = resolveSession(headers)
//       if (!currentSession)
//         return unauthorizedResponse()
//       mockProfile = {
//         ...mockProfile,
//         avatar: body.avatarUrl,
//       }
//       return {
//         code: 0,
//         message: 'success',
//         data: mockProfile,
//       }
//     },
//   },
//   {
//     url: '/api/admin/profile/mobile/change',
//     method: 'post',
//     response: ({ headers, body }: any) => {
//       const currentSession = resolveSession(headers)
//       if (!currentSession)
//         return unauthorizedResponse()
//       if (!consumeCode('MOBILE', mockProfile.mobile, 'CHANGE_MOBILE', body.code)) {
//         return {
//           code: 400,
//           message: '验证码错误',
//           data: null,
//         }
//       }
//       mockProfile = {
//         ...mockProfile,
//         mobile: body.newMobile,
//       }
//       return {
//         code: 0,
//         message: 'success',
//         data: mockProfile,
//       }
//     },
//   },
//   {
//     url: '/api/admin/profile/email/change',
//     method: 'post',
//     response: ({ headers, body }: any) => {
//       const currentSession = resolveSession(headers)
//       if (!currentSession)
//         return unauthorizedResponse()
//       if (!consumeCode('EMAIL', mockProfile.email, 'CHANGE_EMAIL', body.code)) {
//         return {
//           code: 400,
//           message: '验证码错误',
//           data: null,
//         }
//       }
//       mockProfile = {
//         ...mockProfile,
//         email: body.newEmail,
//       }
//       return {
//         code: 0,
//         message: 'success',
//         data: mockProfile,
//       }
//     },
//   },
//   {
//     url: '/api/admin/profile/login-audit/page',
//     method: 'get',
//     response: ({ headers, query }: any) => {
//       const currentSession = resolveSession(headers)
//       if (!currentSession)
//         return unauthorizedResponse()
//       const pageNum = Number(query.pageNum || 1)
//       const pageSize = Number(query.pageSize || 10)
//       return {
//         code: 0,
//         message: 'success',
//         data: paginate(loginAuditList, pageNum, pageSize),
//       }
//     },
//   },
//   {
//     url: '/api/admin/profile/messages/page',
//     method: 'get',
//     response: ({ headers, query }: any) => {
//       const currentSession = resolveSession(headers)
//       if (!currentSession)
//         return unauthorizedResponse()
//       const pageNum = Number(query.pageNum || 1)
//       const pageSize = Number(query.pageSize || 10)
//       return {
//         code: 0,
//         message: 'success',
//         data: paginate(messageList, pageNum, pageSize),
//       }
//     },
//   },
//   {
//     url: '/api/admin/auth/session/page',
//     method: 'get',
//     response: ({ headers, query }: any) => {
//       const currentSession = resolveSession(headers)
//       if (!currentSession)
//         return unauthorizedResponse()
//       const pageNum = Number(query.pageNum || 1)
//       const pageSize = Number(query.pageSize || 10)
//       const username = query.username || ''
//       const displayName = query.displayName || ''
//       const filtered = sessionList
//         .filter(item => item.username.includes(username) && item.displayName.includes(displayName))
//         .map(item => ({
//           ...item,
//           currentSession: item.sessionKey === currentSession.sessionKey,
//         }))
//       return {
//         code: 0,
//         message: 'success',
//         data: paginate(filtered, pageNum, pageSize),
//       }
//     },
//   },
//   {
//     url: '/api/admin/auth/session/offline',
//     method: 'post',
//     response: ({ headers, body }: any) => {
//       const currentSession = resolveSession(headers)
//       if (!currentSession)
//         return unauthorizedResponse()
//       if (body.sessionKey === currentSession.sessionKey) {
//         return {
//           code: 400,
//           message: '不能强制下线当前会话',
//           data: null,
//         }
//       }
//       sessionList = sessionList.filter(item => item.sessionKey !== body.sessionKey)
//       appendLoginAudit('PASSWORD', 'SUCCESS', 'FORCED_OFFLINE')
//       return {
//         code: 0,
//         message: 'success',
//         data: null,
//       }
//     },
//   },
//   // 退出登录
//   {
//     url: '/api/admin/auth/logout',
//     method: 'post',
//     response: ({ headers }: any) => {
//       const currentSession = resolveSession(headers)
//       if (!currentSession)
//         return unauthorizedResponse()
//       sessionList = sessionList.filter(item => item.sessionKey !== currentSession.sessionKey)
//       appendLoginAudit(currentSession.authType, 'SUCCESS', 'LOGOUT')
//       if (sessionList.length > 0) {
//         sessionList = sessionList.map((item, index) => ({
//           ...item,
//           currentSession: index === 0,
//         }))
//       }
//       return {
//         code: 0,
//         message: 'success',
//         data: null,
//       }
//     },
//   },
// ] as MockMethod[]
