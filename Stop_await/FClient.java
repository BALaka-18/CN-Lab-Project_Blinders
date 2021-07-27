/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author BALAKA
 */

import java.net.*;
import java.io.*;
import java.util.*;

public class FClient {
    private static final double LOSS_RATE = 0.3;
	private static final int AVERAGE_DELAY = 100; // milliseconds

	public static void main(String[] args) {

		DatagramSocket cs = null;
		FileOutputStream fos = null;

		try {

			cs = new DatagramSocket();

			cs.setSoTimeout(3000); // set timout in ms

			Scanner sc = new Scanner(System.in);
			byte[] rd, sd, fr;
			String reply;
			DatagramPacket sp, rp;
			int count = 0;
			boolean end = false;
			int flag = 0;
			int consignmentCount = 0;

			String req = "";
			String reqArray[] = new String[0];
			String fn[]=new String[0];
			if (flag == 0) {
				System.out.println("Send File Request:");
				req = sc.nextLine();
				reqArray = req.split(" ");
				flag++;
			}
			
			if (reqArray[0].equals("REQUEST")) {
				String f=reqArray[1];
				fn=f.split("[.]");
				// System.out.println(fn[0] +"-recived."+fn[1]);
				// write received data into demoText1.html
				fos = new FileOutputStream(fn[0] +"-recived."+fn[1]);
				// rename the client side recived file;
				fr = reqArray[1].getBytes();
				sp = new DatagramPacket(fr, fr.length, InetAddress.getByName(args[0]), Integer.parseInt(args[1]));
				cs.send(sp);

				System.out.println("Requesting Requesting " + reqArray[1] + " from server"
						+ InetAddress.getByName(args[0]) + ":" + Integer.parseInt(args[1]) + " serverport");

				while (!end) {
					String ack = "" + count;

					Random random = new Random();
					// send ACK
					sd = ack.getBytes();
					sp = new DatagramPacket(sd, sd.length, InetAddress.getByName(args[0]), // ip address
							Integer.parseInt(args[1])); // port no

					// Decide whether to send, or simulate packet loss.
					if (random.nextDouble() < LOSS_RATE) {
						System.out.println("forgot ACK #" + ack);
						continue;
					}

					// Simulate network delay.
					try {
						Thread.sleep((int) (random.nextDouble() * 2 * AVERAGE_DELAY));
					} catch (InterruptedException e) {
						// TODO catch block
						e.printStackTrace();
					}
					cs.send(sp);
					System.out.println("Sent ACK " + ack);

					// get next consignment
					rd = new byte[512];
					rp = new DatagramPacket(rd, rd.length);

					try {
						cs.receive(rp);
						reply = new String(rp.getData());
						if (!reply.trim().equals("END")) {
							System.out.println("Recived CONSIGNMENT #" + consignmentCount);
							consignmentCount++;
							fos.write(rp.getData());
						} else { // if last consignment
							end = true;
						}
						count++;
					} catch (SocketTimeoutException e) {
						// TODO: Exception Handling for frame loss;
						System.out.println("Timeout");
						sd = ack.getBytes();
						sp = new DatagramPacket(sd, sd.length, InetAddress.getByName(args[0]), // ip address
								Integer.parseInt(args[1])); // port no
						cs.send(sp);

					}

				}
			} else {
				System.out.println("Invalid REQUEST FORMAT\n Give input in format of REQUEST fileName CRLF");
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
