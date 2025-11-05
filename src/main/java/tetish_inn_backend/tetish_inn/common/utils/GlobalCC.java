
package tetish_inn_backend.tetish_inn.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class GlobalCC {

	public static String getClientIpAddress(HttpServletRequest request) {
        return request.getScheme() + "://" + request.getLocalAddr();
	}

	public static String getSysIpAddress() {

		InetAddress ip;
		String hostname;
		try {
			ip = InetAddress.getLocalHost();
			hostname = ip.getHostName();

			System.out.println("Your current IP address : " + ip.getHostAddress() + "...........");
			System.out.println("Your current Hostname : " + hostname.toLowerCase() + "...........");

			return /* ip.getHostAddress(); */ hostname.toLowerCase() + ":8080";
		} catch (UnknownHostException e) {
			log.info(e.getMessage());
		}

		return "localhost";
	}

	public static String getCurrentTime() {
		SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
		Date now = new Date();
        return sdfTime.format(now);
	}

	public static String CheckNullValues(Object obj) {
		if (obj != null) {
			String myString = obj.toString();
			if (myString.trim().isEmpty()) {
				return null;
			}

			if (myString.equalsIgnoreCase("null")) {
				return null;
			}
			return myString;
		}
		return null;
	}

	public static Date formatDate(String dateString) {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			return dateFormat.parse(dateString);
		} catch (Exception e) {
            log.info(e.getMessage());
		}
		return null;
	}
}