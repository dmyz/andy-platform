package net.junanw.upms.core.organization.service;

import cn.xbatis.core.sql.executor.chain.QueryChain;
import net.junanw.upms.infrastructure.shared.exception.BusinessException;
import net.junanw.upms.infrastructure.shared.util.ServiceSupport;
import net.junanw.upms.core.identity.account.entity.AccountEntity;
import net.junanw.upms.core.identity.account.model.AccountType;
import net.junanw.upms.core.identity.user.entity.UserEntity;
import net.junanw.upms.core.identity.account.mapper.AccountMapper;
import net.junanw.upms.core.identity.user.mapper.UserMapper;
import net.junanw.upms.core.organization.model.view.OrganizationDetailView;
import net.junanw.upms.core.organization.model.view.OrganizationMemberItem;
import net.junanw.upms.core.organization.model.request.OrganizationSaveRequest;
import net.junanw.upms.core.organization.model.view.OrganizationTreeNode;
import net.junanw.upms.core.organization.entity.OrganizationMembershipEntity;
import net.junanw.upms.core.organization.entity.OrganizationUnitEntity;
import net.junanw.upms.core.organization.mapper.OrganizationMembershipMapper;
import net.junanw.upms.core.organization.mapper.OrganizationUnitMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    public OrganizationServiceImpl(
            OrganizationUnitMapper orgUnitMapper,
            OrganizationMembershipMapper orgMembershipMapper,
            UserMapper iamUserMapper,
            AccountMapper iamAccountMapper
    ) {
        this.orgUnitMapper = orgUnitMapper;
        this.orgMembershipMapper = orgMembershipMapper;
        this.iamUserMapper = iamUserMapper;
        this.iamAccountMapper = iamAccountMapper;
    }

    /**
     * 查询组织树。
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrganizationTreeNode> tree(String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);

        List<OrganizationUnitEntity> entities = QueryChain.of(orgUnitMapper)
                .eq(OrganizationUnitEntity::getDeleted, false)
                .orderByAsc(OrganizationUnitEntity::getSortOrder)
                .orderByDesc(OrganizationUnitEntity::getUpdatedAt)
                .list();
        if (!normalizedKeyword.isBlank()) {
            entities = filterTreeSearchResult(entities, normalizedKeyword);
        }

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
        fill(entity, request, parent);
        orgUnitMapper.save(entity);
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
        if (QueryChain.of(orgUnitMapper)
                .eq(OrganizationUnitEntity::getParentId, orgId)
                .eq(OrganizationUnitEntity::getDeleted, false)
                .count() > 0) {
            throw new BusinessException(400, "存在下级组织，不能删除");
        }

        // 检查是否有活跃成员
        if (QueryChain.of(orgMembershipMapper)
                .eq(OrganizationMembershipEntity::getOrgId, orgId)
                .eq(OrganizationMembershipEntity::getStatus, "ACTIVE")
                .count() > 0) {
            throw new BusinessException(400, "组织下仍有成员，不能删除");
        }

        orgUnitMapper.deleteById(entity.getId());
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
        QueryChain<OrganizationMembershipEntity> memberQuery = QueryChain.of(orgMembershipMapper)
                .eq(OrganizationMembershipEntity::getOrgId, orgId);
        if (status != null) {
            memberQuery.eq(OrganizationMembershipEntity::getStatus, "ACTIVE");
        }
        List<OrganizationMembershipEntity> memberships = memberQuery.list();

        List<Long> userIds = memberships.stream().map(OrganizationMembershipEntity::getUserId).distinct().toList();
        if (userIds.isEmpty()) {
            return List.of();
        }

        // 查询用户信息，将 displayName 和 status 过滤推到 SQL 层
        QueryChain<UserEntity> userQuery = QueryChain.of(iamUserMapper)
                .in(UserEntity::getId, userIds)
                .eq(UserEntity::getDeleted, false);
        if (!normalizedName.isBlank()) {
            userQuery.like(UserEntity::getDisplayName, normalizedName);
        }
        if (status != null) {
            userQuery.eq(UserEntity::getStatus, normalizeStatus(status));
        }
        Map<Long, UserEntity> userMap = userQuery.list().stream()
                .collect(Collectors.toMap(UserEntity::getId, Function.identity(), (left, right) -> left));

        // 查询账号信息
        Map<Long, List<AccountEntity>> accountMap = QueryChain.of(iamAccountMapper)
                .in(AccountEntity::getUserId, userIds)
                .list().stream()
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
     * 更新用户主组织关系中的岗位名称。
     */
    @Override
    @Transactional
    public void updatePrimaryPosition(Long userId, String positionName) {
        OrganizationMembershipEntity membership = QueryChain.of(orgMembershipMapper)
                .eq(OrganizationMembershipEntity::getUserId, userId)
                .eq(OrganizationMembershipEntity::getPrimaryOrg, true)
                .eq(OrganizationMembershipEntity::getDeleted, false)
                .get();
        if (membership != null) {
            membership.setPositionName(trimToNull(positionName));
            orgMembershipMapper.update(membership);
        }
    }

    /**
     * 校验组织请求。
     */
    private void validate(OrganizationSaveRequest request, Long currentId, OrganizationUnitEntity parent) {
        QueryChain<OrganizationUnitEntity> query = QueryChain.of(orgUnitMapper)
                .eq(OrganizationUnitEntity::getOrgCode, request.code().trim())
                .eq(OrganizationUnitEntity::getDeleted, false);
        if (currentId != null) {
            query.ne(OrganizationUnitEntity::getId, currentId);
        }
        if (query.count() > 0) {
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
            OrganizationUnitEntity entity = QueryChain.of(orgUnitMapper)
                    .eq(OrganizationUnitEntity::getId, current)
                    .eq(OrganizationUnitEntity::getDeleted, false)
                    .get();
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
        AccountEntity account = QueryChain.of(iamAccountMapper)
                .eq(AccountEntity::getAccountType, AccountType.USERNAME.name())
                .eq(AccountEntity::getNormalizedIdentifier, normalized.toLowerCase())
                .get();
        if (account != null) {
            return account.getUserId();
        }

        // 再按显示名称查找
        UserEntity user = QueryChain.of(iamUserMapper)
                .eq(UserEntity::getDisplayName, normalized)
                .eq(UserEntity::getDeleted, false)
                .get();
        return user != null ? user.getId() : null;
    }

    /**
     * 加载组织实体，不存在则抛异常。
     */
    private OrganizationUnitEntity require(Long id) {
        OrganizationUnitEntity entity = QueryChain.of(orgUnitMapper)
                .eq(OrganizationUnitEntity::getId, id)
                .eq(OrganizationUnitEntity::getDeleted, false)
                .get();
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
     * 过滤组织树搜索结果。
     *
     * <p>组织树需要保留命中节点的祖先链，否则二级及以下节点命中后会因为根节点缺失而无法返回。
     */
    private List<OrganizationUnitEntity> filterTreeSearchResult(List<OrganizationUnitEntity> entities, String keyword) {
        Map<Long, OrganizationUnitEntity> entityMap = entities.stream()
                .collect(Collectors.toMap(OrganizationUnitEntity::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Map<Long, List<OrganizationUnitEntity>> descendantsMap = new HashMap<>();
        entities.forEach(item -> descendantsMap.computeIfAbsent(item.getParentId(), ignored -> new ArrayList<>()).add(item));

        Map<Long, OrganizationUnitEntity> visibleMap = new LinkedHashMap<>();
        entities.stream()
                .filter(item -> matchesTreeKeyword(item, keyword))
                .forEach(item -> {
                    addSelfAndAncestors(item, entityMap, visibleMap);
                    addDescendants(item, descendantsMap, visibleMap);
                });
        return new ArrayList<>(visibleMap.values());
    }

    /**
     * 判断组织节点是否命中树搜索关键词。
     */
    private boolean matchesTreeKeyword(OrganizationUnitEntity entity, String keyword) {
        String loweredKeyword = keyword.toLowerCase();
        return containsKeyword(entity.getOrgName(), loweredKeyword)
                || containsKeyword(entity.getOrgCode(), loweredKeyword)
                || containsKeyword(entity.getOrgFullName(), loweredKeyword)
                || containsKeyword(entity.getRemark(), loweredKeyword);
    }

    /**
     * 判断文本是否包含搜索关键词。
     */
    private boolean containsKeyword(String value, String loweredKeyword) {
        return value != null && value.toLowerCase().contains(loweredKeyword);
    }

    /**
     * 将当前节点及祖先节点加入可见结果。
     */
    private void addSelfAndAncestors(
            OrganizationUnitEntity entity,
            Map<Long, OrganizationUnitEntity> entityMap,
            Map<Long, OrganizationUnitEntity> visibleMap
    ) {
        OrganizationUnitEntity current = entity;
        while (current != null) {
            visibleMap.putIfAbsent(current.getId(), current);
            current = current.getParentId() == null ? null : entityMap.get(current.getParentId());
        }
    }

    /**
     * 将当前节点的后代加入可见结果。
     */
    private void addDescendants(
            OrganizationUnitEntity entity,
            Map<Long, List<OrganizationUnitEntity>> descendantsMap,
            Map<Long, OrganizationUnitEntity> visibleMap
    ) {
        for (OrganizationUnitEntity child : descendantsMap.getOrDefault(entity.getId(), List.of())) {
            visibleMap.putIfAbsent(child.getId(), child);
            addDescendants(child, descendantsMap, visibleMap);
        }
    }

    /**
     * 转换为组织详情视图。
     */
    private OrganizationDetailView toDetail(OrganizationUnitEntity entity) {
        OrganizationUnitEntity parent = null;
        if (entity.getParentId() != null) {
            parent = QueryChain.of(orgUnitMapper)
                    .eq(OrganizationUnitEntity::getId, entity.getParentId())
                    .eq(OrganizationUnitEntity::getDeleted, false)
                    .get();
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
        UserEntity user = QueryChain.of(iamUserMapper)
                .eq(UserEntity::getId, leaderUserId)
                .eq(UserEntity::getDeleted, false)
                .get();
        return user != null ? user.getDisplayName() : null;
    }
}
