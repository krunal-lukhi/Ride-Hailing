import React, { useState, useEffect } from 'react';
import axios from 'axios';
import MapComponent from './Map';
import RideRequest from './RideRequest';
import RideStatus from './RideStatus';
import ErrorBoundary from './ErrorBoundary';

// Initial NYC coordinates
const NYC_CENTER = [40.7128, -74.0060];

function App() {
  const [pickup, setPickup] = useState(NYC_CENTER);
  const [dropoff, setDropoff] = useState([40.7200, -74.0100]);
  const [drivers, setDrivers] = useState([]);
  const [currentRide, setCurrentRide] = useState(null);

  // Poll nearby drivers
  useEffect(() => {
    const fetchDrivers = async () => {
      try {
        const res = await axios.get(`/v1/drivers/nearby?lat=${pickup[0]}&lon=${pickup[1]}&radius=50`);
        setDrivers(res.data);
      } catch (err) {
        console.error("Error fetching drivers", err);
      }
    };

    fetchDrivers(); // Initial call
    const interval = setInterval(fetchDrivers, 3000); // 3s polling
    return () => clearInterval(interval);
  }, [pickup]);

  return (
    <div>
      <MapComponent
        drivers={drivers}
        pickup={pickup}
        dropoff={dropoff}
        center={pickup}
      />

      {!currentRide ? (
        <ErrorBoundary>
          <RideRequest
            pickup={pickup}
            setPickup={setPickup}
            dropoff={dropoff}
            setDropoff={setDropoff}
            onRideRequested={setCurrentRide}
          />
        </ErrorBoundary>
      ) : (
        <ErrorBoundary>
          <RideStatus
            ride={currentRide}
            onUpdate={setCurrentRide}
          />
        </ErrorBoundary>
      )}
    </div>
  );
}

export default App;
