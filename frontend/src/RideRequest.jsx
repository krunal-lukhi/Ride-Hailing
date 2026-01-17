import React, { useState } from 'react';
import axios from 'axios';
import { Car, MapPin, Navigation } from 'lucide-react';

const RideRequest = ({ onRideRequested, pickup, setPickup, dropoff, setDropoff }) => {
    const [loading, setLoading] = useState(false);

    const handleRequest = async () => {
        setLoading(true);
        try {
            const res = await axios.post('/v1/rides', {
                riderId: 1, // Hardcoded for demo
                pickupLat: pickup[0],
                pickupLon: pickup[1],
                dropoffLat: dropoff[0],
                dropoffLon: dropoff[1],
                paymentMethod: 'CARD'
            });
            onRideRequested(res.data);
        } catch (error) {
            console.error("Error requesting ride", error);
            alert("Failed to request ride");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="overlay-panel">
            <h2 style={{ marginTop: 0, display: 'flex', alignItems: 'center', gap: '10px' }}>
                <Car size={24} /> Ride App
            </h2>

            <div style={{ marginBottom: '15px' }}>
                <label style={{ display: 'flex', alignItems: 'center', gap: '5px', fontSize: '0.9em', color: '#666' }}>
                    <MapPin size={16} color="green" /> Pickup
                </label>
                <div style={{ fontWeight: 'bold' }}>Lat: {pickup[0].toFixed(4)}, Lon: {pickup[1].toFixed(4)}</div>
            </div>

            <div style={{ marginBottom: '20px' }}>
                <label style={{ display: 'flex', alignItems: 'center', gap: '5px', fontSize: '0.9em', color: '#666' }}>
                    <Navigation size={16} color="red" /> Dropoff
                </label>
                <div style={{ fontWeight: 'bold' }}>Lat: {dropoff[0].toFixed(4)}, Lon: {dropoff[1].toFixed(4)}</div>
            </div>

            <button
                onClick={handleRequest}
                disabled={loading}
                style={{
                    width: '100%',
                    padding: '12px',
                    background: 'black',
                    color: 'white',
                    border: 'none',
                    borderRadius: '8px',
                    fontSize: '1.1em',
                    cursor: loading ? 'not-allowed' : 'pointer',
                    opacity: loading ? 0.7 : 1
                }}
            >
                {loading ? 'Requesting...' : 'Request Ride'}
            </button>
        </div>
    );
};

export default RideRequest;
