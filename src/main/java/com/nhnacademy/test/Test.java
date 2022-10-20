package com.nhnacademy.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Date;

public class Test {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12345);

        Socket connect = serverSocket.accept();

        StringBuilder sb = new StringBuilder();

        BufferedReader in = new BufferedReader(new InputStreamReader(connect.getInputStream()));

        PrintWriter pw = new PrintWriter(connect.getOutputStream());

        /* body 부분 */

        sb.append("<!DOCTYPE html>");
        sb.append("<html>");
        sb.append("   <head>");
        sb.append("       <meta charset=\"UTF-8\">");
        sb.append("       <title>Example</title>");
        sb.append("   </head>");
        sb.append("   <body>");
        sb.append("       <h1>Hello, HttpServer!!!</h1>");
        sb.append("   </body>");
        sb.append("</html>");

        ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(sb.toString());
        int contentLength = byteBuffer.limit();
        byte[] content = new byte[contentLength];
        byteBuffer.get(content, 0, contentLength);

        /* header 설정 */
        pw.println("HTTP/1.1 200 OK");
        pw.println("Date: " + new Date());
        pw.println("Content-Type: text/html;charset=UTF-8");
        pw.println("Content-Length: " + contentLength);
        pw.println("Server: gunicorn/19.9.0");
        pw.println("Access-Control-Allow-Origin: *");
        pw.println("Access-Control-Allow-Credentials: true");

        pw.println();

        pw.println(content);
        pw.println();
        pw.flush();

    }
}
