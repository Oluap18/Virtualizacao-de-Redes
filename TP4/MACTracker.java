package net.floodlightcontroller.mactracker;

import java.util.Collections;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.Set;
import java.lang.Thread;


import java.io.*;
import java.net.*;


import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.action.*;
import org.projectfloodlight.openflow.types.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.packet.IPacket;
import net.floodlightcontroller.statistics.StatisticsCollector;
import net.floodlightcontroller.statistics.SwitchPortBandwidth;
import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.routing.ForwardingBase;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.packet.*;
import net.floodlightcontroller.core.IFloodlightProviderService;

public class MACTracker implements IOFMessageListener, IFloodlightModule {

  	protected IFloodlightProviderService floodlightProvider;
  	protected Set<Long> macAddresses;
  	protected static Logger logger;
    public int host = 0;
    boolean dnsToggle = false;
    long timebefore = 0;

  	StatisticsCollector statistics = new StatisticsCollector();
    SwitchPortBandwidth spb1;

    public Map<IPv4Address, List<MacAddress>> anycast = new HashMap();
    public Map<IPv4Address, MacAddress> ipMacs = new HashMap();

    HashMap<Integer, Long> rxmap= new HashMap<Integer, Long>();
    HashMap<Integer, Long> txmap= new HashMap<Integer, Long>();
    HashMap<Integer, Long> bwdmap= new HashMap<Integer, Long>();

    //Guarda as informações dos pacotes a enviar, quando souber os ips e MACs dos destinos
    public List<IOFSwitch> sws = new ArrayList();
    public List<Ethernet> eths = new ArrayList();
    public List<IPv4Address> ips = new ArrayList();
    //Guardar o load time dos CPUs dos file servers
    public Double fs1 = 0.0;
    public Double fs2 = 0.0;
    double bwd1=0;
    double bwd2=0;
    long linkspeed = 1000000000;




  	@Override
  	public String getName() {
  	    return MACTracker.class.getSimpleName();
  	}

  	@Override
  	public boolean isCallbackOrderingPrereq(OFType type, String name) {
  		// TODO Auto-generated method stub
  		return false;
  	}

  	@Override
  	public boolean isCallbackOrderingPostreq(OFType type, String name) {
        if(type.equals(OFType.PACKET_IN) && name.equals("forwarding")){
			return true;
		}
  		return false;
  	}

