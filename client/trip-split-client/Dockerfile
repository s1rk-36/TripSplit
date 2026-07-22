# REACT_APP_API_URL is baked in at build time (Create React App),
# so it must arrive as a build ARG, not a runtime env var.
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
ARG REACT_APP_API_URL
ENV REACT_APP_API_URL=$REACT_APP_API_URL
RUN npm run build

FROM node:20-alpine
RUN npm install -g serve
WORKDIR /app
COPY --from=build /app/build ./build
EXPOSE 3000
CMD ["sh", "-c", "serve -s build -l ${PORT:-3000}"]
