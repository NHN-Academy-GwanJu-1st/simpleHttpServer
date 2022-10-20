package com.nhnacademy.test2;

import java.io.File;
import java.util.Properties;

public class Test1 {
    public static void main(String[] args) {

        String urlParameter = "D:\\NHNAcademy 강의자료\\http-https\\simple-httpd\\simpleHttpd\\src\\main\\resources/empty1/empty1-test";

        File dir = new File(urlParameter);
        String[] filenames = dir.list();
        for (int i = 0; i < filenames.length; i++) {
//            sb.append("<li><a href=" + filenames[i] + ">" + filenames[i] + "</li>");
            System.out.println("file: " + filenames[i]);
        }

        String test = "empty1";
        Properties properties = System.getProperties();
        Object o = properties.get("user.dir");
        System.out.println(o);
    }
}
