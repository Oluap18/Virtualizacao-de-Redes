# VRTP3
Virtualização de redes - TP3


sudo mn --controller=remote,ip=[floodlight IP],port=6653 --switch=ovsk,protocols=OpenFlow13 --custom customtopotp3.py --topo tp3

Para testar as estatisticas:
Por exemplo do H1 para H2, espera-se fluxo em duas portas de S1 e em duas portas de S2

Abrir terminar em H2 e digitar:

iperf -s

Abrir terminar em H1 e digitar:

iperf -c 10.0.0.2 -t 50 -i 10 -b 1m -f M

Enviar por 50segunds, banda de 1mbit/s exibindo a cada 10 segundos no formato de MBytes/s

deverá aparecer a cada 10 segundos no log:

2018-04-18 21:07:58.226 INFO  [n.f.m.MACTracker] Current Link Speed 1220 KBps on switch 1

2018-04-18 21:07:58.227 INFO  [n.f.m.MACTracker] Current RX Bandwidth 669 Bps on switch 1 port 1

2018-04-18 21:07:58.227 INFO  [n.f.m.MACTracker] Current TX Bandwidth 127379 Bps on switch 1 port 1

2018-04-18 21:07:58.227 INFO  [n.f.m.MACTracker] Current Link Speed 1220 KBps on switch 1

2018-04-18 21:07:58.227 INFO  [n.f.m.MACTracker] Current RX Bandwidth 127310 Bps on switch 1 port 2

2018-04-18 21:07:58.227 INFO  [n.f.m.MACTracker] Current TX Bandwidth 669 Bps on switch 1 port 2

2018-04-18 21:07:58.228 INFO  [n.f.m.MACTracker] Current Link Speed 1220 KBps on switch 2

2018-04-18 21:07:58.228 INFO  [n.f.m.MACTracker] Current RX Bandwidth 159224 Bps on switch 2 port 1

2018-04-18 21:07:58.228 INFO  [n.f.m.MACTracker] Current TX Bandwidth 837 Bps on switch 2 port 1

2018-04-18 21:07:58.228 INFO  [n.f.m.MACTracker] Current Link Speed 1220 KBps on switch 2

2018-04-18 21:07:58.228 INFO  [n.f.m.MACTracker] Current RX Bandwidth 590 Bps on switch 2 port 2

2018-04-18 21:07:58.228 INFO  [n.f.m.MACTracker] Current TX Bandwidth 127390 Bps on switch 2 port 2

2018-04-18 21:07:58.228 INFO  [n.f.m.MACTracker] Current Link Speed 1220 KBps on switch 2

2018-04-18 21:07:58.228 INFO  [n.f.m.MACTracker] Current RX Bandwidth 0 Bps on switch 2 port 3

2018-04-18 21:07:58.228 INFO  [n.f.m.MACTracker] Current TX Bandwidth 79 Bps on switch 2 port 3

2018-04-18 21:07:58.229 INFO  [n.f.m.MACTracker] Current Link Speed 1220 KBps on switch 2

2018-04-18 21:07:58.229 INFO  [n.f.m.MACTracker] Current RX Bandwidth 0 Bps on switch 2 port 4

2018-04-18 21:07:58.229 INFO  [n.f.m.MACTracker] Current TX Bandwidth 99 Bps on switch 2 port 4

2018-04-18 21:07:58.229 INFO  [n.f.m.MACTracker] Current Link Speed 1220 KBps on switch 2

2018-04-18 21:07:58.229 INFO  [n.f.m.MACTracker] Current RX Bandwidth 0 Bps on switch 2 port 5

2018-04-18 21:07:58.229 INFO  [n.f.m.MACTracker] Current TX Bandwidth 69 Bps on switch 2 port 5
