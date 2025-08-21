# 使用帶 JDK 21 的官方映像
FROM eclipse-temurin:21-jdk-alpine

# 設定工作目錄
WORKDIR /app

# 複製專案
COPY . .

# Build 專案
RUN ./mvnw clean package -DskipTests

# 啟動指令
CMD ["java", "-jar", "target/SecurePack-0.0.1-SNAPSHOT.jar"]
