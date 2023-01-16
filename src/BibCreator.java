/*
*
* The main task of this tool is read and process a given .bib file (which has one or more articles) and create 3
* different files with the correct reference formats for IEEE, ACM and NJ.
* */

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class BibCreator {
    static int invalid = 0;
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

//        System.out.println(fileList);

        fileArr = new String[fileList.size()][3];
        splitBibFileName(fileList, fileArr);

//        for (String[] strings : fileArr)
//        {
//            for (String string : strings)
//            {
//                System.out.print(string + "*");
//            }
//            System.out.println();
//        }

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

        for (int i = 0; i < fileArr.length; i++) {
            try
            {
                sc = new Scanner(new FileInputStream(dirPath +"/"+ fileArr[i][0] + fileArr[i][1] + fileArr[i][2]));
            }
            catch (FileNotFoundException ex){
                System.out.println("Could not open input file " + fileArr[i][0] + fileArr[i][1] + fileArr[i][2] + " for reading.\n\nPlease check if file exists! Program will terminate after closing any opened files.");
                sc.close();
                System.exit(0);
            }
            try
            {
                processFilesForValidation(sc, pw, i, outputFiles, fileArr);
            }
            catch (FileNotFoundException | FileInvalidException e ){
                System.out.println(e.getMessage());
            }
        }
        System.out.println("A total of " + invalid + " files were invalid, and could not be processed. All other " + (fileArr.length-invalid) + " valid files have been created.\n");

        displayFile(scanner, outputPath);

        scanner.close();
        System.out.println("Goodbye! Hope you enjoyed creating the needed files using BibCreator.");
    }
    public static String setBibDirectory (Scanner scanner){
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
    public static void getBibFiles(String dirPath, List<String> bibFileList){
        File[] allFiles = new File(dirPath).listFiles();

        if (allFiles != null) {
            for (File file : allFiles) {
                if (file.isFile() && endsWithIgnoreCase(file.getName(), ".bib")) {
                    bibFileList.add(file.getName());
                }
            }
        }
    }
    public static boolean endsWithIgnoreCase(String str, String suffix){
        return endsWith(str, suffix, true);
    }
    private static boolean endsWith(String str, String suffix, boolean ignoreCase){
        if (str == null || suffix == null) {
            return (str == null && suffix == null);
        }
        if (suffix.length() > str.length()) {
            return false;
        }
        int strOffset = str.length() - suffix.length();
        return str.regionMatches(ignoreCase, strOffset, suffix, 0, suffix.length());
    }
    public static boolean startsWithIgnoreCase(String str, String prefix){
        return str.regionMatches(true, 0, prefix, 0, prefix.length());
    }
    public static void splitBibFileName(List<String> fileList, String[][] fileArr){

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
    public static String setOutputDirectory (Scanner scanner, String dirPath){
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
    public static void createJsonFiles (String[][] fileArr,PrintWriter pw, String outputPath, File[][] outputFiles){
        String fileName = "";
        for(int i = 0; i < fileArr.length; i++)
        {
            try {
                fileName = "IEEE" + fileArr[i][1] + ".json";
                outputFiles[i][0] = new File(outputPath + fileName);
                pw = new PrintWriter(new FileOutputStream(outputFiles[i][0]));
                pw.close();
                fileName = "ACM" + fileArr[i][1] + ".json";
                outputFiles[i][1] = new File(outputPath + fileName);
                pw = new PrintWriter(new FileOutputStream(outputFiles[i][1]));
                pw.close();
                fileName = "NJ" + fileArr[i][1] + ".json";
                outputFiles[i][2] = new File(outputPath + fileName);
                pw = new PrintWriter(new FileOutputStream(outputFiles[i][2]));
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
    public static void deleteFiles (File[][]outputFiles, int i){
        for (int j = 0; j < 3; j++) {
            outputFiles[i][j].delete();
        }
    }
    public static void deleteFiles (String outputPath, File[][]outputFiles){
        for (File[] outFile : outputFiles) {
            for (File file : outFile) {
                file.delete();
            }
        }
    }
    public static String readFile(Scanner sc){
        String str = "";
        while (sc.hasNextLine())
        {
            str += sc.nextLine();
        }
        sc.close();
        return str;
    }
    public static void processFilesForValidation (Scanner sc, PrintWriter pw, int i, File[][] outputFiles, String[][] fileArr) throws FileInvalidException, FileNotFoundException {
        String bFile = readFile(sc);
        int ibegin = bFile.indexOf("@");
        String bibFile = bFile.substring(ibegin+1);

        String [] authors = new String[1];
        String journal = "", title = "", year = "", volume = "", number = "", pages = "", doi = "", month = "";
        StringBuilder ieee = new StringBuilder();
        StringBuilder acm = new StringBuilder();
        StringBuilder nj = new StringBuilder();

        String[] result = bibFile.split("@");
//        String[] result = Arrays.stream(bFile.trim().split("@")).filter(s-> !s.equals("")).map(String::trim).toArray(String[]::new);
//        String[] result = Arrays.stream(bFile.split("@")).filter(s-> !s.equals("")).toArray(String[]::new);
        //not needed to convert from object to string as above adjusted method creates array of string to start with
//        Object[] res = Arrays.stream(bFile.toString().trim().split("@")).filter(s-> !s.equals("")).map(s -> s.trim()).toArray();
//        String[] result = Arrays.stream(res).toArray(String[]::new); //convert array of objects to arr of string

        //console printout of article split
//        for (int x=0; x<result.length; x++){
//            System.out.println(x + " " + result[x]);
//        }
//        System.out.println();

        String reg = "([a-zA-Z]*?)=\\{(.*?)\\}"; // a-z 0 or more times = any character 0 or more times in {}
        Pattern regPattern = Pattern.compile(reg);

        for (int x = 0; x < result.length; x++) {
            Matcher match = regPattern.matcher(result[x]);

            while(match.find()){
                String field = match.group();
                String[] fields = field.split("=");

                String emptyReg = "\\{(\\s*)\\}"; // whitespace 0 or more times between {}
                Pattern emptyPattern = Pattern.compile(emptyReg);
                Matcher emptyMatch = emptyPattern.matcher(fields[1]);

                if(emptyMatch.find()){
                    invalid++;
                    deleteFiles(outputFiles, i);
                    throw new FileInvalidException("Error: Detected Empty Field!\n"
                            +"============================\n\n"
                            +"Problem detected with input file: " + fileArr[i][0] + fileArr[i][1] + fileArr[i][2] + "\n"
                            +"File is Invalid: Field \"" + fields[0] + "\" is Empty. Processing stopped at this point. Other empty fields may be present as well!\n");
                }

                String entry = fields[1].substring(1,fields[1].indexOf('}')); //takes the value of fields[1] from { to }
                switch (fields[0]) {
                    case "author" ->
                            authors = entry.split(" and ");
                            //authors = Arrays.stream(entry.split("and")).map(String::trim).toArray(String[]::new); //Stream features -> not right result
                    case "journal" ->
                            journal = entry;
                    case "title" ->
                            title = entry;
                    case "year" ->
                            year = entry;
                    case "volume" ->
                            volume = entry;
                    case "number" ->
                            number = entry;
                    case "pages" ->
                            pages = entry;
                    case "doi" ->
                            doi = entry;
                    case "month" ->
                            month = entry;
                }
            }
            //IEEE
            for (int k = 0; k < authors.length-1; k++) {
                ieee.append(authors[k]).append(", ");
            }
            ieee.append(authors[authors.length - 1]).append(". \"").append(title).append("\", ").append(journal).append(", vol. ").append(volume).append(", no. ").append(number).append(", p. ").append(pages).append(", ").append(month).append(" ").append(year).append(".\n\n");

            //ACM
            if(authors.length<2)
                acm.append("[").append(x+1).append("]\t").append(authors[0]).append(". ").append(year).append(". ").append(title).append(". ").append(journal).append(". ").append(volume).append(", ").append(number).append(" (").append(year).append("), ").append(pages).append(". DOI:https://doi.org/").append(doi).append(".\n\n");
            else{
                acm.append("[").append(x+1).append("]\t").append(authors[0]).append(" et al. ").append(year).append(". ").append(title).append(". ").append(journal).append(". ").append(volume).append(", ").append(number).append(" (").append(year).append("), ").append(pages).append(". DOI:https://doi.org/").append(doi).append(".\n\n");
            }

            //NJ
            for (int k = 0; k < authors.length-1; k++) {
                nj.append(authors[k]).append(" & ");
            }
            nj.append(authors[authors.length - 1]).append(". ").append(title).append(". ").append(journal).append(". ").append(volume).append(", ").append(pages).append("(").append(year).append(").\n\n");
        }//end for loop result[x]

        pw = new PrintWriter(new FileOutputStream(outputFiles[i][0]));
        pw.println(ieee);
        pw.close();

        pw = new PrintWriter(new FileOutputStream(outputFiles[i][1]));
        pw.println(acm);
        pw.close();

        pw = new PrintWriter(new FileOutputStream((outputFiles[i][2])));
        pw.println(nj);
        pw.close();
    }
    public static void readBuffer(BufferedReader br, String fileName) throws IOException
    {
        System.out.println("Here are the contents of the successfully created .JSON file: " + fileName + "\n");
        String str = br.readLine();
        while (str != null)
        {
            System.out.println(str);
            str = br.readLine();
        }
        br.close();
    }
    public static void displayFile(Scanner scanner, String outputPath){
        int counter = 1;
        BufferedReader br;
        while(counter <= 2){
            try{
                System.out.print("Please enter the name of one of the files that you need to review: ");
                String fileName = scanner.nextLine();
                br = new BufferedReader(new FileReader(outputPath + fileName));
                readBuffer(br, fileName);
                return;
            }
            catch (IOException e) //'java.io.FileNotFoundException' is a subclass of 'java.io.IOException'
            {
                if(counter == 1){
                    System.out.println("Could not open input file. File does not exist; possibly it could not be created!\n");
                    System.out.println("However, you will be allowed another chance to enter another file name.\n");
                }
                else if(counter == 2){
                    System.out.println("\nCould not open input file again! Either file does not exist, or could not be created.");
                    System.out.println("Sorry! I am unable to display your desired files! Program will exit!");
                    System.exit(0);
                }
                counter++;
            }
        }
    }


}
