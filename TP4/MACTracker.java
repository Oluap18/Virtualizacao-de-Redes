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
  	StatisticsCollector statistics = new StatisticsCollector();
  	SwitchPortBandwidth spb1;
  	SwitchPortBandwidth spb2;
    public Map<IPv4Address, List<MacAddress>> anycast = new HashMap();
    public Map<IPv4Address, MacAddress> ipMacs = new HashMap();
    //Guarda as informações dos pacotes a enviar, quando souber os ips e MACs dos destinos
    public List<IOFSwitch> sws = new ArrayList();
    public List<Ethernet> eths = new ArrayList();
    public List<IPv4Address> ips = new ArrayList();

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
  		// TODO Auto-generated method stub
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

    public void arpReply(IOFSwitch sw, Ethernet eth, int host, ARP arp){
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
    		l3.setSenderProtocolAddress(IPv4Address.of("10.0.0." + host));
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
            System.out.println("New target" + target + ": " + mac);
            anycast.put(target, lista);
        }
        else{
            List<MacAddress> lista = anycast.get(target);
            if(lista.contains(mac) != true){
                lista.add(mac);
                System.out.println("" + mac);
            }
        }
    }


    public void packetout(IOFSwitch sw, Ethernet eth, IPv4Address ip){
    		IPv4 ipv4 = (IPv4) eth.getPayload();
        UDP udp = (UDP) ipv4.getPayload();
        Data data = (Data) udp.getPayload();
        if(ipMacs.containsKey(ip) == false){
            System.out.println("Não tem ip");
            ips.add(ip);
            sws.add(sw);
            eths.add(eth);
            return;
        }

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

    public void updateArps(ARP arp){
      MacAddress mac = arp.getSenderHardwareAddress();
      IPv4Address ip = arp.getSenderProtocolAddress();
      if(ipMacs.containsKey(ip) != true){
          ipMacs.put(ip, mac);
          int index = ips.indexOf(ip);
          while(index != -1){
            System.out.println("Já tenho." + index);
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

    public void printDetails(IOFSwitch sw, OFMessage msg, FloodlightContext cntx, Ethernet eth){
      switch (msg.getType()) {
      case PACKET_IN:
          /* Retrieve the deserialized packet in message */


          /* Various getters and setters are exposed in Ethernet */
          MacAddress srcMac = eth.getSourceMACAddress();
          MacAddress dstMac = eth.getDestinationMACAddress();
          VlanVid vlanId = VlanVid.ofVlan(eth.getVlanID());
          //if(sw.getId().getLong() == 1) System.out.println(sw.getId());

          System.out.println("######################################");
          System.out.println("Switch " + sw.getId().getLong());

          if (eth.getEtherType() == EthType.IPv4) {

              /* We got an IPv4 packet; get the payload from Ethernet */
              IPv4 ipv4 = (IPv4) eth.getPayload();

              /* Various getters and setters are exposed in IPv4 */
              IPv4Address dstIp = ipv4.getDestinationAddress();
              IPv4Address srcIp = ipv4.getSourceAddress();

              System.out.println("IP Source: " + srcIp);
              System.out.println("IP Destination: " + dstIp);

              /*
               * Check the IP protocol version of the IPv4 packet's payload.
               */
              if (ipv4.getProtocol() == IpProtocol.TCP) {

                  System.out.println("Protocolo TCP");

              } else if (ipv4.getProtocol() == IpProtocol.UDP) {

                  System.out.println("Protocolo UDP");

              } else if(ipv4.getProtocol() == IpProtocol.ICMP){

                System.out.println("Protocolo ICMP");

              }

          } else if (eth.getEtherType() == EthType.ARP) {

              ARP arp = (ARP) eth.getPayload();
              System.out.println("Protocolo ARP");
              System.out.println("MAC source: " + srcMac);
              System.out.println("MAC destination: " + dstMac);

          } else {
              System.out.println("Unhandled ethertype");
          }
          System.out.println("######################################");
          break;
      default:
          break;
      }
    }

    /*
  	 * Overridden IOFMessageListener's receive() function//
  	 */
  	@Override
  	public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx){
  	    switch (msg.getType()) {
  	    case PACKET_IN:
            Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
            //printDetails(sw, msg, cntx, eth);

            if (eth.getEtherType() == EthType.IPv4) {
                IPv4 ipv4 = (IPv4) eth.getPayload();
                String source = "" + ipv4.getSourceAddress();
                String dest = "" + ipv4.getDestinationAddress();

                if (ipv4.getProtocol() == IpProtocol.UDP) {

                    if((sw.getId().getLong() == 2 && dest.equals("10.0.0.240")){
                        if(source.equals("10.0.0.1")){
                            host++;
                            host = host % 2;
                            IPv4Address ipv = IPv4Address.of("10.0.0." + (host+5));
                            packetout(sw, eth, ipv);
                        }
                        if(source.equals("10.0.0.2")){
                            IPv4Address ipv = IPv4Address.of("10.0.0.5");
                            packetout(sw, eth, ipv);
                        }
                    }
                }
            }

            else if(eth.getEtherType() == EthType.ARP){
                ARP arp = (ARP) eth.getPayload();
                //Guardar os ips associados a cada MAC
                updateArps(arp);
                IPv4Address target = arp.getTargetProtocolAddress();
                String str = "" + target;

                //Responder ao pedido de ARP, com um MAC não atribuido a ninguém
                if(str.equals("10.0.0.250") && (sw.getId().getLong() == 2 || sw.getId().getLong() == 9)){
                    arpAsk(sw, eth, arp, 5);
                    arpAsk(sw, eth, arp, 6);
                    arpReply(sw, eth, 250, arp);
                }
                if(str.equals("10.0.0.240") && (sw.getId().getLong() == 2 || sw.getId().getLong() == 9)){
                    arpAsk(sw, eth, arp, 5);
                    arpAsk(sw, eth, arp, 6);
                    arpReply(sw, eth, 240, arp);
                }

                //Guardar os MACs associados a cada ip anycast
                if(str.equals("10.0.0.250") && sw.getId().getLong() == 2){
                    anycastUpdate(arp);
                }


            }
  	        break;
  	    default:
  	        break;
  	    }
  	    return Command.CONTINUE;
  	}
}
