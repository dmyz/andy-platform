// import type { MockMethod } from 'vite-plugin-mock'
// import { loginAuditList, messageList, mockProfile, paginate, resolveSession, unauthorizedResponse } from './auth'

// type AnnouncementRecord = {
//   id: string
//   title: string
//   type: 'SYSTEM' | 'NOTICE' | 'FEATURE'
//   status: 'DRAFT' | 'PUBLISHED' | 'REVOKED'
//   top: boolean
//   targetType: 'ALL' | 'ORG' | 'ROLE' | 'USER'
//   targetValue: string | null
//   creatorName: string
//   publishTime: string | null
//   updateTime: string
//   content: string
//   read: boolean
// }

// type FileRecord = {
//   id: string
//   fileName: string
//   fileType: string
//   fileSize: number
//   uploaderName: string
//   categoryCode: string | null
//   remark: string | null
//   uploadTime: string
// }

// type OperationAuditRecord = {
//   id: string
//   operatorName: string
//   moduleName: string
//   actionType: 'CREATE' | 'UPDATE' | 'DELETE' | 'ASSIGN_PERMISSION'
//   requestMethod: 'POST' | 'PUT' | 'DELETE'
//   requestUri: string
//   requestParams: string
//   durationMs: number
//   responseCode: number
//   result: 'SUCCESS' | 'FAIL'
//   errorMessage: string | null
//   operationTime: string
// }

// function now() {
//   return new Date().toISOString().slice(0, 19).replace('T', ' ')
// }

// function toTimestamp(value?: string | null) {
//   if (!value)
//     return 0
//   const resolved = Date.parse(value)
//   return Number.isNaN(resolved) ? 0 : resolved
// }

// function resolveCurrentSession(headers: Record<string, string | undefined>) {
//   return resolveSession(headers)
// }

// function requireSession(headers: Record<string, string | undefined>) {
//   const session = resolveCurrentSession(headers)
//   if (!session)
//     return { session: null, response: unauthorizedResponse() }
//   return { session, response: null }
// }

// function matchesRange(value: string | null | undefined, startTime?: string, endTime?: string) {
//   const current = toTimestamp(value)
//   if (!current)
//     return !startTime && !endTime
//   if (startTime && current < toTimestamp(startTime))
//     return false
//   if (endTime && current > toTimestamp(endTime))
//     return false
//   return true
// }

// function createCsv(rows: string[][]) {
//   return rows
//     .map(columns => columns.map(column => `"${String(column ?? '').replaceAll('"', '""')}"`).join(','))
//     .join('\n')
// }

// let announcementList: AnnouncementRecord[] = [
//   {
//     id: '16001',
//     title: '系统升级通知',
//     type: 'SYSTEM',
//     status: 'PUBLISHED',
//     top: true,
//     targetType: 'ALL',
//     targetValue: null,
//     creatorName: '超级管理员',
//     publishTime: '2026-04-02 15:41:45',
//     updateTime: '2026-04-02 15:41:45',
//     content: '今晚 23:00 至 23:30 进行平台升级维护，请提前保存数据。',
//     read: false,
//   },
//   {
//     id: '16002',
//     title: '权限模型优化上线',
//     type: 'FEATURE',
//     status: 'PUBLISHED',
//     top: false,
//     targetType: 'ROLE',
//     targetValue: 'admin',
//     creatorName: '超级管理员',
//     publishTime: '2026-04-01 21:41:45',
//     updateTime: '2026-04-01 21:41:45',
//     content: '导航访问规则与动作权限已解耦，角色授权后即时生效。',
//     read: true,
//   },
//   {
//     id: '16003',
//     title: '组织架构调整说明',
//     type: 'NOTICE',
//     status: 'DRAFT',
//     top: false,
//     targetType: 'ORG',
//     targetValue: '10110',
//     creatorName: '超级管理员',
//     publishTime: null,
//     updateTime: '2026-03-31 18:20:00',
//     content: '技术中心与产品部协同流程已更新，请按新流程执行。',
//     read: false,
//   },
// ]

