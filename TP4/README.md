# VRTP3
Virtualização de redes - TP3 parte 3

Para testar as estatisticas:
Por exemplo do H1 para H2, espera-se fluxo em duas portas de S1 e em duas portas de S2

Abrir terminar em H2 e digitar:

iperf -s

Abrir terminar em H1 e digitar:

iperf -c 10.0.0.2 -t 50 -i 10 -b 1m -f M

Enviar por 50segunds, banda de 1mbit/s exibindo a cada 10 segundos no formato de MBytes/s


Nota bene:
É necessário habilitar o módulo de statistics para o balanceamento de carga funcionar! O floodlight não coleta estes dados por default devido ao processamento necessário.

Em: floodlightdefault.properties

net.floodlightcontroller.statistics.StatisticsCollector.enable=TRUE
net.floodlightcontroller.statistics.StatisticsCollector.collectionIntervalPortStatsSeconds=5



Para o tftp client:


VR TP3 - Simple TFTP client 0.2

Commands:
get [remote file] [new local name] - Get a file from server and optionally rename it 
put [filename] - Uploads a file to the server 
connect [ip] - connect to server on port 6969
load - get server load in %
ls - get local list of files
rls - get list of files in the server
help - print this help


Ainda falta habilitar no cliente e servidor a possibilidade de transferir arquivos grandes. Isto pode causar problemas em manter o fluxo corretamente quando se faz uso de anycast.


O server parte automaticamente com o script do mininet


Todo: 
Fazer testes de balanceamento de carga juntamente com o iperf
Entender melhor a configuração do bind9 e tentar usar containers docker para correr o primário e o secundário separadamente
