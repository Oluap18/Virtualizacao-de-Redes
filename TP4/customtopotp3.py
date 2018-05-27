#!/usr/bin/python

from mininet.node import CPULimitedHost
from mininet.topo import Topo
from mininet.net import Mininet
from mininet.log import setLogLevel, info
from mininet.node import RemoteController
from mininet.cli import CLI

import os, sys


class TP3(Topo):
    def __init__(self, **opts):
        """Create custom topo."""
        super(TP3, self).__init__(**opts)

        # Add hosts and switches
        h1 = self.addHost( 'h1' , ip='10.0.0.1' )
        h2 = self.addHost( 'h2' , ip='10.0.0.2' )
        fs1 = self.addHost( 'fs1', ip='10.0.0.3' )
        fs2 = self.addHost( 'fs2', ip='10.0.0.4' )
        dns1 = self.addHost( 'dns1', ip='10.0.0.5' )
        dns2 = self.addHost( 'dns2', ip='10.0.0.6' )


        # Adding switches
        s1 = self.addSwitch( 's1' )
        s2 = self.addSwitch( 's2' )
        s3 = self.addSwitch( 's3' )
        s4 = self.addSwitch( 's4' )
        s5 = self.addSwitch( 's5' )
        s6 = self.addSwitch( 's6' )
        s7 = self.addSwitch( 's7' )
        s8 = self.addSwitch( 's8' )
        s9 = self.addSwitch( 's9' )

        # Add links
        self.addLink( fs1, s1 )
        self.addLink( fs1, s1 )
        self.addLink( fs2, s1 )
        self.addLink( fs2, s1 )

        self.addLink( h1, s2 )
        self.addLink( h2, s9 )

        self.addLink( dns1, s3 )
        self.addLink( dns1, s3 )
        self.addLink( dns2, s7 )
        self.addLink( dns2, s7 )

        self.addLink( s1, s2 )
        self.addLink( s2, s3 )
        self.addLink( s1, s4 )
        self.addLink( s2, s5 )
        self.addLink( s3, s6 )
        self.addLink( s4, s5 )
        self.addLink( s5, s6 )
        self.addLink( s4, s7 )
        self.addLink( s5, s8 )
        self.addLink( s6, s9 )
        self.addLink( s7, s8 )
        self.addLink( s8, s9 )


def run(opt=0):
    c = RemoteController('c', '127.0.0.1', 6653)
    net = Mininet(topo=TP3(), host=CPULimitedHost, controller=None)
    net.addController(c)

    fs1 = net.get('fs1')
    fs2 = net.get('fs2')
    dns1 = net.get('dns1')
    dns2 = net.get('dns2')

    fs1.cmd('ifconfig fs1-eth1 10.0.0.250 netmask 255.0.0.0')
    fs2.cmd('ifconfig fs2-eth1 10.0.0.250 netmask 255.0.0.0')

    if opt:
        print('\nChecking dependencies...\n')
        #os.system('apt-get update')
        #os.system('apt-get install bind9 -y')
    else:
        print('\nChecking dependencies...\n')
        #os.system('apt-get update > /dev/null')
        #os.system('apt-get install bind9 -y > /dev/null')
    dns1.cmd('ifconfig dns1-eth1 10.0.0.240 netmask 255.0.0.0')
    dns2.cmd('ifconfig dns2-eth1 10.0.0.240 netmask 255.0.0.0')
    dns1.cmd('xterm -T "DNS1" -hold -e /usr/sbin/named -c /etc/bind/dns1/conf/named.conf &')
    dns2.cmd('xterm -T "DNS2" -hold -e /usr/sbin/named -c /etc/bind/dns2/conf/named.conf &')

    fs1.cmd('cd fs1')
    fs1.cmd('xterm -T "FTP1" -hold -e ../tftpserver.py -l 10 -b 10.0.0.3 &') #o valor do -l representa um valor simulado de carga para testes de balanceamente

    fs2.cmd('cd fs2')
    fs2.cmd('xterm -T "FTP2" -hold -e ../tftpserver.py -l 30 -b 10.0.0.4 &') #o valor do -l representa um valor simulado de carga para testes de balanceamente

    net.start()
    CLI(net)
    net.stop()


if __name__ == '__main__':
    setLogLevel('info')
    if len(sys.argv) > 1:
        if sys.argv[1] == '-v':
            run(1)
    else:
        run()
    os.system("sudo mn -c")
