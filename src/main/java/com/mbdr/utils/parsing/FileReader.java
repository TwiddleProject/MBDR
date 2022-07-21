package com.mbdr.utils.parsing;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class FileReader {

    private String directory;

    public FileReader(String directory){
        this.directory = directory;
    }

    public String getDirectory(){
        return this.directory;
    }
    
    public ArrayList<String> readFileLines(String fileName) throws FileNotFoundException {
        ArrayList<String> lines = new ArrayList<String>();
        Scanner reader = new Scanner(new File(this.directory + fileName));
        while (reader.hasNext()) {
            lines.add(reader.nextLine());
        }
        reader.close();
        return lines;
    }

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
