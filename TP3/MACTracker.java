package net.floodlightcontroller.mactracker;

import java.util.Collections;
import java.util.Collection;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.Set;

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
	long timebefore = 0;

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


  	public void packetout(IOFSwitch sw, OFMessage msg, FloodlightContext cntx){
        Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
    		ARP arp = (ARP) eth.getPayload();

    		host += 1;
    		host %= 2;
    		int hostfinal = host + 2;
        System.out.println(hostfinal);
    		Ethernet l2 = new Ethernet();
    		l2.setSourceMACAddress(MacAddress.of("00:00:00:00:00:0" + hostfinal));
    		l2.setDestinationMACAddress(MacAddress.of("00:00:00:00:00:01"));
        l2.setPriorityCode(eth.getPriorityCode());
    		l2.setEtherType(eth.getEtherType());


    		ARP l3 = new ARP();
        l3.setHardwareType(ARP.HW_TYPE_ETHERNET);
        l3.setProtocolType(ARP.PROTO_TYPE_IP);
        l3.setHardwareAddressLength((byte) 6);
        l3.setProtocolAddressLength((byte) 4);
        l3.setOpCode(ARP.OP_REPLY);
        l3.setSenderHardwareAddress(MacAddress.of(Ethernet.toMACAddress("00:00:00:00:00:0" + hostfinal)));
        l3.setTargetHardwareAddress(MacAddress.of(Ethernet.toMACAddress("00:00:00:00:00:01")));
    		l3.setSenderProtocolAddress(IPv4Address.of("10.0.0.250"));
    		l3.setTargetProtocolAddress(IPv4Address.of("10.0.0.1"));
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

    /*
  	 * Overridden IOFMessageListener's receive() function//
  	 */
  	@Override
  	public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx){
  	    switch (msg.getType()) {
  	    case PACKET_IN:
  	        /* Retrieve the deserialized packet in message */
  	        Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);

  	        /* Various getters and setters are exposed in Ethernet */
  	        MacAddress srcMac = eth.getSourceMACAddress();
  	        MacAddress dstMac = eth.getDestinationMACAddress();
  	        VlanVid vlanId = VlanVid.ofVlan(eth.getVlanID());
            //if(sw.getId().getLong() == 1) System.out.println(sw.getId());

  	        /*
  	         * Check the ethertype of the Ethernet frame and retrieve the appropriate payload.
  	         * Note the shallow equality check. EthType caches and reuses instances for valid types.
  	         */
			if ((System.currentTimeMillis()-timebefore)>10000) {

				for (int i=1; i<48;i++){
					spb1 = statistics.getBandwidthConsumption(DatapathId.of("00:00:00:00:00:00:00:01") , OFPort.of(i));
					if (spb1 != null) {
						logger.info("Current Link Speed {} KBps on switch 1", spb1.getLinkSpeedBitsPerSec().getValue()/8192);
						logger.info("Current RX Bandwidth {} Bps on switch 1 port {}", spb1.getBitsPerSecondRx().getValue()/8,spb1.getSwitchPort().toString());
						logger.info("Current TX Bandwidth {} Bps on switch 1 port {}", spb1.getBitsPerSecondTx().getValue()/8,spb1.getSwitchPort().toString());
					} 

				}
				for (int i=1; i<48;i++){
					spb2 = statistics.getBandwidthConsumption(DatapathId.of("00:00:00:00:00:00:00:02") , OFPort.of(i));
					if (spb2 != null) {
						logger.info("Current Link Speed {} KBps on switch 2", spb2.getLinkSpeedBitsPerSec().getValue()/8192);
						logger.info("Current RX Bandwidth {} Bps on switch 2 port {}", spb2.getBitsPerSecondRx().getValue()/8,spb2.getSwitchPort().toString());
						logger.info("Current TX Bandwidth {} Bps on switch 2 port {}", spb2.getBitsPerSecondTx().getValue()/8,spb2.getSwitchPort().toString());
					} 

				}

				timebefore = System.currentTimeMillis();
			} 

  	        if (eth.getEtherType() == EthType.IPv4) {

  	            /* We got an IPv4 packet; get the payload from Ethernet */
                IPv4 ipv4 = (IPv4) eth.getPayload();

  	            /* Various getters and setters are exposed in IPv4 */
  	            byte[] ipOptions = ipv4.getOptions();
  	            IPv4Address dstIp = ipv4.getDestinationAddress();
  	            IPv4Address srcIp = ipv4.getSourceAddress();
  	            if(sw.getId().getLong() == 1) System.out.println("Source: " + srcIp + ". Destination: " + dstIp);

  	            /*
  	             * Check the IP protocol version of the IPv4 packet's payload.
  	             */
  	            if (ipv4.getProtocol() == IpProtocol.TCP) {
  	            	  if(sw.getId().getLong() == 1) System.out.println("TCP");
  	                /* We got a TCP packet; get the payload from IPv4 */
  	                TCP tcp = (TCP) ipv4.getPayload();

  	                /* Various getters and setters are exposed in TCP */
  	                TransportPort srcPort = tcp.getSourcePort();
  	                TransportPort dstPort = tcp.getDestinationPort();
  	                short flags = tcp.getFlags();

  	                /* Your logic here! */
  	            } else if (ipv4.getProtocol() == IpProtocol.UDP) {
  	            	  if(sw.getId().getLong() == 1) System.out.println("UDP");
  	                /* We got a UDP packet; get the payload from IPv4 */
  	                UDP udp = (UDP) ipv4.getPayload();

  	                /* Various getters and setters are exposed in UDP */
  	                TransportPort srcPort = udp.getSourcePort();
  	                TransportPort dstPort = udp.getDestinationPort();

  	                /* Your logic here! */
  	            } else if(ipv4.getProtocol() == IpProtocol.ICMP){
  	            	if(sw.getId().getLong() == 1) System.out.println("ICMP");
  	            }

  	        } else if (eth.getEtherType() == EthType.ARP) {
  	        	  if(sw.getId().getLong() == 1) {
                  System.out.println("ARP");
  	        	    System.out.println("MAC source: " + srcMac + ". MAC destination: " + dstMac);
                }
  	            /* We got an ARP packet; get the payload from Ethernet */
  	            ARP arp = (ARP) eth.getPayload();
                IPv4Address target = arp.getTargetProtocolAddress();
                String str = "" + target;
                if(str.equals("10.0.0.250") && sw.getId().getLong() == 1){
                    System.out.println("Entrei");
                    packetout(sw, msg, cntx);
                    return Command.STOP;
                }


  	            /* Various getters and setters are exposed in ARP */
  	            boolean gratuitous = arp.isGratuitous();

  	        } else {
  	            /* Unhandled ethertype */
  	        }
  	        break;
  	    default:
  	        break;
  	    }
  	    return Command.CONTINUE;
  	}
}
