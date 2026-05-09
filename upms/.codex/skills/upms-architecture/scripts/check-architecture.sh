#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../.." && pwd)"

cd "$PROJECT_ROOT"

echo "检查四层架构约束..."
echo ""

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

ROOT="src/main/java/net/junanw/upms"
TEST_ROOT="src/test/java/net/junanw/upms"
VIOLATIONS=0

report_violation() {
    local title="$1"
    local detail="$2"

    echo -e "${RED}错误：$title${NC}"
    echo "$detail"
    echo ""
    VIOLATIONS=$((VIOLATIONS + 1))
}

check_unexpected_top_dirs() {
    local root="$1"
    local label="$2"

    if [ ! -d "$root" ]; then
        return
    fi

    local unexpected
    unexpected=$(find "$root" -mindepth 1 -maxdepth 1 -type d \
        ! -name core \
        ! -name business \
        ! -name application \
        ! -name infrastructure \
        -print | sort)

    if [ -n "$unexpected" ]; then
        report_violation "$label 存在非四层顶层目录" "$unexpected"
    else
        echo -e "${GREEN}通过：$label 顶层目录只包含四层架构目录${NC}"
    fi
}

check_old_package_refs() {
    local matches
    matches=$(rg "net\\.junanw\\.upms\\.(foundation|system|support|portal|biz)" src/main/java src/test/java 2>/dev/null || true)

    if [ -n "$matches" ]; then
        report_violation "仍存在旧顶层包引用" "$matches"
    else
        echo -e "${GREEN}通过：未发现旧顶层包引用${NC}"
    fi
}

check_controller_persistence_refs() {
    local matches
    matches=$(rg -n "import net\\.junanw\\.upms\\..*\\.(mapper|entity)\\." "$ROOT" --glob '*Controller.java' 2>/dev/null || true)

    if [ -n "$matches" ]; then
        report_violation "Controller 直接依赖 Mapper 或 Entity" "$matches"
    else
        echo -e "${GREEN}通过：Controller 未直接依赖持久层对象${NC}"
    fi
}

check_repository_package_absent() {
    local dirs
    local refs

    dirs=$(find "$ROOT" "$TEST_ROOT" -type d -name repository -print 2>/dev/null | sort || true)
    refs=$(rg -n "net\\.junanw\\.upms\\..*\\.repository\\b|package .*\\.repository\\b|import .*\\.repository\\b" src/main/java src/test/java 2>/dev/null || true)

    if [ -n "$dirs$refs" ]; then
        report_violation "仍存在 repository 包或引用，持久层目录统一使用 mapper" "$dirs"$'\n'"$refs"
    else
        echo -e "${GREEN}通过：持久层目录统一使用 mapper，未发现 repository 包或引用${NC}"
    fi
}

check_admin_controller_location() {
    local matches=""
    local file

    while IFS= read -r file; do
        if [[ "$file" != "$ROOT/application/upms/"* ]]; then
            matches+="$file"$'\n'
        fi
    done < <(rg -l '@(Request|Get|Post|Put|Delete|Patch)Mapping\s*\([^)]*"/admin' "$ROOT" --glob '*Controller.java' 2>/dev/null || true)

    if [ -n "$matches" ]; then
        report_violation "/admin/** Controller 未归入 application.upms" "$matches"
    else
        echo -e "${GREEN}通过：/admin/** Controller 均位于 application.upms${NC}"
    fi
}

check_application_upms_persistence_refs() {
    local matches
    matches=$(rg -n "import (net\\.junanw\\.upms\\..*\\.(mapper|entity)\\.|cn\\.xbatis\\.core\\.sql\\.executor\\.chain\\.QueryChain)" "$ROOT/application/upms" 2>/dev/null || true)

    if [ -n "$matches" ]; then
        report_violation "application.upms 直接依赖持久层对象或 QueryChain" "$matches"
    else
        echo -e "${GREEN}通过：application.upms 未直接依赖持久层对象或 QueryChain${NC}"
    fi
}

