#!/usr/bin/env python3

import sys, time, datetime, socket, json
import  _thread, os

from time import sleep

class VRftpserver:

    def __init__(self, port=6969):
        self.port = port
        self.on = True
        self.load = 0
        self.verbose = False
        if (len(sys.argv)==2) and (sys.argv[1]=="-v"):
            self.verbose = True


    def scheduler(self):
        try:
            print("VR TP3 - Simple TFTP server 0.1")
            print("Server started")
            _thread.start_new_thread(self.udp_listener, ())
            self.loadCalculator() #anchor for the threads
        except:
            print("Problem starting server!")

    def loadCalculator(self):
        last_idle = last_total = 0
        while self.on:
            loadfd = open("/proc/stat","r") 
            loaddata = loadfd.readline().strip().split()[1:]
            totaltime=0.0
            for x in loaddata:
                totaltime+=float(x)
            idletime = float(loaddata[3])
            idle_delta, total_delta = idletime - last_idle, totaltime - last_total
            last_idle, last_total = idletime,totaltime
            self.load = 100.0 * (1.0 - idle_delta / total_delta)
            sleep(2)
  
    def udp_listener(self):
        addrinfo = socket.getaddrinfo('localhost', None)[0]
        udpsocket = socket.socket(addrinfo[0], socket.SOCK_DGRAM)
        udpsocket.bind(('', self.port))

        while self.on:
            data, sender = udpsocket.recvfrom(65535)
            payload = json.loads(data.decode())
            senderIP = (str(sender[0]))
            senderPort = (sender[1])
            if (self.verbose):
                #print(data.decode())
                print("Got a connection from: ",senderIP, " on port ", senderPort)
            if (payload[0]=='RRQ'):
                #clientsocket = socket.socket(addrinfo[0], socket.SOCK_DGRAM)
                if (self.verbose): print("Cliente requested: ",payload[1])
                if os.path.exists(payload[1]):
                    if os.path.getsize(payload[1]) < 61440:
                        try:
                            readfd = open(str(payload[1]),"r") 
                            self.sendfile(readfd, udpsocket, senderIP, senderPort) ## implement
                            if (self.verbose): print("Requested file sent")
                        except:
                            if (self.verbose): print("Error opening file")
                    else:
                        print("File is too big. Max 60kB supported in this version")
                        bts = json.dumps(["ERR",2]).encode()
                        udpsocket.sendto(bts,(senderIP,senderPort))
                else:
                    bts = json.dumps(["ERR",1]).encode()
                    udpsocket.sendto(bts,(senderIP,senderPort))
            elif (payload[0]=='LOAD'):
                if (self.verbose): print("Cliente requested the computer load")
                bts = json.dumps(["LOAD",self.load]).encode()
                udpsocket.sendto(bts,(senderIP,senderPort))

    def sendfile(self, sendfd, socket, ip , port):  
        #sendfd.seek(0,2)
        #size = fileobject.tell()
        #print(size)
        data = sendfd.read(60000) 
        bts = json.dumps(["DAT",0,data]).encode()
        socket.sendto(bts,(ip,port))

if __name__ == '__main__':
    try:
        prob = VRftpserver()
        prob.scheduler()
    except KeyboardInterrupt:
        print('Exiting')
