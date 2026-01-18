#!/bin/bash

echo "=== New Relic Monitoring Verification ==="
echo ""

# 1. Check if backend is running
echo "1. Backend Status:"
if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "   ✅ Backend is running on port 8080"
else
    echo "   ❌ Backend is not running"
    echo "   Run: ./start_backend.sh"
    exit 1
fi

# 2. Check New Relic agent
echo ""
echo "2. New Relic Agent:"
if [ -f "newrelic/newrelic.jar" ]; then
    echo "   ✅ New Relic JAR found"
    echo "   Size: $(du -h newrelic/newrelic.jar | cut -f1)"
else
    echo "   ❌ New Relic JAR not found"
    echo "   It will be downloaded automatically on next backend start"
fi

# 3. Check license key
echo ""
echo "3. License Key Configuration:"
if [ -n "$NRIA_LICENSE_KEY" ]; then
    echo "   ✅ NRIA_LICENSE_KEY is set"
    echo "   Key: ${NRIA_LICENSE_KEY:0:10}..."
else
    echo "   ⚠️  NRIA_LICENSE_KEY not set in current shell"
    echo "   Check .env file"
fi

# 4. Check New Relic logs
echo ""
echo "4. New Relic Agent Logs:"
if [ -d "newrelic/logs" ]; then
    LOG_COUNT=$(ls -1 newrelic/logs/*.log 2>/dev/null | wc -l)
    if [ $LOG_COUNT -gt 0 ]; then
        echo "   ✅ Found $LOG_COUNT log file(s)"
        echo "   Latest log:"
        LATEST_LOG=$(ls -t newrelic/logs/*.log 2>/dev/null | head -1)
        if [ -f "$LATEST_LOG" ]; then
            echo "   File: $LATEST_LOG"
            echo "   Last 5 lines:"
            tail -5 "$LATEST_LOG" | sed 's/^/      /'
        fi
    else
        echo "   ⚠️  No log files found yet"
        echo "   Agent may still be initializing"
    fi
else
    echo "   ⚠️  Logs directory not created yet"
fi

# 5. Test API with load
echo ""
echo "5. Generating Test Load:"
echo "   Sending 10 requests to /v1/drivers/nearby..."

SUCCESS=0
FAIL=0

for i in {1..10}; do
    RESPONSE=$(curl -s -w "%{http_code}" -H "X-API-KEY: secret_api_key_123" \
        "http://localhost:8080/v1/drivers/nearby?lat=40.7128&lon=-74.0060&radius=5" \
        -o /dev/null 2>/dev/null)
    
    if [ "$RESPONSE" = "200" ]; then
        ((SUCCESS++))
    else
        ((FAIL++))
    fi
    sleep 0.1
done

echo "   ✅ Successful: $SUCCESS"
if [ $FAIL -gt 0 ]; then
    echo "   ❌ Failed: $FAIL"
fi

# 6. Check New Relic Dashboard
echo ""
echo "6. Next Steps:"
echo "   📊 Login to New Relic: https://one.newrelic.com"
echo "   🔍 Navigate to: APM & Services → 'Ride Backend'"
echo "   📈 You should see:"
echo "      - Response time metrics"
echo "      - Throughput (requests/min)"
echo "      - Transaction traces"
echo "      - Database queries"
echo ""
echo "   ⚠️  Note: It may take 2-3 minutes for data to appear in New Relic"
echo ""
echo "7. Create Alerts:"
echo "   See NEW_RELIC_SETUP.md for detailed instructions"
echo ""
echo "=== Verification Complete ===" 
