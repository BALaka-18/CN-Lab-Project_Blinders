import java.net.*;
import java.io.*;
import java.util.*;

class Client{

  static DatagramSocket cs = null;
  static FileOutputStream fos = null;
  static InetAddress ip;
  static byte[] rd, sd;
  static String reply;
  static DatagramPacket sp,rp;
  static int port;
  static int track=-1;
  static int last_track=-1;
  static final double LOSS_RATE = 0.1;
  public static void main(String[] args) {

    try{
      cs = new DatagramSocket();
      port=Integer.parseInt(args[1]);
      ip = InetAddress.getByName(args[0]);
      // write received data into demoText1.html
      fos = new FileOutputStream("rec_"+args[2]);
      String initiate = "REQUEST" + args[2] +"\r\n";
      sd=initiate.getBytes();
      sp=new DatagramPacket(
        sd,
        sd.length,
        ip,
        port
      );

      cs.send(sp);
      receive_packets_send_acknowledgement();

    }catch (Exception e) {
      System.out.println(e.toString());
    }

  }

  static void receive_packets_send_acknowledgement() throws IOException{
    while(true){
      rd=new byte[2048];
      rp=new DatagramPacket(rd,rd.length);
      try{
        cs.receive(rp);
      }catch(Exception e){
        System.out.println(e.toString());
      }

      // concat consignment
      reply=new String(rp.getData());
      String[] split = reply.split(" ");
      

      String ack = "ACK "+(split[1]).trim()+" \r\n";
      track=Integer.parseInt(split[1]);
      if (track==last_track+1){
        last_track = track;
        System.out.println(track);
        sd = ack.getBytes();
        
        sp=new DatagramPacket(
          sd,
          sd.length,
          ip,
          port
        );

        String file_data = reply.substring(5+split[1].length(), reply.length()-3).trim();
        if (file_data.equals("END")) {
          cs.send(sp);
          break;
        }

        fos.write(Base64.getDecoder().decode(file_data));
        if (Math.random() < LOSS_RATE){          
          System.out.println("Ack lost for Packet "+ track);
        } else {
          cs.send(sp);
        }
      }
    }
  }
}
