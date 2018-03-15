FROM ubuntu:latest
RUN apt-get update
RUN apt-get install -y nano
VOLUME /home/captain
WORKDIR /home/captain
RUN apt-get install -y mininet
RUN apt-get install -y tcpdump
RUN apt-get install -y wireshark
EXPOSE 6653
EXPOSE 6633
