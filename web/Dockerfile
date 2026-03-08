FROM node:25-trixie-slim

WORKDIR /app
COPY requestbin/package*.json .
RUN npm i

COPY requestbin/ .
RUN npm run build

WORKDIR /app/dist
EXPOSE 80

CMD ["npx", "serve", "-l", "80"]