// let fileList: FileRecord[] = [
//   {
//     id: 'file-1',
//     fileName: 'platform-prd.pdf',
//     fileType: 'application/pdf',
//     fileSize: 834521,
//     uploaderName: '超级管理员',
//     categoryCode: 'document',
//     remark: '平台 PRD',
//     uploadTime: '2026-04-02 11:20:00',
//   },
//   {
//     id: 'file-2',
//     fileName: 'navigation-design.png',
//     fileType: 'image/png',
//     fileSize: 284210,
//     uploaderName: '超级管理员',
//     categoryCode: 'image',
//     remark: '导航设计稿',
//     uploadTime: '2026-04-01 16:05:00',
//   },
//   {
//     id: 'file-3',
//     fileName: 'seed-data.txt',
//     fileType: 'text/plain',
//     fileSize: 4096,
//     uploaderName: '张三',
//     categoryCode: 'document',
//     remark: '初始化说明',
//     uploadTime: '2026-03-31 09:30:00',
//   },
// ]

// let operationAuditList: OperationAuditRecord[] = [
//   {
//     id: 'op-1',
//     operatorName: '超级管理员',
//     moduleName: '用户管理',
//     actionType: 'UPDATE',
//     requestMethod: 'PUT',
//     requestUri: '/api/admin/user/1',
//     requestParams: '{"realName":"超级管理员"}',
//     durationMs: 48,
//     responseCode: 200,
//     result: 'SUCCESS',
//     errorMessage: null,
//     operationTime: '2026-04-02 14:32:00',
//   },
//   {
//     id: 'op-2',
//     operatorName: '超级管理员',
//     moduleName: '角色管理',
//     actionType: 'ASSIGN_PERMISSION',
//     requestMethod: 'POST',
//     requestUri: '/api/admin/role/1/permissions',
//     requestParams: '{"permissionCodes":["system:user:view"]}',
//     durationMs: 65,
//     responseCode: 200,
//     result: 'SUCCESS',
//     errorMessage: null,
//     operationTime: '2026-04-02 13:20:00',
//   },
//   {
//     id: 'op-3',
//     operatorName: '超级管理员',
//     moduleName: '组织管理',
//     actionType: 'CREATE',
//     requestMethod: 'POST',
//     requestUri: '/api/admin/org',
//     requestParams: '{"name":"技术中心"}',
//     durationMs: 52,
//     responseCode: 200,
//     result: 'SUCCESS',
//     errorMessage: null,
//     operationTime: '2026-04-01 17:18:00',
//   },
// ]

// function appendOperationAudit(record: Omit<OperationAuditRecord, 'id' | 'operationTime'>) {
//   operationAuditList.unshift({
//     ...record,
//     id: `op-${Date.now()}`,
//     operationTime: now(),
//   })
// }

// function buildAnnouncementPageItem(item: AnnouncementRecord) {
//   return {
//     id: item.id,
//     title: item.title,
//     type: item.type,
//     status: item.status,
//     top: item.top,
//     targetType: item.targetType,
//     targetValue: item.targetValue,
//     creatorName: item.creatorName,
//     publishTime: item.publishTime,
//     updateTime: item.updateTime,
//   }
// }

// function buildInboxItem(item: AnnouncementRecord) {
//   return {
//     id: item.id,
//     title: item.title,
//     type: item.type,
//     publishTime: item.publishTime || item.updateTime,
//     read: item.read,
//     top: item.top,
//     content: item.content,
//   }
// }

// function buildDashboardShortcut() {
//   return [
//     { label: '用户管理', path: '/system/user', icon: 'user', color: '#0052d9' },
//     { label: '角色管理', path: '/system/role', icon: 'usergroup-add', color: '#00a870' },
//     { label: '公告中心', path: '/announcement/manage', icon: 'bell', color: '#ed7b2f' },
//     { label: '文件管理', path: '/file/manage', icon: 'file', color: '#722ed1' },
//     { label: '登录审计', path: '/audit/login', icon: 'monitor', color: '#2f54eb' },
//     { label: '系统配置', path: '/system/setting', icon: 'setting', color: '#14b8a6' },
//   ]
// }