check_application_upms_package_layout() {
    local matches=""
    local unexpected_upms=""

    if [ -d "$ROOT/application" ]; then
        matches=$(find "$ROOT/application" -mindepth 1 -maxdepth 1 -type d \
            \( -name admin -o -name personal -o -name portal \) \
            -print | sort)
    fi

    if [ -n "$matches" ]; then
        report_violation "application 存在未收敛到 application.upms 的管理端入口包" "$matches"
    else
        echo -e "${GREEN}通过：application 未出现 admin、personal、portal 平铺入口包${NC}"
    fi

    if [ -d "$ROOT/application/upms" ]; then
        unexpected_upms=$(find "$ROOT/application/upms" -mindepth 1 -maxdepth 1 -type d \
            ! -name auth \
            ! -name identity \
            ! -name organization \
            ! -name dictionary \
            ! -name setting \
            ! -name audit \
            ! -name content \
            ! -name profile \
            ! -name workspace \
            -print | sort)
    fi

    if [ -n "$unexpected_upms" ]; then
        report_violation "application.upms 存在未登记的一级功能场景包" "$unexpected_upms"
    else
        echo -e "${GREEN}通过：application.upms 一级包符合管理端功能场景分包${NC}"
    fi
}

check_disallowed_layer_deps() {
    local matches

    matches=$(rg -n "import net\\.junanw\\.upms\\.(application|business|core)\\." "$ROOT/infrastructure" 2>/dev/null || true)
    if [ -n "$matches" ]; then
        report_violation "infrastructure 反向依赖业务层" "$matches"
    else
        echo -e "${GREEN}通过：infrastructure 未依赖业务层${NC}"
    fi

    matches=$(rg -n "import net\\.junanw\\.upms\\.application\\." "$ROOT/core" "$ROOT/business" 2>/dev/null || true)
    if [ -n "$matches" ]; then
        report_violation "core 或 business 依赖 application" "$matches"
    else
        echo -e "${GREEN}通过：core 和 business 未依赖 application${NC}"
    fi

    matches=$(rg -n "import net\\.junanw\\.upms\\.business\\." "$ROOT/core" 2>/dev/null || true)
    if [ -n "$matches" ]; then
        echo -e "${YELLOW}警告：core 存在 business 依赖，请确认是否通过 Service 接口或应用层编排更合适${NC}"
        echo "$matches"
        echo ""
    else
        echo -e "${GREEN}通过：core 未依赖 business${NC}"
    fi
}

check_mybatisflex_refs() {
    local matches
    matches=$(rg -n "mybatisflex|mybatis-flex|MyBatis-Flex|MyBatisFlex|QueryWrapper|UpdateWrapper|LambdaQueryWrapper|TableDef" \
        src/main/java src/test/java src/main/resources pom.xml 2>/dev/null || true)

    if [ -n "$matches" ]; then
        report_violation "仍存在 MyBatis-Flex 代码或配置引用" "$matches"
    else
        echo -e "${GREEN}通过：未发现 MyBatis-Flex 代码或配置引用${NC}"
    fi
}

echo "检查顶层目录..."
check_unexpected_top_dirs "$ROOT" "主代码"
check_unexpected_top_dirs "$TEST_ROOT" "测试代码"
echo ""

echo "检查旧包引用..."
check_old_package_refs
echo ""

echo "检查 Controller 依赖..."
check_controller_persistence_refs
check_repository_package_absent
check_admin_controller_location
check_application_upms_persistence_refs
check_application_upms_package_layout
echo ""

echo "检查四层依赖方向..."
check_disallowed_layer_deps
echo ""

echo "检查旧持久层实现回归..."
check_mybatisflex_refs
echo ""

echo "模块统计："
for dir in core business application infrastructure; do
    count=$(find "$ROOT/$dir" -type d 2>/dev/null | wc -l | tr -d ' ')
    echo "  $dir: $count 个目录"
done
echo ""

if [ "$VIOLATIONS" -eq 0 ]; then
    echo -e "${GREEN}四层架构检查通过${NC}"
    exit 0
fi

echo -e "${RED}发现 $VIOLATIONS 个架构违规${NC}"
exit 1
