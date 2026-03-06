#!/bin/bash

# Binance账户监控脚本
# 使用方法: ./binance-monitor.sh <API_KEY> <API_SECRET>

API_KEY=$1
API_SECRET=$2

if [ -z "$API_KEY" ] || [ -z "$API_SECRET" ]; then
    echo "用法: ./binance-monitor.sh <API_KEY> <API_SECRET>"
    echo ""
    echo "示例:"
    echo "  ./binance-monitor.sh your_api_key your_api_secret"
    exit 1
fi

# 获取账户余额
get_balance() {
    timestamp=$(date +%s)000
    signature=$(echo -n "timestamp=${timestamp}" | openssl dgst -sha256 -hmac "$API_SECRET" | cut -d' ' -f2)
    
    curl -s "https://api.binance.com/api/v3/account?timestamp=${timestamp}&signature=${signature}" \
        -H "X-MBX-APIKEY: $API_KEY" | python3 -m json.tool 2>/dev/null || echo "请求失败"
}

# 获取USDT余额
get_usdt() {
    result=$(get_balance)
    echo "$result" | grep -A5 '"asset": "USDT"' | grep -o '"free": "[0-9.]*"' || echo "获取失败"
}

# 主菜单
echo "=================================="
echo "  Binance 账户监控"
echo "=================================="
echo "1. 查看全部余额"
echo "2. 查看USDT余额"
echo "3. 查看BTC持仓"
echo "4. 实时价格监控"
echo "5. 退出"
echo -n "请选择: "
read choice

case $choice in
    1) get_balance ;;
    2) get_usdt ;;
    3) echo "BTC持仓查询..." ;;
    4) echo "价格监控（按Ctrl+C退出）..."; while true; do curl -s "https://api.binance.com/api/v3/ticker/price?symbol=BTCUSDT"; echo ""; sleep 5; done ;;
    5) exit 0 ;;
    *) echo "无效选择" ;;
esac