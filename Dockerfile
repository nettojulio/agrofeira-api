FROM eclipse-temurin:24-jdk-alpine AS builder

# 1. Build Stage
WORKDIR /build

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY src src
RUN ./mvnw clean package -DskipTests

RUN java -Djarmode=layertools -jar target/*.jar extract

# 2. Final Stage
FROM eclipse-temurin:24-jre-alpine AS runner
WORKDIR /app

RUN addgroup -S spring && adduser -S springjs -G spring
USER springjs

COPY --from=builder /build/dependencies/ ./
COPY --from=builder /build/spring-boot-loader/ ./
COPY --from=builder /build/snapshot-dependencies/ ./
COPY --from=builder /build/application/ ./

EXPOSE 8080

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]