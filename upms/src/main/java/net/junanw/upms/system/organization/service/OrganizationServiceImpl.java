package net.junanw.upms.system.organization.service;

import com.mybatisflex.core.query.QueryWrapper;
import net.junanw.upms.foundation.shared.exception.BusinessException;
import net.junanw.upms.foundation.shared.util.ServiceSupport;
import net.junanw.upms.foundation.shared.id.IdGenerator;
import net.junanw.upms.system.iam.account.entity.AccountEntity;
import net.junanw.upms.system.iam.account.model.AccountType;
import net.junanw.upms.system.iam.user.entity.UserEntity;
import net.junanw.upms.system.iam.account.mapper.AccountMapper;
import net.junanw.upms.system.iam.user.mapper.UserMapper;
import net.junanw.upms.system.organization.model.view.OrganizationDetailView;
import net.junanw.upms.system.organization.model.view.OrganizationMemberItem;
import net.junanw.upms.system.organization.model.request.OrganizationSaveRequest;
import net.junanw.upms.system.organization.model.view.OrganizationTreeNode;
import net.junanw.upms.system.organization.entity.OrganizationMembershipEntity;
import net.junanw.upms.system.organization.entity.OrganizationUnitEntity;
import net.junanw.upms.system.organization.repository.OrganizationMembershipMapper;
import net.junanw.upms.system.organization.repository.OrganizationUnitMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * OrganizationServiceImpl 服务实现。
 *
 * <p>负责承接 Organization 相关业务编排与规则落地。
 */
@Service
@Primary
public class OrganizationServiceImpl extends ServiceSupport implements OrganizationService {

    private final OrganizationUnitMapper orgUnitMapper;
    private final OrganizationMembershipMapper orgMembershipMapper;
    private final UserMapper iamUserMapper;
    private final AccountMapper iamAccountMapper;
    private final IdGenerator idGenerator;

    public OrganizationServiceImpl(
            OrganizationUnitMapper orgUnitMapper,
            OrganizationMembershipMapper orgMembershipMapper,
            UserMapper iamUserMapper,
            AccountMapper iamAccountMapper,
            IdGenerator idGenerator
    ) {
        this.orgUnitMapper = orgUnitMapper;
        this.orgMembershipMapper = orgMembershipMapper;
        this.iamUserMapper = iamUserMapper;
        this.iamAccountMapper = iamAccountMapper;
        this.idGenerator = idGenerator;
    }

    /**
     * 查询组织树。
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrganizationTreeNode> tree(String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);

        QueryWrapper query = QueryWrapper.create()
                .where("deleted = false");

        // 将关键词过滤推到 SQL 层
        if (!normalizedKeyword.isBlank()) {
            query.and((Consumer<QueryWrapper>) wrapper -> wrapper.where("LOWER(org_name) LIKE {0}", "%" + normalizedKeyword + "%")
                    .or("LOWER(org_code) LIKE {0}", "%" + normalizedKeyword + "%"));
        }

        query.orderBy("sort_order ASC, updated_at DESC");

        List<OrganizationUnitEntity> entities = orgUnitMapper.selectListByQuery(query);

        Map<Long, List<OrganizationUnitEntity>> childrenMap = new HashMap<>();
        entities.forEach(item -> childrenMap.computeIfAbsent(item.getParentId(), ignored -> new ArrayList<>()).add(item));
        childrenMap.values().forEach(list -> list.sort(Comparator.comparing(OrganizationUnitEntity::getSortOrder).thenComparing(OrganizationUnitEntity::getId)));
        return childrenMap.getOrDefault(null, List.of()).stream().map(item -> toTree(item, childrenMap)).toList();
    }

    /**
     * 查询组织详情。
     */
    @Override
    @Transactional(readOnly = true)
    public OrganizationDetailView detail(String id) {
        return toDetail(require(parseId(id, "组织不存在")));
    }

    /**
     * 创建组织。
     */
    @Override
    @Transactional
    public OrganizationDetailView create(OrganizationSaveRequest request) {
        Long parentId = request.parentId() == null || request.parentId().isBlank() ? null : parseId(request.parentId(), "上级组织不存在");
        OrganizationUnitEntity parent = parentId == null ? null : require(parentId);
        validate(request, null, parent);
        OrganizationUnitEntity entity = new OrganizationUnitEntity();
        entity.setId(idGenerator.nextId());
        fill(entity, request, parent);
        orgUnitMapper.insert(entity);
        return toDetail(entity);
    }

