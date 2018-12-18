package com.github.nkonev.blog.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketUtils {
    public static boolean isTcpPortFree(String host, int port) {
        /*try (ServerSocket serverSocket = new ServerSocket()) {
            // setReuseAddress(false) is required only on OSX,
            // otherwise the code will not work correctly on that platform
            serverSocket.setReuseAddress(false);
            serverSocket.bind(new InetSocketAddress(InetAddress.getByName(host), port), 1);
            return true;
        } catch (Exception ex) {
            return false;
        }*/
        try (Socket ignored = new Socket(host, port)) {
            return false;
        } catch (IOException ignored) {
            return true;
        }
    }
}
