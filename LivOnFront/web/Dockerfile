# ---- Stage 1: build ----
FROM node:18-alpine AS builder
WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

# ---- Stage 2: runtime ----
FROM node:18-alpine AS runner
WORKDIR /app

# 정적 파일 서버만 설치
RUN npm install -g serve --omit=dev

# 빌드 산출물만 복사
COPY --from=builder /app/build /app/build

EXPOSE 3000
CMD ["serve", "-s", "/app/build", "-l", "3000"]
    