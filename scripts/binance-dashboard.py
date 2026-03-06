#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
Binance 账户监控仪表板
使用: python3 binance-dashboard.py
"""

import os
import sys
import time
import hmac
import hashlib
import requests
import json
from flask import Flask, render_template_string, request, jsonify
from datetime import datetime

app = Flask(__name__)

# 存储配置（内存中）
config = {}

def get_balance():
    """获取账户余额"""
    if not config.get('api_key') or not config.get('api_secret'):
        return None, "请先配置API密钥"
    
    try:
        timestamp = int(time.time() * 1000)
        query_string = f'timestamp={timestamp}'
        signature = hmac.new(
            config['api_secret'].encode('utf-8'),
            query_string.encode('utf-8'),
            hashlib.sha256
        ).hexdigest()
        
        url = f"https://api.binance.com/api/v3/account?{query_string}&signature={signature}"
        headers = {'X-MBX-APIKEY': config['api_key']}
        
        response = requests.get(url, headers=headers, timeout=10)
        data = response.json()
        
        if 'code' in data:
            return None, data.get('msg', 'API错误')
        
        # 过滤有余额的资产
        balances = [b for b in data.get('balances', []) 
                   if float(b.get('free', 0)) > 0 or float(b.get('locked', 0)) > 0]
        
        # 获取USDT价格并计算估值
        usdt_prices = get_prices()
        
        total_usdt = 0
        for b in balances:
            asset = b['asset']
            free = float(b['free'])
            locked = float(b['locked'])
            total = free + locked
            
            # 尝试获取USDT估值
            if asset == 'USDT':
                b['usdt_value'] = total
                total_usdt += total
            else:
                price = usdt_prices.get(asset + 'USDT')
                if price:
                    b['usdt_value'] = total * float(price)
                    total_usdt += b['usdt_value']
                else:
                    b['usdt_value'] = None
        
        return {
            'balances': balances,
            'total_usdt': total_usdt,
            'update_time': datetime.now().strftime('%Y-%m-%d %H:%M:%S')
        }, None
        
    except Exception as e:
        return None, str(e)

def get_prices():
    """获取常见币种对USDT的价格"""
    try:
        url = "https://api.binance.com/api/v3/ticker/price"
        response = requests.get(url, timeout=10)
        prices = {}
        for item in response.json():
            if item['symbol'].endswith('USDT'):
                prices[item['symbol']] = item['price']
        return prices
    except:
        return {}

# HTML模板
HTML_TEMPLATE = '''
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Binance 账户监控</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { 
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
            min-height: 100vh;
            color: #fff;
        }
        .container { max-width: 1200px; margin: 0 auto; padding: 20px; }
        
        /* 头部 */
        .header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 20px 0;
            border-bottom: 1px solid rgba(255,255,255,0.1);
            margin-bottom: 30px;
        }
        .header h1 { color: #f0b90b; font-size: 24px; }
        .last-update { color: #888; font-size: 12px; }
        
        /* 总资产卡片 */
        .total-card {
            background: linear-gradient(135deg, #f0b90b 0%, #d49f0a 100%);
            border-radius: 16px;
            padding: 30px;
            margin-bottom: 30px;
            box-shadow: 0 8px 32px rgba(240, 185, 11, 0.3);
        }
        .total-card h2 { font-size: 14px; opacity: 0.8; margin-bottom: 8px; }
        .total-card .amount { font-size: 48px; font-weight: bold; }
        .total-card .currency { font-size: 24px; opacity: 0.8; }
        
        /* 设置区域 */
        .settings {
            background: rgba(255,255,255,0.05);
            border-radius: 12px;
            padding: 20px;
            margin-bottom: 30px;
        }
        .settings h3 { margin-bottom: 15px; color: #f0b90b; }
        .input-group { display: flex; gap: 10px; margin-bottom: 15px; }
        .input-group input {
            flex: 1;
            padding: 12px;
            border: 1px solid rgba(255,255,255,0.2);
            border-radius: 8px;
            background: rgba(0,0,0,0.3);
            color: #fff;
            font-size: 14px;
        }
        .input-group input::placeholder { color: #666; }
        .btn {
            padding: 12px 24px;
            border: none;
            border-radius: 8px;
            cursor: pointer;
            font-size: 14px;
            font-weight: 500;
            transition: all 0.3s;
        }
        .btn-primary { background: #f0b90b; color: #000; }
        .btn-primary:hover { background: #d49f0a; }
        .btn-secondary { background: rgba(255,255,255,0.1); color: #fff; }
        .btn-secondary:hover { background: rgba(255,255,255,0.2); }
        
        /* 资产列表 */
        .balances { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px; }
        .balance-card {
            background: rgba(255,255,255,0.05);
            border-radius: 12px;
            padding: 20px;
            transition: all 0.3s;
        }
        .balance-card:hover { background: rgba(255,255,255,0.1); transform: translateY(-2px); }
        .balance-card .asset { font-size: 18px; font-weight: bold; color: #f0b90b; }
        .balance-card .amount { font-size: 24px; margin: 8px 0; }
        .balance-card .usdt-value { color: #888; font-size: 14px; }
        
        /* 错误提示 */
        .error { 
            background: rgba(255, 77, 77, 0.2); 
            border: 1px solid #ff4d4d; 
            border-radius: 8px; 
            padding: 15px; 
            margin-bottom: 20px;
            color: #ff4d4d;
        }
        
        /* 价格区域 */
        .prices { margin-top: 30px; }
        .prices h3 { margin-bottom: 15px; color: #f0b90b; }
        .price-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(150px, 1fr)); gap: 12px; }
        .price-card {
            background: rgba(255,255,255,0.05);
            border-radius: 8px;
            padding: 15px;
            text-align: center;
        }
        .price-card .symbol { color: #888; font-size: 12px; }
        .price-card .price { font-size: 18px; font-weight: bold; margin-top: 5px; }
        
        @media (max-width: 768px) {
            .total-card .amount { font-size: 32px; }
            .input-group { flex-direction: column; }
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>💰 Binance 监控</h1>
            <div class="last-update">更新于: {{ update_time }}</div>
        </div>
        
        {% if error %}
        <div class="error">{{ error }}</div>
        {% endif %}
        
        <!-- 设置区域 -->
        <div class="settings">
            <h3>⚙️ API 配置</h3>
            <form method="POST" action="/config">
                <div class="input-group">
                    <input type="text" name="api_key" placeholder="API Key" value="{{ api_key or '' }}">
                    <input type="password" name="api_secret" placeholder="API Secret">
                </div>
                <button type="submit" class="btn btn-primary">保存配置</button>
                <a href="/clear" class="btn btn-secondary" style="margin-left:10px;">清除配置</a>
            </form>
            <p style="margin-top:10px;color:#666;font-size:12;">
                ⚠️ API密钥仅保存在服务器内存中，刷新页面后需要重新输入
            </p>
        </div>
        
        {% if has_balance %}
        <!-- 总资产 -->
        <div class="total-card">
            <h2>总资产 (USDT)</h2>
            <div class="amount">${{ "%.2f"|format(total_usdt) }}</div>
        </div>
        
        <!-- 持仓列表 -->
        <div class="balances">
            {% for b in balances %}
            <div class="balance-card">
                <div class="asset">{{ b.asset }}</div>
                <div class="amount">{{ "%.6f"|format(b.free + b.locked) }}</div>
                {% if b.usdt_value %}
                <div class="usdt-value">≈ ${{ "%.2f"|format(b.usdt_value) }}</div>
                {% endif %}
            </div>
            {% endfor %}
        </div>
        
        <!-- 热门价格 -->
        <div class="prices">
            <h3>📈 热门价格</h3>
            <div class="price-grid">
                <div class="price-card">
                    <div class="symbol">BTC/USDT</div>
                    <div class="price">${{ prices.BTCUSDT or '---' }}</div>
                </div>
                <div class="price-card">
                    <div class="symbol">ETH/USDT</div>
                    <div class="price">${{ prices.ETHUSDT or '---' }}</div>
                </div>
                <div class="price-card">
                    <div class="symbol">BNB/USDT</div>
                    <div class="price">${{ prices.BNBUSDT or '---' }}</div>
                </div>
                <div class="price-card">
                    <div class="symbol">SOL/USDT</div>
                    <div class="price">${{ prices.SOLUSDT or '---' }}</div>
                </div>
                <div class="price-card">
                    <div class="symbol">XRP/USDT</div>
                    <div class="price">${{ prices.XRPUSDT or '---' }}</div>
                </div>
                <div class="price-card">
                    <div class="symbol">DOGE/USDT</div>
                    <div class="price">${{ prices.DOGEUSDT or '---' }}</div>
                </div>
            </div>
        </div>
        {% endif %}
        
        <!-- 自动刷新 -->
        <script>
        // 每30秒自动刷新
        setTimeout(() => location.reload(), 30000);
        </script>
    </div>
</body>
</html>
'''

@app.route('/')
def index():
    api_key = config.get('api_key', '')
    api_secret = config.get('api_secret', '')
    
    if not api_key or not api_secret:
        return render_template_string(HTML_TEMPLATE, 
            api_key=api_key[:10] + '...' if api_key else '',
            error=None, has_balance=False)
    
    balances_data, error = get_balance()
    
    if error:
        return render_template_string(HTML_TEMPLATE,
            api_key=api_key[:10] + '...',
            error="错误: " + error,
            has_balance=False)
    
    # 获取热门价格
    prices = get_prices()
    
    return render_template_string(HTML_TEMPLATE,
        api_key=api_key[:10] + '...',
        balances=balances_data.get('balances', []),
        total_usdt=balances_data.get('total_usdt', 0),
        update_time=balances_data.get('update_time', ''),
        prices=prices,
        has_balance=True)

@app.route('/config', methods=['POST'])
def set_config():
    global config
    api_key = request.form.get('api_key', '').strip()
    api_secret = request.form.get('api_secret', '').strip()
    
    if api_key and api_secret:
        config['api_key'] = api_key
        config['api_secret'] = api_secret
    
    return '<script>location.href="/"</script>'

@app.route('/clear')
def clear_config():
    global config
    config = {}
    return '<script>location.href="/"</script>'

if __name__ == '__main__':
    print("=" * 50)
    print("  Binance 监控仪表板")
    print("=" * 50)
    print("  访问地址: http://localhost:5000")
    print("=" * 50)
    app.run(host='0.0.0.0', port=5000, debug=False)