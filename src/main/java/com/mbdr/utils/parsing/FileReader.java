package com.mbdr.utils.parsing;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Class to provide all file reading utilities needed for reading in knowledge bases represented in "formula per line
 * format"
 */
public class FileReader {

    /**
     * Directory containing files to be read
     */
    private String directory;

    /**
     * Constructor - must specify the directory in which all the files are located
     * @param directory
     */
    public FileReader(String directory){
        this.directory = directory;
    }

    /**
     * Get the current directory
     * @return
     */
    public String getDirectory(){
        return this.directory;
    }

    /**
     * Read in the specified file and get an ArrayList containing all the lines separately as strings
     * @param fileName
     * @return
     * @throws FileNotFoundException
     */
    public ArrayList<String> readFileLines(String fileName) throws FileNotFoundException {
        ArrayList<String> lines = new ArrayList<String>();
        Scanner reader = new Scanner(new File(this.directory + fileName));
        while (reader.hasNext()) {
            lines.add(reader.nextLine());
        }
        reader.close();
        return lines;
    }

    /**
     * Get the names of all the files located in the previously set directory
     * @return ArrayList of file names
     */
    public ArrayList<String> getFileNames(){
        ArrayList<String> files = new ArrayList<String>();
        File dir = new File(getDirectory());
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                if (child.isFile()){
                    files.add(child.getName());
                }
            }
        } 
        return files;
    }

    /**
     * Get the names of all the directories located in the previously set directory
     * @return ArrayList of directory names
     */
    public ArrayList<String> getDirectoryNames(){
        ArrayList<String> dirs = new ArrayList<String>();
        File dir = new File(getDirectory());
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                if (child.isDirectory()){
                    dirs.add(child.getName());
                }
            }
        } 
        return dirs;
    }
}