  	@Override
  	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
  		// TODO Auto-generated method stub
  		return null;
  	}

  	@Override
  	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
  		// TODO Auto-generated method stub
  		return null;
  	}

  	@Override
  	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
  	    Collection<Class<? extends IFloodlightService>> l =
  	        new ArrayList<Class<? extends IFloodlightService>>();
  	    l.add(IFloodlightProviderService.class);
  	    return l;
  	}


  	@Override
  	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
  	    floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
  	    macAddresses = new ConcurrentSkipListSet<Long>();
  	    logger = LoggerFactory.getLogger(MACTracker.class);
  	}

  	@Override
  	public void startUp(FloodlightModuleContext context) {
        floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
  	}

    public void arpAsk(IOFSwitch sw, Ethernet eth, ARP arp, int hostfinal){
        IPv4Address ip = arp.getSenderProtocolAddress();
        Ethernet l2 = new Ethernet();
        l2.setSourceMACAddress(ipMacs.get(ip));
        l2.setDestinationMACAddress(MacAddress.of("ff:ff:ff:ff:ff:ff"));
        l2.setPriorityCode(eth.getPriorityCode());
        l2.setEtherType(eth.getEtherType());


        ARP l3 = new ARP();
        l3.setHardwareType(ARP.HW_TYPE_ETHERNET);
        l3.setProtocolType(ARP.PROTO_TYPE_IP);
        l3.setHardwareAddressLength((byte) 6);
        l3.setProtocolAddressLength((byte) 4);
        l3.setOpCode(ARP.OP_REQUEST);
        l3.setSenderHardwareAddress(ipMacs.get(ip));
        l3.setTargetHardwareAddress(MacAddress.of(Ethernet.toMACAddress("00:00:00:00:00:00")));
        l3.setSenderProtocolAddress(ip);
        l3.setTargetProtocolAddress(IPv4Address.of("10.0.0." + hostfinal));
        l3.setProtocolType(arp.getProtocolType());
        l2.setPayload(l3);

        byte[] serializedData = ((IPacket) l2).serialize();

        List<OFAction> list = new ArrayList<>();
        list.add(sw.getOFFactory().actions().output(OFPort.FLOOD, 0xffFFffFF));
        OFPacketOut po = sw.getOFFactory().buildPacketOut()
                .setData(serializedData)
                .setActions(list)
                .setInPort(OFPort.CONTROLLER)
                .build();

        boolean messages = sw.write(po);
        if(messages != true){
            System.out.println("Não mandei");
        }

  	}

    public void arpReply(IOFSwitch sw, Ethernet eth, IPv4Address target, ARP arp){
        IPv4Address ip = arp.getSenderProtocolAddress();
        Ethernet l2 = new Ethernet();
        l2.setSourceMACAddress(MacAddress.of("76:f0:f0:f0:f0:f0"));
        l2.setDestinationMACAddress(ipMacs.get(ip));
        l2.setPriorityCode(eth.getPriorityCode());
        l2.setEtherType(eth.getEtherType());


        ARP l3 = new ARP();
        l3.setHardwareType(ARP.HW_TYPE_ETHERNET);
        l3.setProtocolType(ARP.PROTO_TYPE_IP);
        l3.setHardwareAddressLength((byte) 6);
        l3.setProtocolAddressLength((byte) 4);
        l3.setOpCode(ARP.OP_REPLY);
        l3.setSenderHardwareAddress(MacAddress.of(Ethernet.toMACAddress("ff:ff:ff:ff:ff:ff")));
        l3.setTargetHardwareAddress(ipMacs.get(ip));
        l3.setSenderProtocolAddress(target);
        l3.setTargetProtocolAddress(ip);
        l3.setProtocolType(arp.getProtocolType());
        l2.setPayload(l3);

        byte[] serializedData = ((IPacket) l2).serialize();

        List<OFAction> list = new ArrayList<>();
        list.add(sw.getOFFactory().actions().output(OFPort.FLOOD, 0xffFFffFF));
        OFPacketOut po = sw.getOFFactory().buildPacketOut()
                .setData(serializedData)
                .setActions(list)
                .setInPort(OFPort.CONTROLLER)
                .build();

        boolean messages = sw.write(po);
        if(messages != true){
            System.out.println("Não mandei");
        }

  	}

    public void anycastUpdate(ARP arp){
        IPv4Address target = arp.getSenderProtocolAddress();
        MacAddress mac = arp.getSenderHardwareAddress();
        if(anycast.containsKey(target) == false){
            List<MacAddress> lista = new ArrayList();
            lista.add(mac);
            //System.out.println("New target" + target + ": " + mac);
            anycast.put(target, lista);
            System.out.println("################################");
            for(MacAddress m : lista){
                System.out.println("IP Anycast: " + target + ". MAC: " + m);
            }
            System.out.println("################################");
        }
        else{
            List<MacAddress> lista = anycast.get(target);
            if(lista.contains(mac) != true){
                lista.add(mac);
                System.out.println("################################");
                for(MacAddress m : lista){
                    System.out.println("IP Anycast: " + target + ". MAC: " + m);
                }
                System.out.println("################################");
            }
        }
    }


    public void packetout(IOFSwitch sw, Ethernet eth, IPv4Address ip){
        IPv4 ipv4 = (IPv4) eth.getPayload();
        UDP udp = (UDP) ipv4.getPayload();
        Data data = (Data) udp.getPayload();
        if(ipMacs.containsKey(ip) == false){
            //System.out.println("Não tem ip");
            ips.add(ip);
            sws.add(sw);
            eths.add(eth);
            return;
        }

        //System.out.println("Swtich: "+sw);
        Ethernet l2 = new Ethernet();
        l2.setSourceMACAddress(eth.getSourceMACAddress());
        l2.setDestinationMACAddress(ipMacs.get(ip));
        l2.setEtherType(EthType.IPv4);

        IPv4 l3 = new IPv4();
        l3.setSourceAddress(ipv4.getSourceAddress());
        l3.setDestinationAddress(ip);
        l3.setProtocol(IpProtocol.UDP);
        l3.setTtl((byte) 64);

        UDP l4 = new UDP();
        l4.setSourcePort(udp.getSourcePort());
        l4.setDestinationPort(udp.getDestinationPort());

        Data l7 = new Data();
        l7.setData(data.getData());
        l4.setPayload(l7);
        l3.setPayload(l4);
        l2.setPayload(l3);

        byte[] serializedData = ((IPacket) l2).serialize();

        List<OFAction> list = new ArrayList<>();
        list.add(sw.getOFFactory().actions().output(OFPort.FLOOD, 0xffFFffFF));
    		OFPacketOut po = sw.getOFFactory().buildPacketOut()
    			    .setData(serializedData)
    			    .setActions(list)
                    .setInPort(OFPort.CONTROLLER)
    			    .build();

    		boolean messages = sw.write(po);
        if(messages != true){
            System.out.println("Não mandei");
        }
      }

    public void dnsout(IOFSwitch sw, Ethernet eth, IPv4Address ip){
        IPv4 ipv4 = (IPv4) eth.getPayload();
        UDP udp = (UDP) ipv4.getPayload();
        Data data = (Data) udp.getPayload();
        if(ipMacs.containsKey(ip) == false){
            ips.add(ip);
            sws.add(sw);
            eths.add(eth);
            return;
        }

        //System.out.println("DNSout port: "+udp.getSourcePort());
        Ethernet l2 = new Ethernet();
        l2.setSourceMACAddress(eth.getSourceMACAddress());
        l2.setDestinationMACAddress(eth.getDestinationMACAddress());
        l2.setEtherType(EthType.IPv4);

        IPv4 l3 = new IPv4();
        if ((udp.getSourcePort().getPort()==53)&&!((ipv4.getDestinationAddress().equals("10.0.0.5"))||(ipv4.getDestinationAddress().equals("10.0.0.6")))){
            l3.setSourceAddress(ip);
        }
        else{
            l3.setSourceAddress(ipv4.getSourceAddress());
        }
        l3.setDestinationAddress(ipv4.getDestinationAddress());
        l3.setProtocol(IpProtocol.UDP);
        l3.setTtl((byte) 64);

        UDP l4 = new UDP();
        l4.setSourcePort(udp.getSourcePort());
        l4.setDestinationPort(udp.getDestinationPort());

        Data l7 = new Data();
        l7.setData(data.getData());
        l4.setPayload(l7);
        l3.setPayload(l4);
        l2.setPayload(l3);

        byte[] serializedData = ((IPacket) l2).serialize();

        List<OFAction> list = new ArrayList<>();
        list.add(sw.getOFFactory().actions().output(OFPort.FLOOD, 0xffFFffFF));
        OFPacketOut po = sw.getOFFactory().buildPacketOut()
                .setData(serializedData)
                .setActions(list)
                .setInPort(OFPort.CONTROLLER)
                .build();

        boolean messages = sw.write(po);
        if(messages != true){
            System.out.println("Não mandei");
        }

  	}

    public void updateArps(ARP arp){
      MacAddress mac = arp.getSenderHardwareAddress();
      IPv4Address ip = arp.getSenderProtocolAddress();
      if(ipMacs.containsKey(ip) != true){
          ipMacs.put(ip, mac);
          int index = ips.indexOf(ip);
          while(index != -1){
            //System.out.println("Já tenho." + index);
            packetout(sws.get(index), eths.get(index), ips.get(index));
            ips.remove(index);
            eths.remove(index);
            sws.remove(index);
            index = ips.indexOf(ip);
          }
      }
      /*System.out.println("Update");
      for(IPv4Address i : ipMacs.keySet()){
        System.out.println("Mac: " + ipMacs.get(i) + ".IP: " + i);
      }*/

    }

    /*
  	 * Overridden IOFMessageListener's receive() function//
  	 */
  	@Override
  	public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
        long rxbw;
        long txbw;
        int inPort;


  	    switch (msg.getType()) {
  	    case PACKET_IN:
            Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
            OFPacketIn pktIn = (OFPacketIn) msg;


            if(eth.getEtherType() == EthType.ARP){
                ARP arp = (ARP) eth.getPayload();
                inPort  = Integer.parseInt(pktIn.getMatch().toString().split("=")[1].substring(0,pktIn.getMatch().toString().split("=")[1].length()-1));
                //Guardar os ips associados a cada MAC
                updateArps(arp);
                IPv4Address target = arp.getTargetProtocolAddress();
                String sender = "" + arp.getSenderProtocolAddress();
                String str = "" + target;

                if(sender.equals("10.0.0.1") && sw.getId().getLong() == 2){

                    if(str.equals("10.0.0.250")){
                        //atribuir mac broadcast a ip anycast
                        arpReply(sw, eth, target, arp);
                        //Saber os Macs dos fs
                        arpAsk(sw, eth, arp, 3);
                        arpAsk(sw, eth, arp, 4);
                    }
                    else if(str.equals("10.0.0.240")){
                        //atribuir mac broadcast a ip anycast
                        arpReply(sw, eth, target, arp);
                        //Saber os Macs dos dns
                        arpAsk(sw, eth, arp, 5);
                        arpAsk(sw, eth, arp, 6);
                    }

                }

                else if(sender.equals("10.0.0.2") && sw.getId().getLong() == 9){

                    if(str.equals("10.0.0.250")){
                        //Saber os Macs dos fs
                        arpAsk(sw, eth, arp, 3);
                        arpAsk(sw, eth, arp, 4);
                        //atribuir mac broadcast a ip anycast
                        arpReply(sw, eth, target, arp);
                    }
                    else if(str.equals("10.0.0.240")){
                        //Saber os Macs dos dns
                        arpAsk(sw, eth, arp, 5);
                        arpAsk(sw, eth, arp, 6);
                        //atribuir mac broadcast a ip anycast
                        arpReply(sw, eth, target, arp);
                    }

                }

                //Guardar os MACs associados a cada ip anycast
                if(sender.equals("10.0.0.250") || sender.equals("10.0.0.240")){
                    anycastUpdate(arp);
                }


            }
            else if (eth.getEtherType() == EthType.IPv4) {

                IPv4 ipv4 = (IPv4) eth.getPayload();
                String source = "" + ipv4.getSourceAddress();
                String dest = "" + ipv4.getDestinationAddress();
                //String port = "" + ipv4.getDestinationPort();

                if (ipv4.getProtocol() == IpProtocol.UDP) {

                    //Calcular os iddleTime dos cpus
                    if(sw.getId().getLong() == 1 && dest.equals("10.0.0.254") ){
                        Data data = (Data) ipv4.getPayload().getPayload();
                        String cpu = new String(data.getData());
                        if(source.equals("10.0.0.3")){
                            //System.out.println("Current load file server 1: "+ cpu);
                            fs1 = new Double(cpu);
                        }
                        else if(source.equals("10.0.0.4")){
                            //System.out.println("Current load file server 2: "+ cpu);
                            fs2 = new Double(cpu);
                        }
                    }
                    else {

                        //Distribuir pelos dns
                        if(dest.equals("10.0.0.240")){

                            //inPort  = Integer.parseInt(pktIn.getMatch().toString().split("=")[1].substring(0,pktIn.getMatch().toString().split("=")[1].length()-1));

                            if(source.equals("10.0.0.1")){
                                if (!dnsToggle) {
                                    IPv4Address ipv = IPv4Address.of("10.0.0.5");
                                    dnsToggle = true;
                                    packetout(sw, eth, ipv);
                                    return Command.STOP;
                                } else {
                                    IPv4Address ipv = IPv4Address.of("10.0.0.6");
                                    dnsToggle = false;
                                    packetout(sw, eth, ipv);
                                    return Command.STOP;
                                }

                            }
                            if(source.equals("10.0.0.2")){
                                IPv4Address ipv = IPv4Address.of("10.0.0.5");
                                packetout(sw, eth, ipv);
                                return Command.STOP;
                            }
                        }
                        //Distribuir pelos fileservers && sw.getId().getLong() == 1
                        else if(dest.equals("10.0.0.250")){

                            if ((System.currentTimeMillis()-timebefore)>5000) {
                                //System.out.println("IM now on switch "+ sw.getId());

                                for (int i=1; i<24;i++){

                                    try{
                                        spb1 = statistics.getBandwidthConsumption(DatapathId.of("00:00:00:00:00:00:00:01") , OFPort.of(i));
                                        if (spb1 != null) {
                                            rxbw = spb1.getBitsPerSecondRx().getValue();
                                            rxmap.put(i,rxbw);
                                            txbw = spb1.getBitsPerSecondTx().getValue();
                                            txmap.put(i,txbw);
                                            bwdmap.put(i,rxbw+txbw);
                                            linkspeed = spb1.getLinkSpeedBitsPerSec().getValue();
                                        }
                                    }   catch (Exception e) {
                                        //e.printStackTrace();
                                        logger.trace("Statistics is not ready to give us values now. Trying again in a few seconds.");
                                    }
                                }
                                timebefore = System.currentTimeMillis();
                                bwd1 =(double) (bwdmap.get(1)/(double)linkspeed)*100.0;
                                bwd2 =(double) (bwdmap.get(3)/(double)linkspeed)*100.0;
                            }

                            //inPort  = Integer.parseInt(pktIn.getMatch().toString().split("=")[1].substring(0,pktIn.getMatch().toString().split("=")[1].length()-1));


                            //Now we need to check with port has lower traffic and that is not from where the packet came from
                            System.out.println("Switch capability: "+ (double)linkspeed/1000000.0 + "Mbps");
                            System.out.println("Bandwidth FS1: "+ bwd1 + "%");
                            System.out.println("Bandwidth FS2: "+ bwd2 + "%");
                            System.out.println("Load FS1: "+fs1+ "%");
                            System.out.println("Load FS2: "+fs2+ "%");


                            if((fs1+bwd1 >= fs2+bwd2)){
                                IPv4Address ipv = IPv4Address.of("10.0.0.4");
                                System.out.println("Choosing server 2");
                                packetout(sw, eth, ipv);
                                return Command.STOP;
                            }
                            else{
                                IPv4Address ipv = IPv4Address.of("10.0.0.3");
                                System.out.println("Choosing server 1");
                                packetout(sw, eth, ipv);
                                return Command.STOP;
                            }
                        }
                        else if(  source.equals("10.0.0.5") || (source.equals("10.0.0.6")) )  {
                            IPv4Address ipv = IPv4Address.of("10.0.0.240");
                            dnsout(sw, eth, ipv);
                            return Command.STOP;
                        }
                    }
                }
            }
  	        break;
  	    default:
  	        break;
  	    }
  	    return Command.CONTINUE;
  	}
}
