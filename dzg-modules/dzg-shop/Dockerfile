# 推荐使用 BellSoft Liberica JDK 17
FROM bellsoft/liberica-openjdk-rocky:17.0.16-cds

LABEL maintainer="DZG"

RUN mkdir -p /dzg/shop/logs /dzg/shop/temp

WORKDIR /dzg/shop

ENV SERVER_PORT=9206 LANG=C.UTF-8 LC_ALL=C.UTF-8 JAVA_OPTS=""

EXPOSE ${SERVER_PORT}

ADD ./target/dzg-shop.jar ./app.jar

SHELL ["/bin/bash", "-c"]

ENTRYPOINT java -Djava.security.egd=file:/dev/./urandom -Dserver.port=${SERVER_PORT} \
           -XX:+HeapDumpOnOutOfMemoryError -XX:+UseZGC ${JAVA_OPTS} \
           -jar app.jar
