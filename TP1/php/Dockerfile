FROM php:7.0-apache
RUN apt-get update \
  && apt-get install -y postgresql postgresql-contrib \
  && apt-get install sudo \
  && apt-get install -y libpq-dev \
  && docker-php-ext-install pdo pdo_pgsql \
  && apt-get clean \
  && apt-get install -y net-tools \
  && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*
