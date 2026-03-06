#!/bin/bash

# Binance账户监控脚本（安全版）
# 使用read -s静默输入，不会显示或记录到历史

echo "=================================="
echo "  Binance 账户监控（安全版）"
echo "=================================="
echo ""

# 静默输入API密钥（不显示在屏幕上）
read -p "请输入 API Key: " API_KEY
echo ""
read -s -p "请输入 API Secret: " API_SECRET
echo ""

if [ -z "$API_KEY" ] || [ -z "$API_SECRET" ]; then
    echo -e "\n错误: API密钥不能为空"
    exit 1
fi

# 菜单
echo ""
echo "请选择操作:"
echo "1. 查看全部余额"
echo "2. 查看USDT余额"
echo "3. 查看BTC持仓"
echo "4. 实时价格监控BTC"
echo "5. 实时价格监控ETH"
echo "0. 退出"
echo -n "请选择: "
read choice

# 获取账户余额
get_balance() {
    timestamp=$(date +%s)000
    signature=$(echo -n "timestamp=${timestamp}" | openssl dgst -sha256 -hmac "$API_SECRET" | cut -d' ' -f2)
    
    result=$(curl -s "https://api.binance.com/api/v3/account?timestamp=${timestamp}&signature=${signature}" \
        -H "X-MBX-APIKEY: $API_KEY")
    
    if echo "$result" | grep -q "code"; then
        echo "错误: $(echo $result | python3 -c 'import sys,json; print(json.load(sys.stdin).get(\"msg\",\"未知错误\"))')"
    else
        echo "$result" | python3 -m json.tool
    fi
}

# 获取USDT余额
get_usdt() {
    result=$(curl -s "https://api.binance.com/api/v3/account?timestamp=$(date +%s)000&signature=$(echo -n "timestamp=$(date +%s)000" | openssl dgst -sha256 -hmac "$API_SECRET" | cut -d' ' -f2)" -H "X-MBX-APIKEY: $API_KEY")
    echo "$result" | python3 -c "
import sys,json
data=json.load(sys.stdin)
for b in data.get('balances',[]):
    if b['asset']=='USDT':
        print(f\"USDT 可用: {b['free']}\")
        print(f\"USDT 锁定: {b['locked']}\")
" 2>/dev/null || echo "获取失败"
}

# 获取BTC持仓
get_btc() {
    result=$(curl -s "https://api.binance.com/api/v3/account?timestamp=$(date +%s)000&signature=$(echo -n "timestamp=$(date +%s)000" | openssl dgst -sha256 -hmac "$API_SECRET" | cut -d' ' -f2)" -H "X-MBX-APIKEY: $API_KEY")
    echo "$result" | python3 -c "
import sys,json
data=json.load(sys.stdin)
for b in data.get('balances',[]):
    if float(b['free'])>0 or float(b['locked'])>0:
        print(f\"{b['asset']}: 可用 {b['free']}, 锁定 {b['locked']}\")
" 2>/dev/null | head -20
}

# 价格监控
watch_price() {
    symbol=$1
    echo "监控 $symbol (按Ctrl+C退出)"
    while true; do
        price=$(curl -s "https://api.binance.com/api/v3/ticker/price?symbol=$symbol" | python3 -c "import sys,json; print(json.load(sys.stdin)['price'])")
        echo "$(date '+%H:%M:%S') $symbol: \$$price"
        sleep 5
    done
}

case $choice in
    1) get_balance ;;
    2) get_usdt ;;
    3) get_btc ;;
    4) watch_price "BTCUSDT" ;;
    5) watch_price "ETHUSDT" ;;
    0) exit 0 ;;
    *) echo "无效选择" ;;
esac