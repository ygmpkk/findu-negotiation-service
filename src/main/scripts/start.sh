#!/bin/bash

# 应用名称
APP_NAME="findu-negotiation-service"

# 当前目录
BASE_DIR=$(cd "$(dirname "$0")/.." || exit; pwd)

# 配置文件路径
CONFIG_DIR="$BASE_DIR/conf"
BASE_CONFIG_FILE="$CONFIG_DIR/application.properties"

# 日志目录配置
if [ -n "$APP_LOG_DIR" ]; then
    LOG_DIR="$APP_LOG_DIR"
else
    LOG_DIR=""
    if [ -d "$CONFIG_DIR" ]; then
        for profile_file in "$CONFIG_DIR"/application-*.properties; do
            if [ -f "$profile_file" ]; then
                LOG_DIR=$(grep -E "^app\.log\.dir=" "$profile_file" | head -1 | cut -d'=' -f2 | tr -d ' ')
                if [ -n "$LOG_DIR" ]; then
                    break
                fi
            fi
        done
    fi

    if [ -z "$LOG_DIR" ] && [ -f "$BASE_CONFIG_FILE" ]; then
        LOG_DIR=$(grep -E "^app\.log\.dir=" "$BASE_CONFIG_FILE" | head -1 | cut -d'=' -f2 | tr -d ' ')
    fi

    if [ -z "$LOG_DIR" ]; then
        LOG_DIR="/home/findu/logs/findu-negotiation-service"
    fi
fi

# JVM参数
JAVA_OPTS="-Xms512m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -XX:+OptimizeStringConcat -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=$LOG_DIR/heapdump.hprof -XX:+UseCompressedOops -Xlog:gc*:file=$LOG_DIR/gc.log -Dapp.log.dir=$LOG_DIR"

# 检查是否启用debug模式
if [[ "$*" == *"--debug"* ]]; then
    JAVA_OPTS="$JAVA_OPTS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
fi

# 创建日志目录
mkdir -p "$LOG_DIR"
if [ $? -ne 0 ]; then
    echo "Failed to create log directory: $LOG_DIR"
    exit 1
fi

# 构建类路径
CLASSPATH="$BASE_DIR/conf"
for jar in "$BASE_DIR/lib"/*.jar; do
    if [ -f "$jar" ]; then
        CLASSPATH="$CLASSPATH:$jar"
    fi
done

# 启动应用
echo "Starting $APP_NAME..."
echo "Log directory: $LOG_DIR"
nohup java $JAVA_OPTS -cp "$CLASSPATH" com.findu.negotiation.FinduNegotiationApplication > /dev/null 2>&1 &
PID=$!
echo "$APP_NAME started successfully. PID: $PID"
