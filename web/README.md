# RequestBin Web

RequestBin의 웹 UI 서비스입니다. Lit + TypeScript + Vite 기반으로 동작하며,
Microsoft Entra 로그인 후 bin 생성/조회 기능을 제공합니다.

## 개요

- 로그인: Microsoft Entra (MSAL Browser)
- Bin 생성: API `GET /bin/create`
- 요청 조회: API `GET /bin/read/:binId/:numToRead`
- 자동 갱신: 설정 가능한 polling interval로 주기 조회
- 표시 정보: method/path/query/headers/body(base64 + UTF-8 decode)/remoteHost/createdAt

## 기술 스택

- Lit 3
- TypeScript 5
- Vite 7
- Tailwind CSS 4
- @azure/msal-browser

## 로컬 개발 실행

프로젝트 루트 기준:

1. `cd web/requestbin`
2. `npm install`
3. `npm run dev`

기본 개발 설정은 `public/config.js`를 사용합니다.

## 프로덕션(Docker) 실행

`web/Dockerfile`은 정적 빌드 후 `serve`로 80 포트를 제공합니다.

- 빌드 시점: `npm run build`
- 컨테이너 시작 시점: `entrypoint.sh`가 환경변수로 `dist/config.js` 생성

프로젝트 루트에서 전체 스택 실행:

1. `docker compose up --build`
2. 브라우저에서 `https://requestbin.localhost` 접속

## 환경변수

웹 컨테이너는 아래 환경변수를 사용합니다.

- `REQUESTBIN_API_URL`: 웹이 호출할 API base URL. 기본값 `http://localhost`
- `REQUESTBIN_BIN_BASE_DOMAIN`: bin URL 생성용 base domain. 기본값 `localhost`
- `REQUESTBIN_POLL_INTERVAL_MS`: 요청 목록 자동 갱신 주기(ms). 기본값 `15000`
- `REQUESTBIN_NUM_REQUESTS`: 1회 조회 시 읽을 요청 수. 기본값 `20`
- `REQUESTBIN_ENTRA_TENANT_ID`: Entra tenant id
- `REQUESTBIN_ENTRA_CLIENT_ID`: Entra app(client) id
- `REQUESTBIN_ENTRA_SCOPE`: API access scope (예: `api://<client-id>/RequestBin`)

참고: 로컬 개발 기본값은 `web/requestbin/public/config.js`에 있으며,
Docker 실행 시에는 `entrypoint.sh`가 런타임 값을 덮어씁니다.

## 인증 동작

웹은 Microsoft Entra 인증을 전제로 합니다.

- 앱 시작 시 `handleRedirectPromise`로 redirect 응답 처리
- 로그인은 `loginRedirect` 사용
- API 호출 전 `acquireTokenSilent`로 토큰 획득
- silent 갱신 실패(`InteractionRequiredAuthError`) 시 `acquireTokenRedirect`로 전환

토큰은 `Authorization: Bearer <token>` 형태로 API에 전달됩니다.

## 화면/기능

### 1. 로그인 화면

- 인증되지 않은 상태에서 `Login with Microsoft` 버튼 제공

### 2. Bin 생성 화면

- 인증 후 `Create Bin` 버튼으로 새 bin 생성

### 3. Bin 뷰어 화면

- 좌측: bin URL, Copy 버튼, Refresh 버튼, New Bin 버튼, 요청 목록
- 우측: 선택된 요청 상세
- 자동 갱신 중에는 Refresh 버튼에 진행 원형 인디케이터 표시

### 요청 상세 표시

- Meta: method, path, remoteHost, createdAt
- Query: key -> values[]
- Headers: key -> value
- Body:
  - 원본 base64
  - UTF-8 디코딩 결과(디코딩 불가 시 binary 안내 문구)

## API 의존성

웹이 사용하는 API는 아래 2개입니다.

- `GET /bin/create`
- `GET /bin/read/:binId/:numToRead`

요청/응답 상세는 `api/requestbin/README.md`를 참고하세요.

## 트러블슈팅

- 로그인 후에도 인증이 풀려 보이는 경우:
  - `REQUESTBIN_ENTRA_*` 값과 API 쪽 Entra 설정 일치 여부를 확인하세요.
- 요청이 조회되지 않는 경우:
  - `REQUESTBIN_API_URL`이 프록시/인증서 환경과 일치하는지 확인하세요.
- bin URL로 요청이 들어오지 않는 경우:
  - `REQUESTBIN_BIN_BASE_DOMAIN` 값과 프록시의 subdomain 라우팅 설정을 확인하세요.
