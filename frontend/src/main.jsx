import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'
import axios from 'axios';

// Configure Axios with API Key
const apiKey = import.meta.env.VITE_API_KEY || 'test-secret-key'; // Fallback for dev
axios.defaults.headers.common['X-API-KEY'] = apiKey;

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
