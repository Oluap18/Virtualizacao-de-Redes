#!/usr/bin/env python3

import sys, time, datetime, socket, json
import  _thread, os

from time import sleep

class VRftpserver:

    def __init__(self, port=6969):
        self.port = port
        self.on = True
        self.load = 0
        self.lock = False
        self.verbose = True
        if (len(sys.argv)==5) and (sys.argv[1]=="-l"):
            self.load = float(sys.argv[2])
            self.lock = True
        if (len(sys.argv)==5) and (sys.argv[3]=="-b"):
            self.bind = sys.argv[4]
        if (len(sys.argv)==3) and (sys.argv[1]=="-b"):
            self.bind = sys.argv[2]


    def scheduler(self):
        #try:
        print("VR TP3 - Simple TFTP server 0.3")
        print("Server started")
        _thread.start_new_thread(self.udp_listener, ())
        self.loadCalculator() #anchor for the threads
        #except:
        #    print("Problem starting server!")

    def loadCalculator(self):
        #sleep(10) #wait sometime for the controller 
        last_idle = last_total = 0
        addrinfo = socket.getaddrinfo('localhost', None)[0]
        udpsocket = socket.socket(addrinfo[0], socket.SOCK_DGRAM)
        while self.on:
            loadfd = open("/proc/stat","r") 
            loaddata = loadfd.readline().strip().split()[1:]
            totaltime=0.0
            for x in loaddata:
                totaltime+=float(x)
            idletime = float(loaddata[3])
            idle_delta, total_delta = idletime - last_idle, totaltime - last_total
            last_idle, last_total = idletime,totaltime
            if (self.lock==False):
                self.load = 100.0 * (1.0 - idle_delta / total_delta)
            udpsocket.sendto(str(self.load).encode(), ("10.0.0.254", 9999))
            sleep(5)
  
    def udp_listener(self):
        addrinfo = socket.getaddrinfo('localhost', None)[0]
        udpsocket = socket.socket(addrinfo[0], socket.SOCK_DGRAM)
        udpsocket.bind((self.bind, self.port))

        while self.on:
            data, sender = udpsocket.recvfrom(65535)
            payload = json.loads(data.decode())
            senderIP = (str(sender[0]))
            senderPort = (sender[1])
            if (self.verbose):
                #print(data.decode())
                if (self.verbose):print("Got a connection from: ",senderIP, " on port ", senderPort)
            if (payload[0]=='RRQ'):
                #clientsocket = socket.socket(addrinfo[0], socket.SOCK_DGRAM)
                if (self.verbose): print("Client requested: ",payload[1])
                if os.path.exists(payload[1]):
                    if os.path.getsize(payload[1]) < 61440:
                        try:
                            readfd = open(str(payload[1]),"r") 
                            self.sendfile(readfd, udpsocket, senderIP, senderPort) ## implement
                            if (self.verbose): print("Requested file sent")
                        except:
                            if (self.verbose): print("Error opening file")
                    else:
                        if (self.verbose): print("File is too big. Max 60kB supported in this version")
                        bts = json.dumps(["ERR",2]).encode()
                        udpsocket.sendto(bts,(senderIP,senderPort))
                else:
                    bts = json.dumps(["ERR",1]).encode()
                    udpsocket.sendto(bts,(senderIP,senderPort))

            elif (payload[0]=='WRQ'):
                if (self.verbose): print("Client requested to write: ",payload[1])
                bts = json.dumps(["ACK"]).encode()
                udpsocket.sendto(bts,(senderIP,senderPort))

            elif (payload[0]=='DAT'):
                if os.path.exists(payload[1]):
                    if (self.verbose): print ("File already exists ,replacing")
                    
                else:
                    writefd = open(payload[1],"a")
                    writefd.write(payload[2])
                    writefd.close()

            elif (payload[0]=='LOAD'):
                if (self.verbose): print("Client requested the computer load")
                bts = json.dumps(["LOAD",self.load]).encode()
                udpsocket.sendto(bts,(senderIP,senderPort))

            elif (payload[0]=='RLIST'):
                if (self.verbose): print("Client requested server file list")
                files = [f for f in os.listdir("./") if os.path.isfile(os.path.join("./", f))]
                bts = json.dumps(["LIST",files]).encode()
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