    /**
     * 更新组织。
     */
    @Override
    @Transactional
    public OrganizationDetailView update(String id, OrganizationSaveRequest request) {
        Long orgId = parseId(id, "组织不存在");
        OrganizationUnitEntity entity = require(orgId);
        Long parentId = request.parentId() == null || request.parentId().isBlank() ? null : parseId(request.parentId(), "上级组织不存在");
        if (parentId != null && parentId.equals(orgId)) {
            throw new BusinessException(400, "上级组织不能选择自己");
        }
        OrganizationUnitEntity parent = parentId == null ? null : require(parentId);
        if (parent != null) {
            ensureNotDescendant(parent.getId(), orgId);
        }
        validate(request, orgId, parent);
        fill(entity, request, parent);
        orgUnitMapper.update(entity);
        return toDetail(entity);
    }

    /**
     * 删除组织。
     *
     * <p>删除前会校验是否存在子组织或成员。
     */
    @Override
    @Transactional
    public void delete(String id) {
        Long orgId = parseId(id, "组织不存在");
        OrganizationUnitEntity entity = require(orgId);

        // 检查是否有子组织
        QueryWrapper childQuery = QueryWrapper.create()
                .where("parent_id = {0}", orgId)
                .and("deleted = false");
        if (orgUnitMapper.selectCountByQuery(childQuery) > 0) {
            throw new BusinessException(400, "存在下级组织，不能删除");
        }

        // 检查是否有活跃成员
        QueryWrapper memberQuery = QueryWrapper.create()
                .where("org_id = {0}", orgId)
                .and("status = {0}", "ACTIVE");
        if (orgMembershipMapper.selectCountByQuery(memberQuery) > 0) {
            throw new BusinessException(400, "组织下仍有成员，不能删除");
        }

        entity.setDeleted(true);
        orgUnitMapper.update(entity);
    }

    /**
     * 更新组织状态。
     */
    @Override
    @Transactional
    public void updateStatus(String id, Integer status) {
        OrganizationUnitEntity entity = require(parseId(id, "组织不存在"));
        entity.setStatus(normalizeStatus(status));
        orgUnitMapper.update(entity);
    }

    /**
     * 查询组织成员。
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrganizationMemberItem> members(String id, String displayName, Integer status) {
        Long orgId = parseId(id, "组织不存在");
        require(orgId);
        String normalizedName = normalizeKeyword(displayName);

        // 查询组织成员关系
        QueryWrapper memberQuery = QueryWrapper.create()
                .where("org_id = {0}", orgId);
        if (status != null) {
            memberQuery.and("status = {0}", "ACTIVE");
        }
        List<OrganizationMembershipEntity> memberships = orgMembershipMapper.selectListByQuery(memberQuery);

        List<Long> userIds = memberships.stream().map(OrganizationMembershipEntity::getUserId).distinct().toList();
        if (userIds.isEmpty()) {
            return List.of();
        }

        // 查询用户信息，将 displayName 和 status 过滤推到 SQL 层
        QueryWrapper userQuery = QueryWrapper.create()
                .where("id IN ({0})", userIds)
                .and("deleted = false");
        if (!normalizedName.isBlank()) {
            userQuery.and("LOWER(display_name) LIKE {0}", "%" + normalizedName + "%");
        }
        if (status != null) {
            userQuery.and("status = {0}", normalizeStatus(status));
        }
        Map<Long, UserEntity> userMap = iamUserMapper.selectListByQuery(userQuery).stream()
                .collect(Collectors.toMap(UserEntity::getId, Function.identity(), (left, right) -> left));

        // 查询账号信息
        QueryWrapper accountQuery = QueryWrapper.create()
                .where("user_id IN ({0})", userIds);
        Map<Long, List<AccountEntity>> accountMap = iamAccountMapper.selectListByQuery(accountQuery).stream()
                .collect(Collectors.groupingBy(AccountEntity::getUserId));

        return memberships.stream()
                .map(item -> {
                    UserEntity user = userMap.get(item.getUserId());
                    if (user == null) {
                        return null;
                    }
                    Map<String, String> accountTypeMap = accountMap.getOrDefault(user.getId(), List.of()).stream()
                            .collect(Collectors.toMap(AccountEntity::getAccountType, AccountEntity::getIdentifier, (left, right) -> left));
                    return new OrganizationMemberItem(
                            String.valueOf(user.getId()),
                            accountTypeMap.getOrDefault(AccountType.USERNAME.name(), ""),
                            user.getDisplayName(),
                            accountTypeMap.get(AccountType.MOBILE.name()),
                            accountTypeMap.get(AccountType.EMAIL.name()),
                            item.getPositionName(),
                            toNumericStatus(user.getStatus())
                    );
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(OrganizationMemberItem::displayName))
                .toList();
    }

    /**
     * 校验组织请求。
     */
    private void validate(OrganizationSaveRequest request, Long currentId, OrganizationUnitEntity parent) {
        QueryWrapper query = QueryWrapper.create()
                .where("org_code = {0}", request.code().trim())
                .and("deleted = false");
        if (currentId != null) {
            query.and("id != {0}", currentId);
        }
        if (orgUnitMapper.selectCountByQuery(query) > 0) {
            throw new BusinessException(400, "组织编码已存在");
        }
        if (parent != null && !"ACTIVE".equalsIgnoreCase(parent.getStatus())) {
            throw new BusinessException(400, "上级组织状态不可用");
        }
    }

