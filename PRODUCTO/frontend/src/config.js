// Configuración centralizada de la URL de la API obtenida de las variables de entorno de Vite (.env)
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '';

export default API_BASE_URL;
