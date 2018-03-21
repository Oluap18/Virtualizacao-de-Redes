FROM ubuntu:latest

RUN apt-get update
RUN apt-get install -y nano
VOLUME /home/ubuntu
WORKDIR /home/ubuntu
RUN apt-get install -y mininet
RUN apt-get install -y tcpdump
RUN apt-get install -y sudo
RUN apt-get install -y net-tools

RUN apt-get install -y git
RUN git clone https://github.com/mininet/openflow && cd openflow/
RUN apt-get install -y dpkg-dev
RUN apt-get install -y pkg-config
RUN apt-get install -y autoconf
RUN apt-get install -y openssl
RUN ./boot.sh && ./configure && make && make install
RUN apt-get install -y libssl-dev po-debconf libncurses5-dev libpcre3-dev
RUN cd ..


RUN DEBIAN_FRONTEND=noninteractive apt-get install -y wireshark
EXPOSE 6653
EXPOSE 6633
