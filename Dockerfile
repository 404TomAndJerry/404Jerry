
FROM amazoncorretto:17

ARG JAR_FILE=build/libs/*.jar

COPY ${JAR_FILE} app.jar

ENV TZ=Asia/Seoul

# 6. 실행 명령어 (Entrypoint)
# 컨테이너가 시작될 때 실행할 명령어다.
# "java -jar /app.jar" 와 같다.
ENTRYPOINT ["java", "-jar", "/app.jar"]