-- 描述：security_hardening
-- 作者：宇宙星星
-- 日期：2026-05-04

-- 清理历史登录审计中可能保存的明文 token，后续版本只写入不可逆 token 指纹。
UPDATE `audit_login_event`
SET `session_key` = NULL
WHERE `session_key` IS NOT NULL;
