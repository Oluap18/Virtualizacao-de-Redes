FROM ubuntu:latest
RUN apt-get update
RUN apt-get install -y nano
VOLUME /home/ubuntu
WORKDIR /home/ubuntu
RUN apt-get install -y mininet
RUN apt-get install -y tcpdump
RUN apt-get install -y wireshark
EXPOSE 6653
EXPOSE 6633
