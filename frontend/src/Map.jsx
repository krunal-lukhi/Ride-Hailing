import React, { useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import L from 'leaflet';
import icon from 'leaflet/dist/images/marker-icon.png';
import iconShadow from 'leaflet/dist/images/marker-shadow.png';

// Fix Leaflet Default Icon
let DefaultIcon = L.icon({
    iconUrl: icon,
    shadowUrl: iconShadow,
    iconAnchor: [12, 41]
});
L.Marker.prototype.options.icon = DefaultIcon;

// Dynamic Marker updates
const MapUpdater = ({ center }) => {
    const map = useMap();
    useEffect(() => {
        map.setView(center);
    }, [center, map]);
    return null;
};

const MapComponent = ({ drivers, pickup, dropoff, center }) => {
    return (
        <MapContainer center={center} zoom={13} className="map-container" zoomControl={false}>
            <TileLayer
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                attribution='&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
            />
            <MapUpdater center={center} />

            {/* Drivers */}
            {drivers.map(driver => (
                <Marker key={driver.id} position={[driver.latitude, driver.longitude]} icon={
                    new L.DivIcon({
                        className: 'driver-icon',
                        html: `<div style="background-color:blue;width:12px;height:12px;border-radius:50%;border:2px solid white;"></div>`
                    })
                }>
                    <Popup>Driver {driver.id}</Popup>
                </Marker>
            ))}

            {/* Pickup */}
            {pickup && (
                <Marker position={pickup} icon={
                    new L.Icon({
                        iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png',
                        shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
                        iconSize: [25, 41],
                        iconAnchor: [12, 41],
                        popupAnchor: [1, -34],
                        shadowSize: [41, 41]
                    })
                }>
                    <Popup>Pickup Location</Popup>
                </Marker>
            )}

            {/* Dropoff */}
            {dropoff && (
                <Marker position={dropoff} icon={
                    new L.Icon({
                        iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
                        shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
                        iconSize: [25, 41],
                        iconAnchor: [12, 41],
                        popupAnchor: [1, -34],
                        shadowSize: [41, 41]
                    })
                }>
                    <Popup>Dropoff Location</Popup>
                </Marker>
            )}
        </MapContainer>
    );
};

export default MapComponent;
