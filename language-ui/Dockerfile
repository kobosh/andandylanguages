# ---------- Build stage ----------
FROM node:20-alpine AS build
WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .
RUN npm run build

# ---------- Runtime stage ----------
FROM nginx:alpine

# REMOVE nginx default files
RUN rm -rf /usr/share/nginx/html/*

# COPY Angular browser build to nginx root
COPY --from=build /app/dist/language-ui/browser/ /usr/share/nginx/html/

EXPOSE 80
