// The main task of this tool is read and process a given .bib file (which has one or more articles) and create 3
//different files with the correct reference formats for IEEE, ACM and NJ.

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class BibCreator {

    public static void main(String[] args) {


        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        System.out.println("===================Welcome to BibCreator===================\n");
        Scanner scanner = new Scanner(System.in);

        String dirPath = System.getProperty("user.dir");
//        mini switch
//        optional string input
//                dirPath = input

//        File dir = new File(dirPath);
//        File[] fileList = dir.listFiles();
//        File[] fileList1 = new File(dirPath).listFiles();


        List<String> fileList = new ArrayList<>();
        getBibFiles(dirPath, fileList);

        System.out.println(fileList);

        String[][] fileArr = new String[fileList.size()][3];

        splitBibFileName(fileList, fileArr);
        for (String[] strings : fileArr) {
            for (String string : strings) {
                System.out.print(string + " ");
            }
            System.out.println();
        }



    }

    private static void getBibFiles(String dirPath, List<String> bibFileList) {
        File[] allFiles = new File(dirPath).listFiles();

        if (allFiles != null) {
            for (File file : allFiles) {
                if (file.isFile() && endsWithIgnoreCase(file.getName(), ".bib")) {
                    bibFileList.add(file.getName());
                }
            }
        }
    }
    public static boolean endsWithIgnoreCase(String str, String suffix) {
        return endsWith(str, suffix, true);
    }
    private static boolean endsWith(String str, String suffix, boolean ignoreCase) {
        if (str == null || suffix == null) {
            return (str == null && suffix == null);
        }
        if (suffix.length() > str.length()) {
            return false;
        }
        int strOffset = str.length() - suffix.length();
        return str.regionMatches(ignoreCase, strOffset, suffix, 0, suffix.length());
    }
    public static boolean startsWithIgnoreCase(String str, String prefix) {
        return str.regionMatches(true, 0, prefix, 0, prefix.length());
    }
    public static void splitBibFileName(List<String> fileList, String[][] fileArr){

        String start = "Latex";
        for (int i = 0; i < fileList.size(); i++) {

            if(startsWithIgnoreCase(fileList.get(i), start)) {

                fileArr[i][0] = fileList.get(i).substring(0, 5);

                //String remainder = fileList.get(i).substring(5);
                String[] parts = fileList.get(i).substring(5).split("\\.(?=[^.]*$)");

                fileArr[i][1] = parts[0];
                fileArr[i][2] = "."+parts[1];
            }
            else{
                fileArr[i][0] = "";
                String[] parts = fileList.get(i).split("\\.(?=[^.]*$)");

                fileArr[i][1] = parts[0];
                fileArr[i][2] = "."+parts[1];
            }
        }
    }

}
