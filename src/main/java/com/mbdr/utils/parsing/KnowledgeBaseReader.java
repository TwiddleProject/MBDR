package com.mbdr.utils.parsing;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class KnowledgeBaseReader {

    private String path;

    public KnowledgeBaseReader(String path){
        this.path = path;
    }

    public ArrayList<String> readFormulasFromFile(String fileName) throws FileNotFoundException{
        ArrayList<String> formulas = new ArrayList<String>();
        Scanner reader = new Scanner(new File(this.path + fileName));
        while(reader.hasNext()){
            formulas.add(reader.nextLine());
        }
        reader.close();
        return formulas;
    }

    public static ArrayList<String> readFormulasFromString(String data){
        ArrayList<String> formulas = new ArrayList<String>();
        Scanner reader = new Scanner(data);
        while(reader.hasNext()){
            formulas.add(reader.nextLine());
        }
        reader.close();
        return formulas;
    }

}
