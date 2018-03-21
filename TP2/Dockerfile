FROM ubuntu:latest
RUN apt-get update && apt-get install -y apt-utils && apt-get install -y git && apt-get install -y sudo 
RUN cd  /root && git clone git://github.com/mininet/mininet && mininet/util/install.sh -w
VOLUME /home/ubuntu
WORKDIR /home/ubuntu
RUN apt-get install -y tcpdump
RUN DEBIAN_FRONTEND=noninteractive apt-get install -y wireshark
EXPOSE 6653
EXPOSE 6633
