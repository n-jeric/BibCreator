// The main task of this tool is read and process a given .bib file (which has one or more articles) and create 3
//different files with the correct reference formats for IEEE, ACM and NJ.

import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;


public class BibCreator {

    public static void main(String[] args) {

        String dirPath;
        List<String> fileList;
        String[][] fileArr;
        File[][] outputFiles;

        System.out.println("===================Welcome to BibCreator===================\n");
        Scanner scanner = new Scanner(System.in);
        Scanner sc = null;
        PrintWriter pw = null;

        dirPath =  setBibDirectory (scanner);

        fileList = new ArrayList<>();
        getBibFiles(dirPath, fileList);

        System.out.println(fileList);

        fileArr = new String[fileList.size()][3];
        splitBibFileName(fileList, fileArr);

        for (String[] strings : fileArr)
        {
            for (String string : strings)
            {
                System.out.print(string);
            }
            System.out.println();
        }

        //open all .bib files in directory
        for (String[] strings : fileArr)
        {
            try
            {
                sc = new Scanner(new FileInputStream(dirPath +"/"+ strings[0] + strings[1] + strings[2]));
                sc.close();
            }
            catch (FileNotFoundException e) //if file does not exist
            {
                System.out.println("Could not open input file " + strings[0] + strings[1] + strings[2] + " for reading.\n\nPlease check if file exists! Program will terminate after closing any opened files.");
                sc.close(); //close any opened files
                System.exit(0);
            }
        }

        outputFiles = new File[fileArr.length][3];
        String outputPath = setOutputDirectory(scanner, dirPath);
        createJsonFiles(fileArr, pw, outputPath, outputFiles);
        try
        {
            Thread.sleep(3000);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        deleteFiles (outputPath, outputFiles);

        //tests output path method
//        try {
//            pw = new PrintWriter(new FileOutputStream(outputPath+ "jimmy.txt"));
//            pw.close();
//        } catch (FileNotFoundException e) {
//            pw.close();
//            throw new RuntimeException(e);
//
//        }
        scanner.close();
    }
    public static String setBibDirectory (Scanner scanner)
    {
        String dirPath;
        System.out.println("Working Directory for .bib files = " + System.getProperty("user.dir"));

        System.out.print("Would you like to enter a different directory? (y/n) ");
        char ans = scanner.next().toLowerCase().charAt(0);
        scanner.nextLine();

        if(ans == 'y'){
            System.out.print("Enter directory path: ");
            dirPath = scanner.nextLine();
        }
        else {
            dirPath = System.getProperty("user.dir");
        }
        return dirPath;
    }
    public static void getBibFiles(String dirPath, List<String> bibFileList)
    {
        File[] allFiles = new File(dirPath).listFiles();

        if (allFiles != null) {
            for (File file : allFiles) {
                if (file.isFile() && endsWithIgnoreCase(file.getName(), ".bib")) {
                    bibFileList.add(file.getName());
                }
            }
        }
    }
    public static boolean endsWithIgnoreCase(String str, String suffix)
    {
        return endsWith(str, suffix, true);
    }
    private static boolean endsWith(String str, String suffix, boolean ignoreCase)
    {
        if (str == null || suffix == null) {
            return (str == null && suffix == null);
        }
        if (suffix.length() > str.length()) {
            return false;
        }
        int strOffset = str.length() - suffix.length();
        return str.regionMatches(ignoreCase, strOffset, suffix, 0, suffix.length());
    }
    public static boolean startsWithIgnoreCase(String str, String prefix)
    {
        return str.regionMatches(true, 0, prefix, 0, prefix.length());
    }
    public static void splitBibFileName(List<String> fileList, String[][] fileArr)
    {

        String start = "Latex";
        for (int i = 0; i < fileList.size(); i++) {

            if(startsWithIgnoreCase(fileList.get(i), start)) {

                fileArr[i][0] = fileList.get(i).substring(0, 5);

                //String remainder = fileList.get(i).substring(5);
                String[] parts = fileList.get(i).substring(5).split("\\.(?=[^.]*$)");//regex positive lookahead - to ensure it only splits on the last occurrence

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
    public static String setOutputDirectory (Scanner scanner, String dirPath)
    {
        String outputPath;
        System.out.println("Default Directory to save .json files = " + System.getProperty("user.dir"));

        if(dirPath.equals(System.getProperty("user.dir"))){
            System.out.print("Would you like to enter a different directory? (y/n) ");
            char ans = scanner.next().toLowerCase().charAt(0);
            scanner.nextLine();

            if(ans == 'y'){
                System.out.print("Enter directory path: ");
                outputPath = scanner.nextLine();
            }
            else {
                outputPath = System.getProperty("user.dir");
            }
        }
        else
        {
            System.out.print("Would you like to use the current .bib file directory at "+ dirPath + "? (y/n) ");
            char ans = scanner.next().toLowerCase().charAt(0);
            scanner.nextLine();

            if(ans == 'y'){
                outputPath = dirPath;
            }
            else {
                System.out.print("Would you like to enter a different directory? (y/n) ");
                ans = scanner.next().toLowerCase().charAt(0);
                scanner.nextLine();

                if(ans == 'y'){
                    System.out.print("Enter directory path: ");
                    outputPath = scanner.nextLine();
                }
                else {
                    outputPath = System.getProperty("user.dir");
                }
            }
        }
        outputPath += "\\";
        return outputPath;
    }
    public static void createJsonFiles (String[][] fileArr,PrintWriter pw, String outputPath, File[][] outputFiles)
    {
        String fileName = "";
        for(int i = 0; i < fileArr.length; i++)
        {
            try {
                fileName = "IEEE" + fileArr[i][1] + ".json";
                outputFiles[i][0] = new File(outputPath + fileName);
                pw = new PrintWriter(new FileOutputStream(outputPath + fileName));
                pw.close();
                fileName = "ACM" + fileArr[i][1] + ".json";
                outputFiles[i][1] = new File(outputPath + fileName);
                pw = new PrintWriter(new FileOutputStream(outputPath + fileName));
                pw.close();
                fileName = "NJ" + fileArr[i][1] + ".json";
                outputFiles[i][2] = new File(outputPath + fileName);
                pw = new PrintWriter(new FileOutputStream(outputPath + fileName));
                pw.close();
            }
            catch (FileNotFoundException e)
            {
                System.out.println("Could not create " + fileName + " for " + fileArr[i][0] + fileArr[i][1] +fileArr[i][2] + ".\nClearing directory of all other created output files.");
                pw.close();
                //Deleting all opened/created files.
                for (File[] outFile : outputFiles) {
                    for (File file : outFile) {
                        file.delete();
                    }
                }
                //Exiting Program
                System.exit(0);
                //throw new RuntimeException(e);
            }
        }
    }
    public static void deleteFiles (String outputPath, File[][]outputFiles)
    {
        for (File[] outFile : outputFiles) {
            for (File file : outFile) {
                file.delete();
            }
        }
    }
}
