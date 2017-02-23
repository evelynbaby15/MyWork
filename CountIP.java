package com.evelyn;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class CountIP {

	public static void main(String[] args) throws Exception {
		String start_ip = "172.16.3.0",
				end_ip = "172.16.3.60";

		long counts = countAssignableIpByRange(start_ip, end_ip);
		System.out.println(counts);
	}

	/**
	 * Converting IPV4Address from String to long
	 * 
	 * @param ipaddr
	 *            String
	 * @return long format of string IP address
	 * @throws UnknownHostException
	 */
	public static long ipToLong(String ipaddr) throws UnknownHostException {
		try {
			InetAddress addr = InetAddress.getByName(ipaddr);
			int shiftbits = 0;
			long mask = 0;

			shiftbits = 8;
			mask = 0xff;

			byte[] octets = addr.getAddress();
			long result = 0;
			for (byte octet : octets) {
				result <<= shiftbits;
				result |= octet & mask;
			}
			return result;
		} catch (UnknownHostException e) {
			throw new UnknownHostException(ipaddr + " is NOT a valid IP address format.");
		}

	}

	/**
	 * Here will exclude network id and broadcast address by default class c subnet.
	 * @param start_ip
	 * @param end_ip
	 * @return
	 * @throws UnknownHostException
	 */
	public static long countAssignableIpByRange(String start_ip, String end_ip) throws UnknownHostException {
		// Note this default sub net mask is class c.
		final long subnet_mask = 0xFFFFFF00;
		final long hostLength = 8; 
		//hostLength = (long) (Math.log(~subnet_mask + 1) / Math.log(2)); // 這邊在算 2的幾次方會等於特定數字

		long start = ipToLong(start_ip),
				end = ipToLong(end_ip);

		if (start > end) {
			return 0;
		}

		long first0 = (start | (~subnet_mask)) + 1,
				last0 = (end & subnet_mask);

		long exclude_ip = 0;
		if (last0 >= first0) {
			exclude_ip = (((last0 - first0) >> hostLength) + 1) * 2;
		}

		long first = start & (~subnet_mask);
		if (first == 0) {
			exclude_ip++;
		}
		long last = end & (~subnet_mask);
		if (last == (~subnet_mask)) {
			exclude_ip++;
		}

		return end - start - exclude_ip + 1;

	}

}
