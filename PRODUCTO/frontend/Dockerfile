# --- Etapa 1: Base de dependencias ---
FROM node:20-alpine AS base
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .

# --- Etapa 2: Entorno de Desarrollo (HMR) ---
FROM base AS development
EXPOSE 5173
CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0"]

# --- Etapa 3: Build de Producción ---
FROM base AS builder
RUN npm run build

# --- Etapa 4: Servidor Nginx de Producción ---
FROM nginx:1.25-alpine AS production
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
