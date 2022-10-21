package com.nhnacademy.test2;

import java.io.File;
import java.util.Properties;

public class Test1 {
    public static void main(String[] args) {

        String urlParameter = "D:\\NHNAcademy 강의자료\\http-https\\simple-httpd\\simpleHttpd\\src\\main\\resources/empty3";

        File file = new File(urlParameter);

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
            } else {
                System.out.println("파일 삭제 실패");
            }
        } else {
            System.out.println("파일이 존재 하지 않음");
        }
    }
}
