# syntax=docker/dockerfile:1

FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:17-jre-alpine AS runner

RUN apk add --no-cache curl \
    && addgroup -S yxboot \
    && adduser -S yxboot -G yxboot

WORKDIR /app

COPY --from=builder /build/target/yunxing-api-*.jar app.jar

RUN mkdir -p /data/uploads \
    && chown -R yxboot:yxboot /app /data/uploads

USER yxboot

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
  CMD curl -f http://localhost:8080/api-docs || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