    /**
     * 填充组织实体字段。
     */
    private void fill(OrganizationUnitEntity entity, OrganizationSaveRequest request, OrganizationUnitEntity parent) {
        entity.setParentId(parent == null ? null : parent.getId());
        entity.setOrgName(request.name().trim());
        entity.setOrgCode(request.code().trim());
        entity.setLeaderUserId(resolveLeaderUserId(request.leader()));
        entity.setLevelNo(parent == null ? 1 : parent.getLevelNo() + 1);
        entity.setSortOrder(request.sort());
        entity.setStatus(normalizeStatus(request.status()));
        entity.setRemark(trimToNull(request.remark()));
        entity.setOrgFullName(parent == null ? entity.getOrgName() : parent.getOrgFullName() + "/" + entity.getOrgName());
    }

    /**
     * 校验上级组织不是当前节点后代。
     */
    private void ensureNotDescendant(Long parentId, Long orgId) {
        Long current = parentId;
        while (current != null) {
            if (current.equals(orgId)) {
                throw new BusinessException(400, "上级组织不能选择当前节点的下级组织");
            }
            QueryWrapper query = QueryWrapper.create()
                    .where("id = {0}", current)
                    .and("deleted = false");
            OrganizationUnitEntity entity = orgUnitMapper.selectOneByQuery(query);
            current = entity != null ? entity.getParentId() : null;
        }
    }

    /**
     * 解析负责人用户 ID。
     */
    private Long resolveLeaderUserId(String leader) {
        String normalized = trimToNull(leader);
        if (normalized == null) {
            return null;
        }

        // 先按用户名查找
        QueryWrapper accountQuery = QueryWrapper.create()
                .where("account_type = {0}", AccountType.USERNAME.name())
                .and("normalized_identifier = {0}", normalized.toLowerCase());
        AccountEntity account = iamAccountMapper.selectOneByQuery(accountQuery);
        if (account != null) {
            return account.getUserId();
        }

        // 再按显示名称查找
        QueryWrapper userQuery = QueryWrapper.create()
                .where("display_name = {0}", normalized)
                .and("deleted = false");
        UserEntity user = iamUserMapper.selectOneByQuery(userQuery);
        return user != null ? user.getId() : null;
    }

    /**
     * 加载组织实体，不存在则抛异常。
     */
    private OrganizationUnitEntity require(Long id) {
        QueryWrapper query = QueryWrapper.create()
                .where("id = {0}", id)
                .and("deleted = false");
        OrganizationUnitEntity entity = orgUnitMapper.selectOneByQuery(query);
        if (entity == null) {
            throw new BusinessException(404, "组织不存在");
        }
        return entity;
    }

    /**
     * 转换为组织树节点。
     */
    private OrganizationTreeNode toTree(OrganizationUnitEntity entity, Map<Long, List<OrganizationUnitEntity>> childrenMap) {
        return new OrganizationTreeNode(
                String.valueOf(entity.getId()),
                entity.getParentId() == null ? null : String.valueOf(entity.getParentId()),
                entity.getOrgName(),
                entity.getOrgCode(),
                toNumericStatus(entity.getStatus()),
                entity.getSortOrder(),
                childrenMap.getOrDefault(entity.getId(), List.of()).stream().map(child -> toTree(child, childrenMap)).toList()
        );
    }

    /**
     * 转换为组织详情视图。
     */
    private OrganizationDetailView toDetail(OrganizationUnitEntity entity) {
        OrganizationUnitEntity parent = null;
        if (entity.getParentId() != null) {
            QueryWrapper query = QueryWrapper.create()
                    .where("id = {0}", entity.getParentId())
                    .and("deleted = false");
            parent = orgUnitMapper.selectOneByQuery(query);
        }
        return new OrganizationDetailView(
                String.valueOf(entity.getId()),
                entity.getParentId() == null ? null : String.valueOf(entity.getParentId()),
                parent == null ? null : parent.getOrgName(),
                entity.getOrgName(),
                entity.getOrgCode(),
                resolveLeaderName(entity.getLeaderUserId()),
                entity.getLevelNo(),
                entity.getSortOrder(),
                toNumericStatus(entity.getStatus()),
                entity.getRemark()
        );
    }

    /**
     * 解析负责人姓名。
     */
    private String resolveLeaderName(Long leaderUserId) {
        if (leaderUserId == null) {
            return null;
        }
        QueryWrapper query = QueryWrapper.create()
                .where("id = {0}", leaderUserId)
                .and("deleted = false");
        UserEntity user = iamUserMapper.selectOneByQuery(query);
        return user != null ? user.getDisplayName() : null;
    }
}
