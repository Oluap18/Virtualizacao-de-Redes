#!/usr/bin/env python3

import sys, time, datetime, socket, json
import  _thread, os

from time import sleep

class VRftpserver:

    def __init__(self, port=6969):
        self.port = port
        self.on = True
        self.verbose = False
        if (len(sys.argv)==2) and (sys.argv[1]=="-v"):
            self.verbose = True


    def scheduler(self):
        try:
            print("VR TP3 - Simple TFTP server 0.1")
            print("Server started")
            self.udp_listener()
        except:
            print("Problem starting server!")

    def udp_listener(self):
        addrinfo = socket.getaddrinfo('localhost', None)[0]
        udpsocket = socket.socket(addrinfo[0], socket.SOCK_DGRAM)
        udpsocket.bind(('', self.port))

        while self.on:
            data, sender = udpsocket.recvfrom(65535)
            payload = data.decode()
            payload = payload[0:-1]
            senderIP = (str(sender[0]))
            senderPort = 9888
            if (self.verbose):
                #print(data.decode())
                print("Got a connection from: ",senderIP, " on port ", senderPort)
            upload = payload.split(" > ")
            if len(upload) > 1:
                name = upload[0].split(" ")
                if name[0] == upload[0]:
                    name = name[0]
                else:
                    name = None
            else:
                name = None
            if name == None:
                if (self.verbose): print("Cliente requested: ",payload)
                if os.path.exists(payload):
                    if os.path.getsize(payload) < 61440:
                        try:
                            readfd = open(str(payload),"r")
                            self.sendfile(readfd, udpsocket, senderIP, senderPort) ## implement
                            if (self.verbose): print("Requested file sent")
                        except:
                            if (self.verbose): print("Error opening file")
                    else:
                        print("File is too big. Max 60kB supported in this version")
                        bts = "2: ".encode()
                        udpsocket.sendto(bts,(senderIP,senderPort))
                else:
                    bts = "1: ".encode()
                    udpsocket.sendto(bts,(senderIP,senderPort))
            else:
                if(self.verbose): print("Cliente uploaded: ", name)
                file = open(name,"w")
                file.write(payload[len(name) + 3:])
                file.close()


    def sendfile(self, sendfd, socket, ip , port):
        #sendfd.seek(0,2)
        #size = fileobject.tell()
        #print(size)
        data = sendfd.read(60000)
        bts = ("0: " + data).encode()
        socket.sendto(bts,(ip,port))

if __name__ == '__main__':
    try:
        prob = VRftpserver()
        prob.scheduler()
    except KeyboardInterrupt:
        print('Exiting')
