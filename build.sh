#!/bin/bash

# 默认环境为 dev
ENV=${1:-dev}

# 验证环境参数
if [[ ! "$ENV" =~ ^(stable|dev|test|prod)$ ]]; then
    echo "错误: 无效的环境参数 '$ENV'"
    echo "用法: $0 [stable|dev|test|prod]"
    echo "示例: $0 dev    # 打包开发环境"
    echo "      $0 stable   # 打包集成环境"
    echo "      $0 test   # 打包测试环境"
    echo "      $0 prod   # 打包生产环境"
    exit 1
fi

echo "=========================================="
echo "开始打包，环境: $ENV"
echo "=========================================="

# 使用 Maven profile 进行打包
mvn -B clean package -Dmaven.test.skip=true -Dautoconfig.skip -P$ENV

# 检查打包是否成功
if [ $? -ne 0 ]; then
    echo "打包失败！"
    exit 1
fi

# 创建输出目录
mkdir -p output

# 解压打包文件
tar --strip-components=1 -zxf target/*.tar.gz -C output

echo "=========================================="
echo "打包完成，环境: $ENV"
echo "输出目录: output/"
echo "=========================================="
