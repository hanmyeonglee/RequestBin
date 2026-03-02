FROM openjdk:21-rc-jdk-slim

RUN apt update && apt install -y curl gzip
RUN curl -fL https://github.com/coursier/coursier/releases/latest/download/cs-x86_64-pc-linux.gz | gzip -d > cs
RUN chmod +x cs
RUN ./cs setup -y

WORKDIR /api

CMD ["tail", "-f", "/dev/null"]