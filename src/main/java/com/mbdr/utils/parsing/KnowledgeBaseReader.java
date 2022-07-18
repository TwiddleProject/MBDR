package com.mbdr.utils.parsing;

import java.util.ArrayList;
import java.util.Scanner;

//TODO: refactor to no longer require fixed path to allow for reading from different directories - e.g. KB files and query files may be in different directories
public class KnowledgeBaseReader extends FileReader{

    public KnowledgeBaseReader(String path) {
        super(path);
    }

    public static ArrayList<String> readFormulasFromString(String data) {
        ArrayList<String> formulas = new ArrayList<String>();
        Scanner reader = new Scanner(data);
        while (reader.hasNext()) {
            formulas.add(reader.nextLine());
        }
        reader.close();
        return formulas;
    }

}
