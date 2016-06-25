FROM ubuntu:16.04
MAINTAINER Daigo Tanaka <daigo.tanaka@gmail.com>

# Install dev tools
RUN apt-get update && apt-get install -y \
    wget git lsof vim build-essential

# nodejs and npm
RUN apt-get update && apt-get install -y nodejs npm git git-core
RUN ln -s /usr/bin/nodejs /usr/bin/node

# Open JDK 8
RUN apt-get update && apt-get install -y openjdk-8-jdk

###############################################################################
# Clojure
ENV LEIN_VERSION=2.6.1
ENV LEIN_INSTALL=/usr/local/bin/

WORKDIR /tmp

# Download the whole repo as an archive
RUN mkdir -p $LEIN_INSTALL \
  && wget --quiet https://github.com/technomancy/leiningen/archive/$LEIN_VERSION.tar.gz \
  && echo "Comparing archive checksum ..." \
  && echo "f7643a14fd8a4d5c19eeb416db8ea549d8d2c18a *$LEIN_VERSION.tar.gz" | sha1sum -c - \

  && mkdir ./leiningen \
  && tar -xzf $LEIN_VERSION.tar.gz  -C ./leiningen/ --strip-components=1 \
  && mv leiningen/bin/lein-pkg $LEIN_INSTALL/lein \
  && rm -rf $LEIN_VERSION.tar.gz ./leiningen \

  && chmod 0755 $LEIN_INSTALL/lein \

# Download and verify Lein stand-alone jar
  && wget --quiet https://github.com/technomancy/leiningen/releases/download/$LEIN_VERSION/leiningen-$LEIN_VERSION-standalone.zip \
  && wget --quiet https://github.com/technomancy/leiningen/releases/download/$LEIN_VERSION/leiningen-$LEIN_VERSION-standalone.zip.asc \

  && gpg --keyserver pool.sks-keyservers.net --recv-key 2E708FB2FCECA07FF8184E275A92E04305696D78 \
  && echo "Verifying Jar file signature ..." \
  && gpg --verify leiningen-$LEIN_VERSION-standalone.zip.asc \

# Put the jar where lein script expects
  && rm leiningen-$LEIN_VERSION-standalone.zip.asc \
  && mv leiningen-$LEIN_VERSION-standalone.zip /usr/share/java/leiningen-$LEIN_VERSION-standalone.jar \

# Some REPLs (e.g., Figwheel) necessitate a readline wrapper.
  &&  apt-get update && apt-get install rlfe && rm -rf /var/lib/apt/lists/*

ENV PATH=$PATH:$LEIN_INSTALL
ENV LEIN_ROOT 1

RUN lein 

# Create a user account
RUN adduser --quiet merncljs

# Install node CLI
RUN npm install -g bunyan
RUN npm install -g pm2

ENV AWS_ACCESS_KEY_ID="" \
    AWS_SECRET_ACCESS_KEY="" \
    AWS_REGION="us-east-1" \
    NODE_ENV="production" \
    RABBITMQ_DOMAIN="" \
    RABBITMQ_PORT=80 \
    RABBITMQ_USER="" \
    RABBITMQ_PASSWORD="" \
    COOKIE_SECRET="" \
    FACEBOOK_CLIENT_ID="" \
    FACEBOOK_CLIENT_SECRET=""
