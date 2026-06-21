FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

ARG MODULE_PATH

COPY . .

RUN mvn -pl ${MODULE_PATH} -am -DskipTests -Dspotless.skip=true clean package

FROM eclipse-temurin:21-jre

WORKDIR /app

ARG MODULE_PATH

COPY --from=build /app/${MODULE_PATH}/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
