import java.net.*;
import java.io.*;
import java.util.*;


class Server{

  static DatagramSocket ss = null;
  static FileInputStream fis = null;
  static DatagramPacket rp, sp;
  static byte[] rd, sd;
  static String reply;
  static InetAddress ip;
  static int port;
  static HashMap<Integer, DatagramPacket> map = new HashMap<>();
  static int map_size=0;
  static int wd_size=4;
  static int wd_lft=0, wd_rgt=wd_lft+wd_size-1;
  static int TIME_OUT=2000;
  static final double LOSS_RATE = 0.2;
  public static void main(String[] args) {

    try{
      ss = new DatagramSocket(Integer.parseInt(args[0]));
      System.out.println("Server is up....");
      // read file into buffer
      rd=new byte[100];
      sd=new byte[512];
      rp = new DatagramPacket(rd,rd.length);
      ss.receive(rp);
      ip = rp.getAddress();
      port =rp.getPort();
      reply=new String(rp.getData());
      String file_name = reply.substring(7, reply.length()).trim();


      System.out.println(ip);
      System.out.println(port);
      System.out.println(file_name);

      map_size=read_file_convert_to_hashmap(file_name);

      System.out.println(map_size);
      System.out.println(map);
      
      ss.setSoTimeout(TIME_OUT);


      while (true){
        System.out.println("Send sliding window");
        send_packets(wd_lft,wd_rgt);
        System.out.println("Packet send "+wd_lft+" "+wd_rgt);
        int track=check_acknowledgement(wd_lft,wd_rgt);
        System.out.println("Last ack received "+track);
        wd_lft=track+1;
        wd_rgt=Math.min(wd_lft+wd_size-1,map_size-1);

        if (wd_lft>wd_rgt){
          System.out.println("All send");
          break;
        }
      }
    }
    catch (Exception e) {
      System.out.println(e.toString());
    }

  }

  static int read_file_convert_to_hashmap(String file_name){
    int result=0,count=0;
    try{
      fis = new FileInputStream(file_name);

      while (true){
        sd=new byte[512];
        result = fis.read(sd);
        
        if (result == -1) {
          sd = new String("RTD " + count + " END \r\n").getBytes();
          sp=new DatagramPacket(sd,sd.length,ip,port);
          map.put(count, sp);
          count++;
          break;
        } else {
        sd = new String("RTD "+ count+ " " + Base64.getEncoder().encodeToString(sd) +" \r\n").getBytes();
        sp=new DatagramPacket(sd,sd.length,ip,port);

        map.put(count,sp);
        count++;
        }

      }

    }
    catch(Exception e){
      System.out.println(e.toString());
    }
    return map.size();

  }

  static void send_packets(int i,int j) throws IOException{
    while(i<=j){
      if (Math.random() < LOSS_RATE) {
        System.out.println("Packet Loss "+ i);
      } else {
        ss.send(map.get(i));
        
      }
      i++;
    }
  }

  static int check_acknowledgement(int i, int j) throws Exception{
    int track = i - 1;
    String ack="";
    while (i <= j) {
      rp = new DatagramPacket(rd, rd.length);
      i++;
      try {
        ss.receive(rp);
      } catch (Exception e) {
        continue;
      }
      ack = new String(rp.getData());
      System.out.println(Integer.parseInt(ack.split(" ")[1]));
      track = Math.max(track, Integer.parseInt(ack.split(" ")[1]));
    }
    return track;
  }
}
