#!/bin/bash

# 获取hostname
HOSTNAME=$(hostname)
# 获取内网IP（推荐方法）
IP=$(hostname -I | awk '{print $1}')
ENV=$(hostname | cut -d'-' -f1)

echo "Internal IP: $IP"
echo "Hostname: $HOSTNAME"
echo "ENV=$ENV"

# 获取脚本所在目录
SCRIPT_DIR=$(cd "$(dirname "$0")" || exit; pwd)

# 重启操作：先停止再启动
echo "=========================================="
echo "Restarting application..."
echo "=========================================="

# 先执行停止脚本
echo "Step 1: Stopping application..."
bash "$SCRIPT_DIR/stop.sh"

# 等待几秒确保进程完全停止
echo "Waiting for application to stop completely..."
sleep 5

# 再执行启动脚本（传递所有参数，如 --debug）
echo "Step 2: Starting application..."
bash "$SCRIPT_DIR/start.sh" "$@"

echo "=========================================="
echo "Restart completed."
echo "=========================================="

