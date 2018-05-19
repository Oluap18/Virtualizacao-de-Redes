#!/usr/bin/env python3
 
import  _thread, sys, time, datetime, socket, json, os
from time import sleep

class VRftp:

    def __init__(self, port=6969):
        self.port = port
        self.on = True
        self.timeout = 10
        self.verbose = True
        self.server = "127.0.0.1"
        self.udpsocket = ()

    def scheduler(self):
        #try:
            #_thread.start_new_thread(self.udp_listener, ())
        self.prompt() #anchor for the threads
        #except:
        #    print("Scheduling error!")

    def prompt(self):
        try:
            while self.on:
                inp = input("VRftp#>")
                command = inp.split()
                if len(command) > 0:
                    if command[0] == 'help':
                        self.printhelp()
                    elif len(command)==1 and command[0] == 'clear':
                        print("\033c")
                    elif len(command)==1 and command[0] == 'load':
                        _thread.start_new_thread(self.load, ())
                    elif len(command)==2 and command[0] == 'connect':
                        self.server = command[1]
                        _thread.start_new_thread(self.connect, ())
                    elif len(command)==2 and command[0] == 'get':
                        _thread.start_new_thread(self.get, (command[1],command[1]))
                    elif len(command)==3 and command[0] == 'get':
                        _thread.start_new_thread(self.get, (command[1],command[2]))
                    elif len(command)==2 and command[0] == 'put':
                        _thread.start_new_thread(self.put, (command[1],))
                    elif len(command)==1 and command[0] == 'ls':
                        _thread.start_new_thread(self.llist, ())
                    elif len(command)==1 and command[0] == 'rls':
                        _thread.start_new_thread(self.rlist, ())
                    elif command[0] == 'quit':
                        self.on = False
                        print("Shutting Down")
                    else:
                        print("Invalid command!")
                        self.printhelp()
        except EOFError:
            self.on = False
            print("Shutting Down")
  
    def printhelp(self):
        print()
        print("VR TP3 - Simple TFTP client 0.2")
        print()
        print("Commands:")
        print("get [remote file] [new local name] - Get a file from server and optionally rename it ")
        print("connect [ip] - connect to server on port 6969")
        print("loal - get server load in %")
        print("help - print this help")
        print("VRftp#>")

    def llist(self):
        files = [f for f in os.listdir("./") if os.path.isfile(os.path.join("./", f))]
        print(files)
        print("VRftp#>")

    def rlist(self):
        addrinfo = socket.getaddrinfo('localhost', None)[0]
        rlistudpsocket = socket.socket(addrinfo[0], socket.SOCK_DGRAM)
        rlistudpsocket.sendto(json.dumps(["RLIST"]).encode(),(self.server,self.port))
        rlistudpsocket.settimeout(10)
        try:
            data, sender = rlistudpsocket.recvfrom(65535)
            payload = json.loads(data.decode())
            if (payload[0]=="LIST"):
                print(payload[1])
            else:
                print("Got a malformed packet")
        except:
            print("Connection timed out")
        print("VRftp#>")

    def load(self):
        addrinfo = socket.getaddrinfo('localhost', None)[0]
        loadudpsocket = socket.socket(addrinfo[0], socket.SOCK_DGRAM)
        loadudpsocket.sendto(json.dumps(["LOAD"]).encode(),(self.server,self.port))
        loadudpsocket.settimeout(10)
        try:
            data, sender = loadudpsocket.recvfrom(65535)
            payload = json.loads(data.decode())
            if (payload[0]=="LOAD"):
                print("Server load: ",payload[1])
            else:
                print("Something went wrong. :(")
        except:
            print("Connection timed out")

    def connect(self):
        addrinfo = socket.getaddrinfo('localhost', None)[0]
        self.udpsocket = socket.socket(addrinfo[0], socket.SOCK_DGRAM)
        #self.udpsocket.connect((self.server,self.port))

    def get(self,filename, newfilename):
        addrinfo = socket.getaddrinfo('localhost', None)[0]
        getudpsocket = socket.socket(addrinfo[0], socket.SOCK_DGRAM)
        getudpsocket.sendto(json.dumps(["RRQ", filename]).encode(),(self.server,self.port))
        getudpsocket.settimeout(10)

        try:
            data, sender = getudpsocket.recvfrom(65535)
            payload = json.loads(data.decode())
            if (payload[0]=="ERR"):
                if (payload[1]==1):
                    print("Server sent errorn: 1 - File does not exist")
                    #udpsocket.close()
                    #print("VRftp#>")
                if (payload[1]==2):
                    print("Server sent errorn: 2 - File is too big")
                    #udpsocket.close()
                    #print("VRftp#>")
                else:
                    print("Server sent errorn: ", payload[1]) 
            elif (payload[0]=="DAT"):
                if os.path.exists(newfilename):
                    print ("File already exists")
                    
                else:
                    writefd = open(newfilename,"a")
                    writefd.write(payload[2])
                    writefd.close()

        except:
            print("Connection timed out")
        #print("VRftp#>")


    def put(self,filename):
        addrinfo = socket.getaddrinfo('localhost', None)[0]
        putudpsocket = socket.socket(addrinfo[0], socket.SOCK_DGRAM)
        putudpsocket.sendto(json.dumps(["WRQ", filename]).encode(),(self.server,self.port))
        putudpsocket.settimeout(10)

        try:
            data, sender = putudpsocket.recvfrom(65535)
            payload = json.loads(data.decode())
            senderIP = (str(sender[0]))
            senderPort = (sender[1])
            if (payload[0]=="ACK"):
                if os.path.exists(filename):
                    if os.path.getsize(filename) < 61440:
                        try:
                            readfd = open(str(filename),"r") 
                            self.sendfile(readfd, putudpsocket, senderIP, senderPort,filename) 
                            print("File sent")
                        except:
                            print("Error opening file")
                    else:
                        print("File is too big. Max 60kB supported in this version")

            else:
                print(payload[0])
                print ("Server didn't respond")

        except:
            print("Connection timed out")
        #print("VRftp#>")

        
    def sendfile(self, sendfd, socket, ip , port, filename):  
        #sendfd.seek(0,2)
        #size = fileobject.tell()
        #print(size)
        data = sendfd.read(60000) 
        bts = json.dumps(["DAT",filename,data]).encode()
        socket.sendto(bts,(ip,port))


if __name__ == '__main__':
    try:
        prob = VRftp()
        prob.scheduler()
    except KeyboardInterrupt:
        print('Exiting')
