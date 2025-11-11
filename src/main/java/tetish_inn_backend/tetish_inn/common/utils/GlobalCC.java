
package tetish_inn_backend.tetish_inn.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import tetish_inn_backend.tetish_inn.modules.user.User;
import tetish_inn_backend.tetish_inn.modules.user.UserDetailsImpl;
import tetish_inn_backend.tetish_inn.modules.user.UserDetailsServiceImpl;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class GlobalCC {
    public static User getUserFromContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof UserDetailsImpl userDetails) {
                return userDetails.getUser();
            }
        }

        return null;
    }

    public static User getCurrentUser() {
        User user = getUserFromContext();
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid or expired expired");
        }
        return user;
    }

    public static Optional<String> extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return Optional.of(header.substring(7));
        }
        return Optional.empty();
    }

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