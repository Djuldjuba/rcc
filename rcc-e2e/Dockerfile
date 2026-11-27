FROM gradle:8.12-jdk21

WORKDIR /home/gradle/rcc

COPY ./settings.gradle ./
COPY ./gradle.properties ./
COPY ./build.gradle ./
COPY ./gradle ./gradle

COPY ./rcc-e2e/build.gradle ./rcc-e2e/

RUN gradle :rcc-e2e:dependencies --no-daemon || true

COPY ./rcc-e2e ./rcc-e2e

CMD gradle :rcc-e2e:test --no-daemon \
    -Dtest.env=docker \
    -Duser.timezone=Europe/Moscow \
    -Dbrowser=${BROWSER:-chrome}