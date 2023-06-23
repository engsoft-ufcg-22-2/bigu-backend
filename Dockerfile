FROM eclipse-temurin:20

COPY . ./app
WORKDIR ./app
RUN chmod +x mvnw
RUN ./mvnw install -Dmaven.test.skip=true

CMD ./mvnw spring-boot:run