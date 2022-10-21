package com.nhnacademy.simplehttpd;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.StringTokenizer;

public class SimpleServer implements Runnable {

    private static final int PORT = 12345;
    private static final String ROOT_DIRECTORY = "D:\\NHNAcademy 강의자료\\http-https\\simple-httpd\\simpleHttpd";
    private static final String FILE_NOT_FOUND = "D:\\NHNAcademy 강의자료\\http-https\\simple-httpd\\simpleHttpd\\src\\main\\resources\\404.html";

    private static final String FORIDDEN = "D:\\NHNAcademy 강의자료\\http-https\\simple-httpd\\simpleHttpd\\src\\main\\resources\\403.html";

    private static final String NO_CONTENT = "D:\\NHNAcademy 강의자료\\http-https\\simple-httpd\\simpleHttpd\\src\\main\\resources\\204.html";


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

    static BufferedReader input;
    static PrintWriter out;
    static BufferedOutputStream dataOut;

    @Override
    public void run() {

        try {

            /* HTML 파일 만들기 위한 StringBuilder */
            StringBuilder sb;

//            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//            PrintWriter out = new PrintWriter(socket.getOutputStream());
//            BufferedOutputStream dataOut = new BufferedOutputStream(socket.getOutputStream());
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream());
            dataOut = new BufferedOutputStream(socket.getOutputStream());


            String requestHeader = input.readLine();
            System.out.println(requestHeader);
            StringTokenizer st = new StringTokenizer(requestHeader);

            /* 요청 HTTP Method  ex: GET / (url parameter) HTTP/1.1 */
            String httpMethod = st.nextToken().toUpperCase();
            System.out.println(httpMethod);

            /* url Parameter ex: localhost:12345/( urlParameter ) */
            String urlParameter = st.nextToken().toLowerCase();
            System.out.println("requestParameter : " + urlParameter);


            String url = input.readLine().split(" ")[1];
            System.out.println(url);

            if (httpMethod.equals("GET")) {
                httpGetHandler(urlParameter);
            } else if (httpMethod.equals("POST")) {
                httpPostHandler(urlParameter);
            } else if (httpMethod.equals("DELETE")) {
                httpDeleteHandler(urlParameter);
            }

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

    public static byte[] readFileData(File file, int fileLength) throws IOException {
        FileInputStream fileInputStream = null;
        byte[] fileData = new byte[fileLength];

        try {
            System.out.println("readFileData() 파일 절대경로" + file.getAbsolutePath());
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(fileData);
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            fileNotFound();
        }

        return fileData;
    }

    public static void fileNotFound() throws IOException {
        File file = new File(FILE_NOT_FOUND);
        int fileLength = (int) file.length();
        String contentType = "text/html";
        byte[] fileData = readFileData(file, fileLength);

        out.println("HTTP/1.1 404 Not Found");
        out.println("Date: " + new Date());
        out.println("Content-Type: " + contentType);
        out.println("Content-Length: " + fileLength);
        out.println("Server: gunicorn/19.9.0");
        out.println("Access-Control-Allow-Origin: *");
        out.println("Access-Control-Allow-Credentials: true");
        out.println();

        out.flush();

        dataOut.write(fileData, 0, fileLength);
        dataOut.flush();

    }


    public static void httpGetHandler(String urlParameter) throws IOException {

        StringBuilder sb;

        // 권한 없는 파일 처리 ex: java
        if (urlParameter.endsWith("/")) {

            sb = htmlBuilder(urlParameter);
            ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(sb.toString());

            int contentLength = byteBuffer.limit();
            byte[] content = new byte[contentLength];
            byteBuffer.get(content, 0, contentLength);

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

        } else if (urlParameter.endsWith(".html")) {
            File file = new File(ROOT_DIRECTORY, urlParameter);
            int fileLength = (int) file.length();
            String contentType = "text/html";

            byte[] fileData = readFileData(file, fileLength);

            out.println("HTTP/1.1 200 OK");
            out.println("Date: " + new Date());
            out.println("Content-Type: text/html");
            out.println("Content-Length: " + fileLength);
            out.println("Server: gunicorn/19.9.0");
            out.println("Access-Control-Allow-Origin: *");
            out.println("Access-Control-Allow-Credentials: true");

            out.println();
            out.flush();

            dataOut.write(fileData);
            dataOut.flush();

        } else if (urlParameter.endsWith(".java")) {        // 읽기 권한이 없는 파일 (ex: java 파일 등등...)
            foriddenError();
        } else {
            fileNotFound();
        }
    }

    public static void foriddenError() throws IOException {
        File file = new File(FORIDDEN);
        int fileLength = (int) file.length();
        String contentType = "text/html";
        byte[] fileData = readFileData(file, fileLength);

        out.println("HTTP/1.1 403 Not Found");
        out.println("Date: " + new Date());
        out.println("Content-Type: " + contentType);
        out.println("Content-Length: " + fileLength);
        out.println("Server: gunicorn/19.9.0");
        out.println("Access-Control-Allow-Origin: *");
        out.println("Access-Control-Allow-Credentials: true");
        out.println();

        out.flush();

        dataOut.write(fileData, 0, fileLength);
        dataOut.flush();
    }


    private static void httpPostHandler(String urlParameter) throws IOException {

        /* POST /src/main/D://test.html HTTP/1.1 */

        /* urlParameter = "/src/main/D://test.html/" */

        String[] split = urlParameter.split(":");
        char driveName = split[0].charAt(split[0].length() - 1);

        // D://test.html
        String fileName = Character.toString(driveName).toUpperCase() + ":" + split[1].substring(0, split[1].length() - 1);
        System.out.println(fileName);

        File file = new File(fileName);

        byte[] fileContent = Files.readAllBytes(Path.of(file.getPath()));


        String saveDir = ROOT_DIRECTORY + split[0].substring(0, split[0].length() - 1);
        String createFileName = saveDir + fileName;

        File createFile = new File(createFileName);

        try (FileOutputStream fileOutputStream = new FileOutputStream(createFile)) {
            fileOutputStream.write(fileContent);
        }

        String contentType = "multipart/form-data";
        int fileLength = (int) createFile.length();
        byte[] fileData = readFileData(createFile, fileLength);

        out.println("HTTP/1.1 200 OK");
        out.println("Date: " + new Date());
        out.println("Content-Type: " + contentType);
        out.println("Content-Length: " + fileLength);
        out.println("Server: gunicorn/19.9.0");
        out.println("Access-Control-Allow-Origin: *");
        out.println("Access-Control-Allow-Credentials: true");
        out.println();

        out.flush();

        dataOut.write(fileData, 0, fileLength);
        dataOut.flush();

    }

    private static void httpDeleteHandler(String urlParameter) throws IOException {

        File file = null;

        try {
            file = new File(ROOT_DIRECTORY, urlParameter);
            System.out.println(file.getPath());
            if (file.exists()) {
                if (file.isDirectory()) {
                    File[] files = file.listFiles();

                    for (int i = 0; i < files.length; i++) {
                        if (files[i].delete()) {
                            System.out.println(files[i].getName() + " 삭제 성공");
                        } else {
                            System.out.println(files[i].getName() + " 삭제 실패");
                        }
                    }
                }

                if (file.delete()) {
                    System.out.println("파일 삭제 성공");
                    noContent();
                } else {
                    System.out.println("파일 삭제 실패");
                    foriddenError();
                }
            } else {
                System.out.println("파일이 존재 하지 않음");
                noContent();
            }

        } catch (FileNotFoundException e) {
            noContent();
        }
    }


    public static void noContent() throws IOException {
        File file = new File(NO_CONTENT);
        int fileLength = (int) file.length();
        String contentType = "application/json";
        byte[] fileData = readFileData(file, fileLength);

        out.println("HTTP/1.1 203 No Content");
        out.println("Date: " + new Date());
        out.println("Content-Type: " + contentType);
        out.println("Content-Length: " + fileLength);
        out.println("Server: gunicorn/19.9.0");
        out.println("Access-Control-Allow-Origin: *");
        out.println("Access-Control-Allow-Credentials: true");
        out.println();

        out.flush();

        dataOut.write(fileData, 0, fileLength);
        dataOut.flush();
    }
}
