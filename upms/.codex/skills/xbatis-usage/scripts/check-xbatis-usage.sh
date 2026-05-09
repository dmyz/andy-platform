#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../.." && pwd)"

cd "$PROJECT_ROOT"

RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

VIOLATIONS=0

report_violation() {
    local title="$1"
    local detail="$2"

    echo -e "${RED}错误：$title${NC}"
    echo "$detail"
    echo ""
    VIOLATIONS=$((VIOLATIONS + 1))
}

report_success() {
    local message="$1"

    echo -e "${GREEN}通过：$message${NC}"
}

check_required_tools() {
    if ! command -v rg >/dev/null 2>&1; then
        report_violation "缺少检查工具" "请先安装 ripgrep，并确保 rg 可执行。"
    fi
}

check_mybatisflex_refs() {
    local matches

    matches=$(rg -n "mybatisflex|mybatis-flex|MyBatis-Flex|MyBatisFlex|QueryWrapper|UpdateWrapper|LambdaQueryWrapper|TableDef" \
        src/main/java src/test/java src/main/resources pom.xml 2>/dev/null || true)

    if [ -n "$matches" ]; then
        report_violation "仍存在 MyBatis-Flex 代码或配置引用" "$matches"
    else
        report_success "未发现 MyBatis-Flex 代码或配置引用"
    fi
}

check_long_id_table_id() {
    local matches

    matches=$(rg -n -B 1 "private Long id;" src/main/java 2>/dev/null \
        | awk '
            /^--$/ {
                block = ""
                next
            }
            {
                block = block $0 "\n"
                if ($0 ~ /private Long id;/) {
                    if (block !~ /@TableId\(value = IdAutoType\.GENERATOR , generator= Generators\.nextId\)/) {
                        printf "%s", block
                    }
                    block = ""
                }
            }
        ' || true)

    if [ -n "$matches" ]; then
        report_violation "Long 类型实体主键未使用项目统一生成器" "$matches"
    else
        report_success "Long 类型实体主键均使用统一 @TableId 生成器"
    fi
}

check_manual_primary_id_assignment() {
    local matches

    matches=$(rg -n "setId\\(IdGenerator\\.nextId\\(\\)\\)|setId\\(Generators\\.nextId\\(\\)\\)" src/main/java 2>/dev/null || true)

    if [ -n "$matches" ]; then
        report_violation "已配置生成器的实体主键仍存在手动 setId" "$matches"
    else
        report_success "未发现实体主键手动 setId 生成 ID"
    fi
}

echo "检查 XBatis 使用约定..."
echo ""

check_required_tools

if [ "$VIOLATIONS" -eq 0 ]; then
    check_mybatisflex_refs
    check_long_id_table_id
    check_manual_primary_id_assignment
fi

if [ "$VIOLATIONS" -gt 0 ]; then
    echo -e "${RED}XBatis 使用检查失败：$VIOLATIONS 项问题${NC}"
    exit 1
fi

echo ""
report_success "XBatis 使用检查完成"
