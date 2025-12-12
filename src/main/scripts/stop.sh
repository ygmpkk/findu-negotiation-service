#!/bin/bash

# 应用名称
APP_NAME="findu-negotiation-service"
# 主类名
MAIN_CLASS="com.findu.negotiation.FinduNegotiationApplication"

# 查找并停止进程
PIDS=$(ps -ef | grep java | grep "$MAIN_CLASS" | grep -v grep | awk '{print $2}')

if [ -z "$PIDS" ]; then
    echo "$APP_NAME is not running."
else
    echo "Stopping $APP_NAME..."
    for PID in $PIDS; do
        if [ -n "$PID" ]; then
            echo "  Killing process PID: $PID"
            kill "$PID" 2>/dev/null
        fi
    done
    sleep 3
    REMAINING_PIDS=$(ps -ef | grep java | grep "$MAIN_CLASS" | grep -v grep | awk '{print $2}')
    if [ -n "$REMAINING_PIDS" ]; then
        echo "  Force killing remaining processes..."
        for PID in $REMAINING_PIDS; do
            if [ -n "$PID" ]; then
                kill -9 "$PID" 2>/dev/null
            fi
        done
        sleep 1
    fi
    echo "$APP_NAME stopped."
fi
