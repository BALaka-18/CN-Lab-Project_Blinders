import java.net.*;
import java.io.*;
import java.util.*;

public class FClient {
	private static final double LOSS_RATE = 0.2;

	public static void main(String[] args) {

    	DatagramSocket cs = null;
   	 	FileOutputStream fos = null;

   	 	try {

   		 	cs = new DatagramSocket();

			byte[] rd, sd;
			String reply="",pastReply="";
			DatagramPacket sp,rp;
			int count=0;
			boolean end = false;

			// write received data into demoText1.html
			fos = new FileOutputStream("rec_"+args[2]);
			String initiate = "REQUEST" + args[2] +"\r\n";
			sd=initiate.getBytes();
			sp=new DatagramPacket(
				sd,
				sd.length,
				InetAddress.getByName(args[0]),
				Integer.parseInt(args[1])
			);
			cs.send(sp);
			while(!end)
			{
				// get next consignment
				rd=new byte[2048];
				rp=new DatagramPacket(rd,rd.length);
				cs.receive(rp);

				// concat consignment
				reply=new String(rp.getData());
				String[] split = reply.split(" ");
				// send ACK
				String ack = "ACK "+(split[1]).trim()+" \r\n";
				sd=ack.getBytes();
				sp=new DatagramPacket(
					sd,
					sd.length,
					InetAddress.getByName(args[0]),
					Integer.parseInt(args[1])
				);

				if (reply.trim().endsWith("END")){
					end = true;
					cs.send(sp);
					break;
				}

				if (Math.random() > LOSS_RATE){
					cs.send(sp);
				}else{
					System.out.println("Client not send acknowledgement");
				}

				if (reply.equals(pastReply)==true){
					continue;
				}
				pastReply=reply;

				System.out.println(reply.substring(5+split[1].length(), reply.length()-3).trim());
				System.out.println();
				System.out.println();
   			 	fos.write(Base64.getDecoder().decode(reply.substring(5+split[1].length(), reply.length()-3).trim()));

   			 	count++;
   		 	}
   	 	} catch (IOException ex) {
   		 	System.out.println(ex.getMessage());
   	 	} finally {

			try {
				if (fos != null)
					fos.close();
				if (cs != null)
					cs.close();
			} catch (IOException ex) {
				System.out.println(ex.getMessage());
			}
   	 	}
	}
}
