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

public class FServer {
    private static final double LOSS_RATE = 0.3;
	private static final int AVERAGE_DELAY = 100; // milliseconds

	public static void main(String[] args) {

		DatagramSocket ss = null;
		FileInputStream fis = null;

		DatagramPacket rp, sp;
		byte[] rd, sd;

		int flag = 0;

		InetAddress ip;
		int port;

		try {
			ss = new DatagramSocket(Integer.parseInt(args[0])); // setting port no;
			System.out.println("Server is Listening....");

			// read file into buffer
			int consignment;
			String strConsignment;
			int result = 0; // number of bytes read

			if (flag == 0) {
				rd = new byte[100];
				sd = new byte[512];
				rp = new DatagramPacket(rd, rd.length);
				ss.receive(rp);
				ip = rp.getAddress();
				port = rp.getPort();
				strConsignment = new String(rp.getData());
				String FileName = (strConsignment.trim());
				System.out.println("########################################");
				System.out.println();
				System.out.println("Received request for " + FileName + " filename from client ip address " + ip + " clientport "
						+ port);
				try {
					fis = new FileInputStream(FileName); // file n
				} catch (FileNotFoundException e) {
					System.out.println("File not found on server Side");
				}
				rp = null;
				sp = null;
				flag++;
			}

			Random random = new Random();

			ss.setSoTimeout(3000);

			while (true && result != -1) {

				rd = new byte[100];
				sd = new byte[512];

				rp = new DatagramPacket(rd, rd.length);

				try {
					ss.receive(rp);
				} catch (SocketTimeoutException e) {
					// TODO: handle exception
					continue;
				}

				// get client's consignment request from DatagramPacket
				ip = rp.getAddress();
				port = rp.getPort();

				strConsignment = new String(rp.getData());
				consignment = Integer.parseInt(strConsignment.trim());
				System.out.println("Recieved ACK " + strConsignment);

				// Decide whether to send, or simulate packet loss.
				if (random.nextDouble() < LOSS_RATE) {
					System.out.println("forgot CONSIGNMENT #" + consignment);
					continue;
				}

				// Simulate network delay.
				try {
					Thread.sleep((int) (random.nextDouble() * 2 * AVERAGE_DELAY));
				} catch (InterruptedException e) {
					// TODO catch block
					e.printStackTrace();
				}

				// prepare data
				result = fis.read(sd); // read next 512 byte
				if (result == -1) {
					sd = new String("END").getBytes();
					consignment = -1;
				}
				sp = new DatagramPacket(sd, sd.length, ip, port);

				ss.send(sp);

				rp = null;
				sp = null;

				System.out.println("Sent CONSIGNMENT #" + consignment);

			}

		} catch (IOException ex) {
			System.out.println(ex.getMessage());

		} finally {
			try {
				if (fis != null)
					fis.close();
			} catch (IOException ex) {
				System.out.println(ex.getMessage());
			}
		}

	}
}
