FROM node:25-trixie-slim

WORKDIR /app
COPY requestbin/package*.json .
RUN npm i

COPY requestbin/ .
RUN npm run build

COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

WORKDIR /app/dist
EXPOSE 80

ENTRYPOINT ["/entrypoint.sh"]