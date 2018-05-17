import subprocess, socket, time

while(True):
    result = subprocess.check_output("mpstat", shell=True).decode()
    res = result.split(" ")
    iddl = res[len(res)-1].split("\n")[0]
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.sendto(iddl.encode(), ("10.0.0.254", 9999))
    time.sleep(5)
