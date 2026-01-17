#!/bin/bash

# Base URL
URL="http://localhost:8080/v1"

echo "1. Updating Driver Location..."
curl -s -X POST "$URL/drivers/2/location" \
  -H "Content-Type: application/json" \
  -d '{"latitude": 40.7128, "longitude": -74.0060}'
echo -e "\nDriver Location Updated."

echo "2. Requesting a Ride..."
RIDE_RESPONSE=$(curl -s -X POST "$URL/rides" \
  -H "Content-Type: application/json" \
  -d '{
    "riderId": 1,
    "pickupLat": 40.7128,
    "pickupLon": -74.0060,
    "dropoffLat": 40.7200,
    "dropoffLon": -74.0100,
    "paymentMethod": "CARD"
  }')
echo "Response: $RIDE_RESPONSE"

# Removing potential whitespace/newlines and picking the first match
RIDE_ID=$(echo "$RIDE_RESPONSE" | grep -o '"id":[0-9]*' | head -n 1 | cut -d':' -f2 | tr -d ' \n\r')

if [ -z "$RIDE_ID" ]; then
  echo "Failed to create ride or extract ID. Exiting."
  exit 1
fi
echo "Ride ID: $RIDE_ID"

echo -e "\n3. Accepting Ride (Driver ID 2, Ride ID $RIDE_ID)..."
# Adding quotes and cleaning potential newlines
curl -s -X POST "$URL/drivers/2/accept?rideId=$RIDE_ID"
echo -e "\nRide Accepted."

echo -e "\n4. Checking Ride Status..."
curl -s -X GET "$URL/rides/$RIDE_ID"
echo -e "\n"

echo -e "\n5. Ending Trip..."
curl -s -X POST "$URL/trips/$RIDE_ID/end"
echo -e "\nTrip Ended."

echo -e "\n6. Processing Payment..."
# Ensure JSON doesn't break
curl -s -X POST "$URL/payments" \
  -H "Content-Type: application/json" \
  -d "{\"rideId\": $RIDE_ID, \"amount\": 25.50}"
echo -e "\nPayment Processed."

echo "Test Complete."
