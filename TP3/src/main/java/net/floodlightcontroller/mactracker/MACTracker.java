package net.floodlightcontroller.mactracker;

import java.util.Collections;
import java.util.Collection;
import java.util.Map;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.Set;

import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.protocol.action.*;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.TransportPort;
import org.projectfloodlight.openflow.types.VlanVid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.routing.ForwardingBase;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.packet.ARP;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.TCP;
import net.floodlightcontroller.packet.UDP;
import net.floodlightcontroller.core.IFloodlightProviderService;

public class MACTracker implements IOFMessageListener, IFloodlightModule {

  	protected IFloodlightProviderService floodlightProvider;
  	protected Set<Long> macAddresses;
  	protected static Logger logger;
  	public int host = 0;

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
    		IPv4 ipv4 = (IPv4) eth.getPayload();

    		host += 1;
    		host %= 2;
    		int hostfinal = host + 2;
        System.out.println(hostfinal);
    		Ethernet l2 = new Ethernet();
    		l2.setSourceMACAddress(eth.getSourceMACAddress());
    		l2.setDestinationMACAddress(MacAddress.BROADCAST);
    		l2.setEtherType(eth.getEtherType());

    		IPv4 l3 = new IPv4();
    		l3.setSourceAddress(ipv4.getSourceAddress());
    		l3.setDestinationAddress(IPv4.toIPv4Address("10.0.0." + hostfinal));
    		l3.setTtl((byte) 64);
    		l3.setProtocol(ipv4.getProtocol());

    		l2.setPayload(l3);
    		l3.setPayload(ipv4.getPayload());

    		byte[] serializedData = l2.serialize();

    		OFMessage po = sw.getOFFactory().buildPacketOut()
    			    .setData(serializedData)
    			    .setActions(Collections.singletonList((OFAction) sw.getOFFactory().actions().output(OFPort.FLOOD, 0xffFFffFF)))
    			    .setInPort(OFPort.CONTROLLER)
    			    .build();

    		boolean messages = sw.write(po);
        if(messages != true){
          System.out.println("Não mandei");
        }

  	}

    /*
  	 * Overridden IOFMessageListener's receive() function.
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

  	        /*
  	         * Check the ethertype of the Ethernet frame and retrieve the appropriate payload.
  	         * Note the shallow equality check. EthType caches and reuses instances for valid types.
  	         */

  	        if (eth.getEtherType() == EthType.IPv4) {

  	            /* We got an IPv4 packet; get the payload from Ethernet */
                IPv4 ipv4 = (IPv4) eth.getPayload();

  	            /* Various getters and setters are exposed in IPv4 */
  	            byte[] ipOptions = ipv4.getOptions();
  	            IPv4Address dstIp = ipv4.getDestinationAddress();
  	            IPv4Address srcIp = ipv4.getSourceAddress();
  	            String str = "" + dstIp;
                if(str.equals("10.0.0.250")){
                  packetout(sw, msg, cntx);
                  return Command.STOP;
                }

  	            System.out.println("Source: " + srcIp + ". Destination: " + dstIp);

  	            /*
  	             * Check the IP protocol version of the IPv4 packet's payload.
  	             */System.out.println(IpProtocol.TCP);
  	            if (ipv4.getProtocol() == IpProtocol.TCP) {
  	            	System.out.println("TCP");
  	                /* We got a TCP packet; get the payload from IPv4 */
  	                TCP tcp = (TCP) ipv4.getPayload();

  	                /* Various getters and setters are exposed in TCP */
  	                TransportPort srcPort = tcp.getSourcePort();
  	                TransportPort dstPort = tcp.getDestinationPort();
  	                short flags = tcp.getFlags();

  	                /* Your logic here! */
  	            } else if (ipv4.getProtocol() == IpProtocol.UDP) {
  	            	System.out.println("UDP");
  	                /* We got a UDP packet; get the payload from IPv4 */
  	                UDP udp = (UDP) ipv4.getPayload();

  	                /* Various getters and setters are exposed in UDP */
  	                TransportPort srcPort = udp.getSourcePort();
  	                TransportPort dstPort = udp.getDestinationPort();

  	                /* Your logic here! */
  	            } else if(ipv4.getProtocol() == IpProtocol.ICMP){
  	            	System.out.println("ICMP");
  	            }

  	        } else if (eth.getEtherType() == EthType.ARP) {
  	        	System.out.println("ARP");
  	        	System.out.println("MAC source: " + srcMac + ". MAC destination: " + dstMac);
  	            /* We got an ARP packet; get the payload from Ethernet */
  	            ARP arp = (ARP) eth.getPayload();

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