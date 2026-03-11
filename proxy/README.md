# Reverse Proxy for DEV

## 0. 디렉토리

```bash
mkdir certs
cd certs
```

## 1. key/crt 생성

```bash
openssl req -x509 -newkey rsa:2048 -nodes -sha256 \
    -keyout requestbin.localhost.key \
    -out requestbin.localhost.crt \
    -days 365 \
    -subj "/CN=requestbin.localhost" \
    -addext "subjectAltName=DNS:requestbin.localhost"
```

## 2. local cert

```bash
mkcert -key-file .\requestbin.localhost.key -cert-file .\requestbin.localhost.crt requestbin.localhost api.requestbin.localhost *.api.requestbin.localhost
```

**cert가 브라우저에서 인증될 때까지 다소 시간 소요됨**

## 3. docker-compose.override.yml

```yml
proxy:
    build: ./proxy
    ports:
      - "80:80"
      - "443:443"
    depends_on:
      - api
      - web
```
