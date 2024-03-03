FROM docker.io/sbtscala/scala-sbt:eclipse-temurin-focal-11.0.17_8_1.9.0_2.12.18

COPY . /src
RUN mkdir /app
RUN mkdir /app/approve
RUN mkdir /app/deny

WORKDIR /app
COPY media/approve* approve/
COPY media/deny* deny/

WORKDIR /src
RUN sbt assembly
RUN cp target/scala-2.13/knuckles-assembly-*.jar /app/runtime.jar
WORKDIR /
RUN rm -rf /src

WORKDIR /app

# config needs to be mapped to /app/config
ENTRYPOINT ["java", "-jar", "runtime.jar"]
