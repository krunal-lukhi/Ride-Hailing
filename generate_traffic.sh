#!/bin/bash

echo "Generating database traffic for New Relic monitoring..."
echo ""

API_KEY="secret_api_key_123"
BASE_URL="http://localhost:8080"

# 1. Create some rides (triggers INSERT queries)
echo "1. Creating rides (INSERT queries)..."
for i in {1..5}; do
    curl -s -X POST "$BASE_URL/v1/rides" \
        -H "X-API-KEY: $API_KEY" \
        -H "Content-Type: application/json" \
        -d '{
            "riderId": 1,
            "pickupLat": 40.7128,
            "pickupLon": -74.0060,
            "dropoffLat": 40.7580,
            "dropoffLon": -73.9855,
            "paymentMethod": "CARD"
        }' > /dev/null
    echo "   Created ride $i"
    sleep 0.5
done

# 2. Query rides (triggers SELECT queries)
echo ""
echo "2. Querying rides (SELECT queries)..."
for i in {1..10}; do
    RIDE_ID=$((i % 5 + 1))
    curl -s -H "X-API-KEY: $API_KEY" \
        "$BASE_URL/v1/rides/$RIDE_ID" > /dev/null
    echo "   Queried ride $RIDE_ID"
    sleep 0.3
done

# 3. Update driver locations (triggers Redis + DB queries)
echo ""
echo "3. Updating driver locations..."
for driver_id in {1..3}; do
    for j in {1..5}; do
        LAT=$(echo "40.7128 + $j * 0.001" | bc)
        LON=$(echo "-74.0060 + $j * 0.001" | bc)
        curl -s -X POST "$BASE_URL/v1/drivers/$driver_id/location" \
            -H "X-API-KEY: $API_KEY" \
            -H "Content-Type: application/json" \
            -d "{\"latitude\": $LAT, \"longitude\": $LON}" > /dev/null
        echo "   Updated driver $driver_id location"
        sleep 0.2
    done
done

# 4. Query nearby drivers (triggers complex geo queries)
echo ""
echo "4. Querying nearby drivers (GEO queries)..."
for i in {1..10}; do
    curl -s -H "X-API-KEY: $API_KEY" \
        "$BASE_URL/v1/drivers/nearby?lat=40.7128&lon=-74.0060&radius=5" > /dev/null
    echo "   Queried nearby drivers $i"
    sleep 0.3
done

echo ""
echo "✅ Traffic generation complete!"
echo ""
echo "📊 Database operations performed:"
echo "   - 5 ride creations (INSERT)"
echo "   - 10 ride queries (SELECT)"
echo "   - 15 driver location updates (UPDATE + Redis)"
echo "   - 10 nearby driver searches (GEO + SELECT)"
echo ""
echo "⏱️  Wait 2-3 minutes for data to appear in New Relic"
echo "🔄 Then click 'Run' again in the New Relic alert setup page"
