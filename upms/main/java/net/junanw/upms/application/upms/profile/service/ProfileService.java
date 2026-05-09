package net.junanw.upms.application.upms.profile.service;

import net.junanw.upms.infrastructure.shared.api.PageResponse;
import net.junanw.upms.application.upms.profile.model.request.ProfileAvatarUpdateRequest;
import net.junanw.upms.application.upms.profile.model.request.ProfileEmailChangeRequest;
import net.junanw.upms.application.upms.profile.model.view.ProfileLoginAuditItem;
import net.junanw.upms.application.upms.profile.model.view.ProfileMeView;
import net.junanw.upms.application.upms.profile.model.view.ProfileMessageItem;
import net.junanw.upms.application.upms.profile.model.request.ProfileMobileChangeRequest;
import net.junanw.upms.application.upms.profile.model.request.ProfileUpdateRequest;
import net.junanw.upms.application.upms.profile.model.view.ProfileVerificationCodeSendView;
/**
 * ProfileService 服务接口。
 *
 * <p>定义 Profile 相关业务能力边界。
 */

/**
 * 个人中心服务接口。
 *
 * <p>定义个人资料读取、资料更新、联系方式变更与个人消息查询能力。
 */
public interface ProfileService {

    /** 查询个人资料。 */
    ProfileMeView me(String username);

    /** 更新个人资料。 */
    ProfileMeView updateMe(String username, ProfileUpdateRequest request);

    /** 更新头像。 */
    ProfileMeView updateAvatar(String username, ProfileAvatarUpdateRequest request);

    /** 发送更换手机号验证码。 */
    ProfileVerificationCodeSendView sendMobileChangeCode(String username);

    /** 发送更换邮箱验证码。 */
    ProfileVerificationCodeSendView sendEmailChangeCode(String username);

    /** 更换手机号。 */
    ProfileMeView changeMobile(String username, ProfileMobileChangeRequest request);

    /** 更换邮箱。 */
    ProfileMeView changeEmail(String username, ProfileEmailChangeRequest request);

    /** 查询个人登录审计分页。 */
    PageResponse<ProfileLoginAuditItem> loginAuditPage(String username, int pageNum, int pageSize);

    /** 查询个人消息分页。 */
    PageResponse<ProfileMessageItem> messagePage(String username, int pageNum, int pageSize);
}
