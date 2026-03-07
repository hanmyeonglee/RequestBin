FROM openjdk:21-rc-jdk-slim

RUN apt update && apt install -y curl gzip
RUN curl -fL https://github.com/coursier/coursier/releases/latest/download/cs-x86_64-pc-linux.gz | gzip -d > cs
RUN chmod +x cs
RUN ./cs setup -y --install-dir /usr/local/bin

RUN mkdir -p /db && chmod 766 /db

WORKDIR /api
COPY . .

WORKDIR /api/requestbin
CMD ["sbt", "compile", "run"]