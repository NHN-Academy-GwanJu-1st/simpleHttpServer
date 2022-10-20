package com.nhnacademy.test2;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.StringTokenizer;

public class SimpleServer implements Runnable {

    private static final int PORT = 12345;
    private static final String ROOT_DIRECTORY = "D:\\NHNAcademy 강의자료\\http-https\\simple-httpd\\simpleHttpd\\src\\main\\resources";

//    private static String urlParameter = "/";

    private Socket socket;

    public SimpleServer(Socket socket) {
        this.socket = socket;
    }

    public static void main(String[] args) {

        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server Start");

            while (true) {
                SimpleServer myServer = new SimpleServer(serverSocket.accept());
                System.out.println("Server Connect Success!");

                Thread thread = new Thread(myServer);
                thread.start();

            }
        } catch (IOException e) {
            throw new RuntimeException("Server Connect Fail!");
        }
    }

    @Override
    public void run() {

        try {

            StringBuilder sb;

            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            BufferedOutputStream dataOut = new BufferedOutputStream(socket.getOutputStream());

            StringTokenizer st = new StringTokenizer(input.readLine());

            /* 요청 HTTP Method  ex: GET / (url parameter) HTTP/1.1 */
            String httpMethod = st.nextToken().toUpperCase();
            System.out.println(httpMethod);

            /* url Parameter ex: localhost:12345/( urlParameter ) */
            String urlParameter = st.nextToken().toLowerCase();
            System.out.println("requestParameter : " + urlParameter);


//            if (!requestParameter.equals("/")) {
//                urlParameter = urlParameter + requestParameter;
//            }
//            System.out.println(urlParameter);

//            if (urlParameter.equals("/")) {
//                File dir = new File(ROOT_DIRECTORY);
//                String[] filenames = dir.list();
//                for (int i = 0; i < filenames.length; i++) {
//                    System.out.println("file: " + filenames[i]);
//                }

                sb = htmlBuilder(urlParameter);
                ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(sb.toString());

                int contentLength = byteBuffer.limit();
                byte[] content = new byte[contentLength];
                byteBuffer.get(content, 0, contentLength);
//            }

            out.println("HTTP/1.1 200 OK");
            out.println("Date: " + new Date());
            out.println("Content-Type: text/html");
            out.println("Content-Length: " + contentLength);
            out.println("Server: gunicorn/19.9.0");
            out.println("Access-Control-Allow-Origin: *");
            out.println("Access-Control-Allow-Credentials: true");

            out.println();
            out.flush();

            dataOut.write(content);
            dataOut.flush();

            input.close();
            out.close();
            dataOut.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static StringBuilder htmlBuilder(String urlParameter) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>");
        sb.append("<html>");
        sb.append("   <head>");
        sb.append("       <meta charset=\"UTF-8\">");
        sb.append("       <title>MyeongGwan Simple Http Server</title>");
        sb.append("   </head>");
        sb.append("   <body>");
        sb.append("           <ul>");

//        if (!urlParameter.equals("/") || !urlParameter.equals("")) {
//            urlParameter = ROOT_DIRECTORY + urlParameter;
//        } else {
//            urlParameter = ROOT_DIRECTORY;
//        }

        System.out.println("htmlBuilder() urlParam : " + urlParameter);

        String currentDir = System.getProperty("user.dir") + urlParameter;
        File dir = new File(currentDir);
        String[] fileNames = dir.list();

        if (fileNames != null) {
            for (int i = 0; i < fileNames.length; i++) {
                File file = new File(currentDir + fileNames[i]);
                String fileName = fileNames[i];
                if (file.isDirectory()) {
                    fileName = fileNames[i] + "/";
                }
                sb.append("<li><a href=" + fileName + ">" + fileName + "</li>");
            }
        }
        sb.append("           </ul>");
        sb.append("   </body>");
        sb.append("</html>");

        return sb;
    }

//    private static void
}