package com.nhnacademy.simplehttpd;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

public class JavaHTTPServer implements Runnable{

    static final File WEB_ROOT = new File("");
    static final String DEFAULT_FILE = "D:\\NHNAcademy 강의자료\\http-https\\simple-httpd\\simpleHttpd\\src\\main\\resources\\document_root\\index.html";

    // 404
    static final String FILE_NOT_FOUND = "D:\\NHNAcademy 강의자료\\http-https\\simple-httpd\\simpleHttpd\\src\\main\\resources\\404.html";

    // 405
    static final String METHOD_NOT_ALLOWED = "D:\\NHNAcademy 강의자료\\http-https\\simple-httpd\\simpleHttpd\\src\\main\\resources\\405.html";

    // 403
    static final String FORBIDDEN = "D:\\NHNAcademy 강의자료\\http-https\\simple-httpd\\simpleHttpd\\src\\main\\resources\\403.html";

    // 409
    static final String CONFLICT = "D:\\NHNAcademy 강의자료\\http-https\\simple-httpd\\simpleHttpd\\src\\main\\resources\\409.html";


    static final int PORT = 12345;

    static final boolean verbos = true;

    private Socket connect;

    public JavaHTTPServer(Socket socket) {
        connect = socket;
    }

    public static void main(String[] args) throws IOException {
        try {
            ServerSocket serverConnect = new ServerSocket(PORT);
            System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");

            // we listen until user halts server execution
            while (true) {
                JavaHTTPServer myServer = new JavaHTTPServer(serverConnect.accept());

                if (verbos) {
                    System.out.println("Connecton opened. (" + new Date() + ")");
                }

                // create dedicated thread to manage the client connection
                Thread thread = new Thread(myServer);
                thread.start();
            }

        } catch (IOException e) {
            System.err.println("Server Connection error : " + e.getMessage());
        }
    }

    @Override
    public void run() {

        BufferedReader br = null;
        PrintWriter pw = null;
        BufferedOutputStream dataOut = null;
        String fileRequest = null;

        try {
            br = new BufferedReader(new InputStreamReader(connect.getInputStream()));

            pw = new PrintWriter(connect.getOutputStream());

            dataOut = new BufferedOutputStream(connect.getOutputStream());

            String input = br.readLine();
            System.out.println("input: " + input);
            StringTokenizer parse = new StringTokenizer(input);

            String method = parse.nextToken().toUpperCase();    // get http method

            fileRequest = parse.nextToken().toLowerCase();      // get file request

            // 일단 GET만 구현 다른 method는 안됨 501 서버에러
            if (!method.equals("GET")) {
                if (verbos) {
                    System.out.println("501 Not Implemented : " + method + " method.");
                }

                //    public File(File parent, String child) {
                File file = new File(WEB_ROOT, METHOD_NOT_ALLOWED);
                int fileLength = (int) file.length();
                String contentType = "text/html";

                // 파일 읽어오기
                byte[] fileData = readFileData(file, fileLength);

                pw.println("HTTP/1.1 501 Not Implemented");
                pw.println("Date: " + new Date());
                pw.println("Content-Type: " + contentType);
                pw.println("Content-Length: " + fileLength);
                pw.println("Server: gunicorn/19.9.0");
                pw.println("Access-Control-Allow-Origin: *");
                pw.println("Access-Control-Allow-Credentials: true");
                pw.println();

                pw.flush();

                dataOut.write(fileData);
                dataOut.flush();
            } else {

                if (fileRequest.endsWith("/")) {
                    fileRequest += DEFAULT_FILE;
                }


                String filter = fileRequest.replaceAll("/", "");
                System.out.println("fileRequest : " + fileRequest);
                System.out.println("DEFAULT_FILE : " + DEFAULT_FILE);


                File file = new File(fileRequest);

                int fileLength = (int) file.length();
                String contentType = getContentType(fileRequest);

                if (method.equals("GET")) {

                    byte[] fileData = readFileData(file, fileLength);

                    pw.println("HTTP/1.1 200 OK");
                    pw.println("Date: " + new Date());
                    pw.println("Content-Type: " + contentType);
                    pw.println("Content-Length: " + fileLength);
                    pw.println("Server: gunicorn/19.9.0");
                    pw.println("Access-Control-Allow-Origin: *");
                    pw.println("Access-Control-Allow-Credentials: true");

                    pw.println();
                    pw.flush();

                    dataOut.write(fileData);
                    dataOut.flush();
                }

                if (verbos) {
                    System.out.println("File " + fileRequest + " of type " + contentType + " returned");
                }
            }

        } catch (FileNotFoundException e) {
            try {
                fileNotFound(pw, dataOut, fileRequest);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                br.close();
                pw.close();
                dataOut.close();
                connect.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Connection closed");
        }
    }

    public byte[] readFileData(File file, int fileLength) throws IOException {
        FileInputStream fileInputStream = null;
        byte[] fileData = new byte[fileLength];

        try {
            System.out.println("readFileData() 파일 절대경로" + file.getAbsolutePath());
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(fileData);
        } finally {
            if (fileInputStream != null)
                fileInputStream.close();
        }

        return fileData;
    }

    public String getContentType(String fileRequest) {
        if (fileRequest.endsWith(".html")) {
            return "text/html";
        } else {
            return "text/plain";
        }
    }

    public void fileNotFound(PrintWriter pw, OutputStream dataOut, String fileRequest) throws IOException {
        File file = new File(FILE_NOT_FOUND);
        int fileLength = (int) file.length();
        String contentType = "text/html";
        byte[] fileData = readFileData(file, fileLength);

        pw.println("HTTP/1.1 404 Not Found");
        pw.println("Date: " + new Date());
        pw.println("Content-Type: " + contentType);
        pw.println("Content-Length: " + fileLength);
        pw.println("Server: gunicorn/19.9.0");
        pw.println("Access-Control-Allow-Origin: *");
        pw.println("Access-Control-Allow-Credentials: true");
        pw.println();

        pw.flush();

        dataOut.write(fileData, 0, fileLength);
        dataOut.flush();

        System.out.println("File " + fileRequest + " not found");
    }


}
