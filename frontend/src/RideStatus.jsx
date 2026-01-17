import React, { useEffect } from 'react';
import axios from 'axios';
import { Loader2, CheckCircle, XCircle } from 'lucide-react';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

const RideStatus = ({ ride, onUpdate }) => {

    useEffect(() => {
        if (!ride || ride.status === 'COMPLETED' || ride.status === 'CANCELLED') return;

        // WebSocket Connection
        const socket = new SockJS('http://localhost:8080/ws');
        const stompClient = Stomp.over(socket);
        stompClient.debug = null; // Disable debug logging

        stompClient.connect({}, () => {
            console.log('Connected to WebSocket');

            stompClient.subscribe(`/topic/ride/${ride.id}`, (message) => {
                const updatedRide = JSON.parse(message.body);
                console.log("Received Update:", updatedRide);
                onUpdate(updatedRide);
            });
        }, (err) => {
            console.error("WebSocket connection error:", err);
        });

        return () => {
            if (stompClient.connected) {
                stompClient.disconnect();
            } else {
                // If closing before connected, we should try to close the socket directly
                // to avoid stompjs trying to send a DISCONNECT frame on a connecting socket
                if (socket.readyState !== WebSocket.CLOSED) {
                    socket.close();
                }
            }
        };
    }, [ride, onUpdate]);

    // State for payment status
    const [paid, setPaid] = React.useState(false);

    const handleEndTrip = async () => {
        try {
            await axios.post(`/v1/trips/${ride.id}/end`);
            // Poll will pick up status change
        } catch (error) {
            console.error("Failed to end trip", error);
            alert("Failed to end trip");
        }
    };

    const handlePayment = async () => {
        try {
            await axios.post('/v1/payments', {
                rideId: ride.id,
                amount: ride.fare || 25.50 // Fallback if backend doesn't send fare yet
            });
            setPaid(true);
        } catch (error) {
            console.error("Payment failed", error);
            alert("Payment failed");
        }
    };

    if (!ride) return null;

    if (paid) {
        return (
            <div className="overlay-panel" style={{ top: '320px', textAlign: 'center' }}>
                <CheckCircle size={48} color="green" style={{ display: 'block', margin: '0 auto 10px' }} />
                <h2>Payment Successful!</h2>
                <p>Thank you for riding with us.</p>
                <button onClick={() => window.location.reload()} style={{
                    marginTop: '10px', padding: '10px 20px', background: '#333', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer'
                }}>
                    Book Another Ride
                </button>
            </div>
        );
    }

    const getStatusIcon = () => {
        switch (ride.status) {
            case 'REQUESTED': return <Loader2 className="spin" />;
            case 'ACCEPTED': return <CheckCircle color="green" />;
            case 'COMPLETED': return <CheckCircle color="blue" />;
            default: return <XCircle color="red" />;
        }
    };

    return (
        <div className="overlay-panel" style={{ top: '320px' }}> {/* Stack below request panel */}
            <div className={`status-badge status-${ride.status}`}>
                {ride.status}
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '10px' }}>
                {getStatusIcon()}
                <span style={{ fontWeight: 'bold' }}>Ride #{ride.id}</span>
            </div>

            {ride.driver ? (
                <div style={{ background: '#f8f9fa', padding: '10px', borderRadius: '8px', marginBottom: '15px' }}>
                    <div style={{ fontSize: '0.8em', color: '#666' }}>DRIVER</div>
                    <div style={{ fontWeight: 'bold', fontSize: '1.2em' }}>{ride.driver.name}</div>
                    <div style={{ fontSize: '0.9em' }}>Toyota Camry • 4.9⭐</div>
                </div>
            ) : (
                <div style={{ color: '#666', fontStyle: 'italic', marginBottom: '15px' }}>Matching you with a driver...</div>
            )}

            {/* Simulation Controls for Demo */}
            {ride.status === 'REQUESTED' && (
                <button onClick={() => {
                    axios.post(`/v1/drivers/2/accept?rideId=${ride.id}`)
                        .catch(err => {
                            console.error(err);
                            alert("Failed to accept ride");
                        });
                }} style={{
                    width: '100%', padding: '10px', background: '#28a745', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer', fontWeight: 'bold', marginBottom: '10px'
                }}>
                    Simulate Driver Accept (Bob)
                </button>
            )}

            {ride.status === 'ACCEPTED' && (
                <button onClick={handleEndTrip} style={{
                    width: '100%', padding: '10px', background: '#ffc107', border: 'none', borderRadius: '5px', cursor: 'pointer', fontWeight: 'bold'
                }}>
                    Simulate Driver Ending Trip
                </button>
            )}

            {/* Payment View */}
            {ride.status === 'COMPLETED' && (
                <div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '15px', borderTop: '1px solid #eee', paddingTop: '10px' }}>
                        <span>Total Fare</span>
                        <span style={{ fontSize: '1.5em', fontWeight: 'bold' }}>${ride.fare || '25.50'}</span>
                    </div>
                    <button onClick={handlePayment} style={{
                        width: '100%', padding: '12px', background: 'black', color: 'white', border: 'none', borderRadius: '8px', fontSize: '1.1em', cursor: 'pointer'
                    }}>
                        Pay Now
                    </button>
                </div>
            )}
        </div>
    );
};

export default RideStatus;
