FROM theasp/clojurescript-nodejs:alpine

WORKDIR /usr/src/app
COPY project.clj /usr/src/app/project.clj
RUN lein deps

WORKDIR /usr/src/app-tmp/
COPY ./ .
RUN lein clean
RUN set -ex; \
  rm -rf .git; \
  rm Dockerfile docker-compose.yml; \
  mv ./* /usr/src/app/

WORKDIR /usr/src/app
RUN lein with-profile production do deps, compile
ENTRYPOINT lein with-profile production run
