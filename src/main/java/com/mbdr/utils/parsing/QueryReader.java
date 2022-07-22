package com.mbdr.utils.parsing;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.tweetyproject.commons.ParserException;

import com.mbdr.common.structures.DefeasibleFormulaCollection;
import com.mbdr.common.structures.DefeasibleQuerySet;

//TODO: refactor to no longer require fixed path to allow for reading from different directories - e.g. KB files and query files may be in different directories
public class QueryReader extends FileReader implements DefeasibleParser<DefeasibleQuerySet>{

    public QueryReader(String path) {
        super(path);
    }

    public String getQueryFileName(String knowledgeBaseFileName){
        int dotIndex = knowledgeBaseFileName.lastIndexOf('.');
        String suffix = knowledgeBaseFileName.substring(dotIndex);
        return knowledgeBaseFileName.substring(0, dotIndex) + "_queries" + suffix;
    }

    @Override
    public DefeasibleQuerySet parse(String filePath) throws FileNotFoundException, ParserException, IOException {
        ArrayList<String> formulas = this.readFileLines(filePath);
        DefeasibleFormulaCollection parsedFormulas = Parsing.parseFormulas(formulas);
        return new DefeasibleQuerySet(parsedFormulas);
    }

}
