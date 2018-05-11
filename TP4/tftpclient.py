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
        print("VR TP3 - Simple TFTP client 0.1")
        print()
        print("Commands:")
        print("get [remote file] [new local name] - Get a file from server and optionally rename it ")
        print("connect [ip] - connect to server on port 6969")
        print("loal - get server load in %")
        print("help - print this help")
        print("VRftp#>")

    def load(self):
        self.udpsocket.send(json.dumps(["LOAD"]).encode())
        self.udpsocket.settimeout(10)
        try:
            data, sender = self.udpsocket.recvfrom(65535)
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
        self.udpsocket.connect((self.server,self.port))

    def get(self,filename, newfilename):
        

        self.udpsocket.send(json.dumps(["RRQ", filename]).encode())
        self.udpsocket.settimeout(10)

        try:
            data, sender = self.udpsocket.recvfrom(65535)
            payload = json.loads(data.decode())
            if (payload[0]=="ERR"):
                if (payload[1]==1):
                    print("Server sent errorn: 1 - File does not exist")
                    #udpsocket.close()
                    #print("VRftp#>")
                elif (payload[1]==2):
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

        

if __name__ == '__main__':
    try:
        prob = VRftp()
        prob.scheduler()
    except KeyboardInterrupt:
        print('Exiting')
