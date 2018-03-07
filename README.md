# VR-EP1
Implementação de um serviço de autenticação e um serviço de email.

Antes de usar a aplicação criada, é necessária a criação da base de dados usada pela aplicação (vr), assim como o utilizador para realizar as operações na base de dados (vr).
Para a sua criação, é necessário executar os seguintes passos após a criação do container da base de dados (através do docker-compose up, ou docker-compose run db).

1. docker exec -it <name_of_db_container> /bin/bash
2. su - postgres
3. psql
4. CREATE DATABASE vr;
5. alter user vr with encrypted password 'vr';
6. grant all privileges on database vr to vr;

Realizados estes passos, poderá usufruir da aplicação.