// export default [
//   {
//     url: '/api/admin/dashboard/summary',
//     method: 'get',
//     response: ({ headers }: any) => {
//       const { session, response } = requireSession(headers)
//       if (!session)
//         return response
//       return {
//         code: 0,
//         message: 'success',
//         data: {
//           username: mockProfile.username,
//           displayName: mockProfile.realName,
//           orgName: mockProfile.orgName || '平台管理组',
//           roleName: '超级管理员',
//           loginTime: session.loginTime,
//         },
//       }
//     },
//   },
//   {
//     url: '/api/admin/dashboard/favorite-navigations',
//     method: 'get',
//     response: ({ headers }: any) => {
//       const { session, response } = requireSession(headers)
//       if (!session)
//         return response
//       return { code: 0, message: 'success', data: buildDashboardShortcut() }
//     },
//   },
//   {
//     url: '/api/admin/dashboard/announcements',
//     method: 'get',
//     response: ({ headers }: any) => {
//       const { session, response } = requireSession(headers)
//       if (!session)
//         return response
//       const items = announcementList
//         .filter(item => item.status === 'PUBLISHED')
//         .sort((left, right) => Number(right.top) - Number(left.top) || toTimestamp(right.publishTime) - toTimestamp(left.publishTime))
//         .slice(0, 5)
//         .map(item => ({
//           id: item.id,
//           title: item.title,
//           publishTime: item.publishTime || item.updateTime,
//           top: item.top,
//           type: item.type.toLowerCase(),
//         }))
//       return { code: 0, message: 'success', data: items }
//     },
//   },
//   {
//     url: '/api/admin/dashboard/recent-operations',
//     method: 'get',
//     response: ({ headers }: any) => {
//       const { session, response } = requireSession(headers)
//       if (!session)
//         return response
//       const items = operationAuditList.slice(0, 6).map(item => ({
//         id: item.id,
//         operationTime: item.operationTime,
//         module: item.moduleName,
//         action: item.actionType,
//         result: item.result,
//       }))
//       return { code: 0, message: 'success', data: items }
//     },
//   },
//   {
//     url: '/api/admin/announcement/page',
//     method: 'get',
//     response: ({ headers, query }: any) => {
//       const { session, response } = requireSession(headers)
//       if (!session)
//         return response
//       const pageNum = Number(query.pageNum || 1)
//       const pageSize = Number(query.pageSize || 10)
//       const items = announcementList
//         .filter(item => !query.title || item.title.includes(query.title))
//         .filter(item => !query.type || item.type === query.type)
//         .filter(item => !query.status || item.status === query.status)
//         .sort((left, right) => toTimestamp(right.updateTime) - toTimestamp(left.updateTime))
//         .map(buildAnnouncementPageItem)
//       return { code: 0, message: 'success', data: paginate(items, pageNum, pageSize) }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/announcement/[^/]+$'),
//     method: 'get',
//     response: ({ headers, url }: any) => {
//       const { session, response } = requireSession(headers)
//       if (!session)
//         return response
//       const id = url.split('/').pop()
//       const item = announcementList.find(current => current.id === id)
//       if (!item)
//         return { code: 404, message: '公告不存在', data: null }
//       return {
//         code: 0,
//         message: 'success',
//         data: {
//           ...buildAnnouncementPageItem(item),
//           content: item.content,
//         },
//       }
//     },
//   },
//   {
//     url: '/api/admin/announcement',
//     method: 'post',
//     response: ({ headers, body }: any) => {
//       const { session, response } = requireSession(headers)
//       if (!session)
//         return response
//       const record: AnnouncementRecord = {
//         id: `ann-${Date.now()}`,
//         title: body.title,
//         type: body.type,
//         status: 'DRAFT',
//         top: Boolean(body.top),
//         targetType: body.targetType,
//         targetValue: body.targetValue || null,
//         creatorName: mockProfile.realName,
//         publishTime: null,
//         updateTime: now(),
//         content: body.content,
//         read: false,
//       }
//       announcementList.unshift(record)
//       appendOperationAudit({
//         operatorName: mockProfile.realName,
//         moduleName: '公告管理',
//         actionType: 'CREATE',
//         requestMethod: 'POST',
//         requestUri: '/api/admin/announcement',
//         requestParams: JSON.stringify(body),
//         durationMs: 40,
//         responseCode: 200,
//         result: 'SUCCESS',
//         errorMessage: null,
//       })
//       return { code: 0, message: 'success', data: buildAnnouncementPageItem(record) }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/announcement/[^/]+$'),
//     method: 'put',
//     response: ({ headers, url, body }: any) => {
//       const { session, response } = requireSession(headers)
//       if (!session)
//         return response
//       const id = url.split('/').pop()
//       const index = announcementList.findIndex(item => item.id === id)
//       if (index < 0)
//         return { code: 404, message: '公告不存在', data: null }
//       announcementList[index] = {
//         ...announcementList[index],
//         title: body.title,
//         type: body.type,
//         top: Boolean(body.top),
//         targetType: body.targetType,
//         targetValue: body.targetValue || null,
//         content: body.content,
//         updateTime: now(),
//       }
//       appendOperationAudit({
//         operatorName: mockProfile.realName,
//         moduleName: '公告管理',
//         actionType: 'UPDATE',
//         requestMethod: 'PUT',
//         requestUri: `/api/admin/announcement/${id}`,
//         requestParams: JSON.stringify(body),
//         durationMs: 45,
//         responseCode: 200,
//         result: 'SUCCESS',
//         errorMessage: null,
//       })
//       return { code: 0, message: 'success', data: buildAnnouncementPageItem(announcementList[index]) }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/announcement/[^/]+$'),
//     method: 'delete',
//     response: ({ headers, url }: any) => {
//       const { session, response } = requireSession(headers)
//       if (!session)
//         return response
//       const id = url.split('/').pop()
//       announcementList = announcementList.filter(item => item.id !== id)
//       appendOperationAudit({
//         operatorName: mockProfile.realName,
//         moduleName: '公告管理',
//         actionType: 'DELETE',
//         requestMethod: 'DELETE',
//         requestUri: `/api/admin/announcement/${id}`,
//         requestParams: '{}',
//         durationMs: 32,
//         responseCode: 200,
//         result: 'SUCCESS',
//         errorMessage: null,
//       })
//       return { code: 0, message: 'success', data: null }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/announcement/[^/]+/publish$'),
//     method: 'post',
//     response: ({ headers, url }: any) => {
//       const { session, response } = requireSession(headers)
//       if (!session)
//         return response
//       const id = url.split('/')[4]
//       const item = announcementList.find(current => current.id === id)
//       if (!item)
//         return { code: 404, message: '公告不存在', data: null }
//       item.status = 'PUBLISHED'
//       item.publishTime = now()
//       item.updateTime = item.publishTime
//       appendOperationAudit({
//         operatorName: mockProfile.realName,
//         moduleName: '公告管理',
//         actionType: 'UPDATE',
//         requestMethod: 'POST',
//         requestUri: `/api/admin/announcement/${id}/publish`,
//         requestParams: '{}',
//         durationMs: 29,
//         responseCode: 200,
//         result: 'SUCCESS',
//         errorMessage: null,
//       })
//       return { code: 0, message: 'success', data: null }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/announcement/[^/]+/revoke$'),
//     method: 'post',
//     response: ({ headers, url }: any) => {
//       const { session, response } = requireSession(headers)
//       if (!session)
//         return response
//       const id = url.split('/')[4]
//       const item = announcementList.find(current => current.id === id)
//       if (!item)
//         return { code: 404, message: '公告不存在', data: null }
//       item.status = 'REVOKED'
//       item.updateTime = now()
//       appendOperationAudit({
//         operatorName: mockProfile.realName,
//         moduleName: '公告管理',
//         actionType: 'UPDATE',
//         requestMethod: 'POST',
//         requestUri: `/api/admin/announcement/${id}/revoke`,
//         requestParams: '{}',
//         durationMs: 26,
//         responseCode: 200,
//         result: 'SUCCESS',
//         errorMessage: null,
//       })
//       return { code: 0, message: 'success', data: null }
//     },
//   },
//   {
//     url: '/api/admin/announcement/my/page',
//     method: 'get',
//     response: ({ headers, query }: any) => {
//       const { session, response } = requireSession(headers)
//       if (!session)
//         return response
//       const pageNum = Number(query.pageNum || 1)
//       const pageSize = Number(query.pageSize || 10)
//       const items = announcementList
//         .filter(item => item.status === 'PUBLISHED')
//         .filter(item => !query.title || item.title.includes(query.title))
//         .filter(item => query.read === undefined || item.read === (query.read === true || query.read === 'true'))
//         .sort((left, right) => Number(right.top) - Number(left.top) || toTimestamp(right.publishTime) - toTimestamp(left.publishTime))
//         .map(buildInboxItem)
//       return { code: 0, message: 'success', data: paginate(items, pageNum, pageSize) }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/announcement/[^/]+/read$'),
//     method: 'post',
//     response: ({ headers, url }: any) => {
//       const { session, response } = requireSession(headers)
//       if (!session)
//         return response
//       const id = url.split('/')[4]
//       const item = announcementList.find(current => current.id === id)
//       if (!item)
//         return { code: 404, message: '公告不存在', data: null }
//       item.read = true
//       const message = messageList.find(current => current.id === id)
//       if (message)
//         message.read = true
//       return { code: 0, message: 'success', data: null }
//     },
//   },
//   {
//     url: '/api/admin/file/page',
//     method: 'get',
//     response: ({ headers, query }: any) => {
//       const { session, response } = requireSession(headers)
//       if (!session)
//         return response
//       const pageNum = Number(query.pageNum || 1)
//       const pageSize = Number(query.pageSize || 10)
//       const items = fileList
//         .filter(item => !query.fileName || item.fileName.includes(query.fileName))
//         .filter(item => !query.fileType || item.fileType.includes(query.fileType))
//         .filter(item => !query.uploaderName || item.uploaderName.includes(query.uploaderName))
//         .filter(item => matchesRange(item.uploadTime, query.startTime, query.endTime))
//         .sort((left, right) => toTimestamp(right.uploadTime) - toTimestamp(left.uploadTime))
//       return { code: 0, message: 'success', data: paginate(items, pageNum, pageSize) }
//     },
//   },
//   {
//     url: '/api/admin/file/upload',
//     method: 'post',
//     response: ({ headers }: any) => {
//       const { session, response } = requireSession(headers)
//       if (!session)
//         return response
//       const record: FileRecord = {
//         id: `file-${Date.now()}`,
//         fileName: `mock-upload-${Date.now()}.dat`,
//         fileType: 'application/octet-stream',
//         fileSize: 1024,
//         uploaderName: mockProfile.realName,
//         categoryCode: 'document',
//         remark: 'mock 上传文件',
//         uploadTime: now(),
//       }
//       fileList.unshift(record)
//       appendOperationAudit({
//         operatorName: mockProfile.realName,
//         moduleName: '文件管理',
//         actionType: 'CREATE',
//         requestMethod: 'POST',
//         requestUri: '/api/admin/file/upload',
//         requestParams: '{}',
//         durationMs: 58,
//         responseCode: 200,
//         result: 'SUCCESS',
//         errorMessage: null,
//       })
//       return { code: 0, message: 'success', data: record }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/file/[^/]+/preview$'),
//     method: 'get',
//     response: ({ headers, url }: any) => {
//       const { session, response } = requireSession(headers)
//       if (!session)
//         return response
//       const id = url.split('/')[4]
//       const item = fileList.find(current => current.id === id)
//       if (!item)
//         return { code: 404, message: '文件不存在', data: null }
//       const previewUrl = item.fileType.startsWith('image/')
//         ? 'https://tdesign.gtimg.com/site/source/avatar.png'
//         : item.fileType === 'application/pdf'
//           ? 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf'
//           : 'https://example.com/mock-preview.txt'
//       return { code: 0, message: 'success', data: { previewUrl } }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/file/[^/]+/download$'),
//     method: 'get',
//     rawResponse: async (req, res) => {
//       const authorization = (req.headers.authorization || req.headers.Authorization || '') as string
//       const token = authorization.startsWith('Bearer ') ? authorization.slice(7) : ''
//       if (!token || !resolveCurrentSession({ authorization: `Bearer ${token}` })) {
//         res.statusCode = 401
//         res.setHeader('Content-Type', 'application/json;charset=UTF-8')
//         res.end(JSON.stringify(unauthorizedResponse()))
//         return
//       }
//       const id = req.url?.split('/')[4]
//       const item = fileList.find(current => current.id === id)
//       if (!item) {
//         res.statusCode = 404
//         res.setHeader('Content-Type', 'application/json;charset=UTF-8')
//         res.end(JSON.stringify({ code: 404, message: '文件不存在', data: null }))
//         return
//       }
//       res.setHeader('Content-Type', 'application/octet-stream')
//       res.setHeader('Content-Disposition', `attachment; filename="${encodeURIComponent(item.fileName)}"`)
//       res.end(`mock download content for ${item.fileName}`)
//     },
//   },
//   {
//     url: new RegExp('/api/admin/file/[^/]+$'),
//     method: 'delete',
//     response: ({ headers, url }: any) => {
//       const { session, response } = requireSession(headers)
//       if (!session)
//         return response
//       const id = url.split('/').pop()
//       fileList = fileList.filter(item => item.id !== id)
//       appendOperationAudit({
//         operatorName: mockProfile.realName,
//         moduleName: '文件管理',
//         actionType: 'DELETE',
//         requestMethod: 'DELETE',
//         requestUri: `/api/admin/file/${id}`,
//         requestParams: '{}',
//         durationMs: 34,
//         responseCode: 200,
//         result: 'SUCCESS',
//         errorMessage: null,
//       })
//       return { code: 0, message: 'success', data: null }
//     },
//   },
//   {
//     url: '/api/admin/login-audit/page',
//     method: 'get',
//     response: ({ headers, query }: any) => {
//       const { session, response } = requireSession(headers)
//       if (!session)
//         return response
//       const pageNum = Number(query.pageNum || 1)
//       const pageSize = Number(query.pageSize || 10)
//       const items = loginAuditList
//         .map(item => ({
//           id: item.id,
//           username: mockProfile.username,
//           realName: mockProfile.realName,
//           loginType: item.loginType,
//           ip: item.ip,
//           browser: item.browser,
//           loginTime: item.loginTime,
//           result: item.result,
//           failureReason: item.result === 'FAIL' ? '认证失败' : undefined,
//         }))
//         .filter(item => !query.username || item.username.includes(query.username))
//         .filter(item => !query.loginType || item.loginType === query.loginType)
//         .filter(item => !query.result || item.result === query.result)
//         .filter(item => matchesRange(item.loginTime, query.startTime, query.endTime))
//         .sort((left, right) => toTimestamp(right.loginTime) - toTimestamp(left.loginTime))
//       return { code: 0, message: 'success', data: paginate(items, pageNum, pageSize) }
//     },
//   },
//   {
//     url: '/api/admin/login-audit/export',
//     method: 'get',
//     rawResponse: async (req, res) => {
//       const authorization = (req.headers.authorization || req.headers.Authorization || '') as string
//       const token = authorization.startsWith('Bearer ') ? authorization.slice(7) : ''
//       if (!token || !resolveCurrentSession({ authorization: `Bearer ${token}` })) {
//         res.statusCode = 401
//         res.setHeader('Content-Type', 'application/json;charset=UTF-8')
//         res.end(JSON.stringify(unauthorizedResponse()))
//         return
//       }
//       const rows = [
//         ['username', 'realName', 'loginType', 'ip', 'browser', 'loginTime', 'result', 'failureReason'],
//         ...loginAuditList.map(item => [
//           mockProfile.username,
//           mockProfile.realName,
//           item.loginType,
//           item.ip,
//           item.browser,
//           item.loginTime,
//           item.result,
//           item.result === 'FAIL' ? '认证失败' : '',
//         ]),
//       ]
//       res.setHeader('Content-Type', 'text/csv;charset=UTF-8')
//       res.setHeader('Content-Disposition', 'attachment; filename="login-audit.csv"')
//       res.end(createCsv(rows))
//     },
//   },
//   {
//     url: '/api/admin/operation-audit/page',
//     method: 'get',
//     response: ({ headers, query }: any) => {
//       const { session, response } = requireSession(headers)
//       if (!session)
//         return response
//       const pageNum = Number(query.pageNum || 1)
//       const pageSize = Number(query.pageSize || 10)
//       const items = operationAuditList
//         .filter(item => !query.operatorName || item.operatorName.includes(query.operatorName))
//         .filter(item => !query.moduleName || item.moduleName.includes(query.moduleName))
//         .filter(item => !query.actionType || item.actionType === query.actionType)
//         .filter(item => !query.result || item.result === query.result)
//         .filter(item => matchesRange(item.operationTime, query.startTime, query.endTime))
//         .sort((left, right) => toTimestamp(right.operationTime) - toTimestamp(left.operationTime))
//         .map(item => ({
//           id: item.id,
//           operatorName: item.operatorName,
//           moduleName: item.moduleName,
//           actionType: item.actionType,
//           requestUri: item.requestUri,
//           durationMs: item.durationMs,
//           operationTime: item.operationTime,
//           result: item.result,
//         }))
//       return { code: 0, message: 'success', data: paginate(items, pageNum, pageSize) }
//     },
//   },
//   {
//     url: new RegExp('/api/admin/operation-audit/[^/]+$'),
//     method: 'get',
//     response: ({ headers, url }: any) => {
//       const { session, response } = requireSession(headers)
//       if (!session)
//         return response
//       const id = url.split('/').pop()
//       const item = operationAuditList.find(current => current.id === id)
//       if (!item)
//         return { code: 404, message: '操作审计不存在', data: null }
//       return {
//         code: 0,
//         message: 'success',
//         data: item,
//       }
//     },
//   },
//   {
//     url: '/api/admin/operation-audit/export',
//     method: 'get',
//     rawResponse: async (req, res) => {
//       const authorization = (req.headers.authorization || req.headers.Authorization || '') as string
//       const token = authorization.startsWith('Bearer ') ? authorization.slice(7) : ''
//       if (!token || !resolveCurrentSession({ authorization: `Bearer ${token}` })) {
//         res.statusCode = 401
//         res.setHeader('Content-Type', 'application/json;charset=UTF-8')
//         res.end(JSON.stringify(unauthorizedResponse()))
//         return
//       }
//       const rows = [
//         ['operatorName', 'moduleName', 'actionType', 'requestMethod', 'requestUri', 'durationMs', 'responseCode', 'result', 'operationTime', 'errorMessage'],
//         ...operationAuditList.map(item => [
//           item.operatorName,
//           item.moduleName,
//           item.actionType,
//           item.requestMethod,
//           item.requestUri,
//           String(item.durationMs),
//           String(item.responseCode),
//           item.result,
//           item.operationTime,
//           item.errorMessage || '',
//         ]),
//       ]
//       res.setHeader('Content-Type', 'text/csv;charset=UTF-8')
//       res.setHeader('Content-Disposition', 'attachment; filename="operation-audit.csv"')
//       res.end(createCsv(rows))
//     },
//   },
// ] as MockMethod[]
