package com.mbdr.utils.parsing;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.tweetyproject.commons.ParserException;

import com.mbdr.common.structures.DefeasibleFormulaCollection;
import com.mbdr.common.structures.DefeasibleKnowledgeBase;

public class KnowledgeBaseReader extends FileReader implements DefeasibleParser<DefeasibleKnowledgeBase>{

    public KnowledgeBaseReader(String path) {
        super(path);
    }

    @Override
    public DefeasibleKnowledgeBase parse(String filePath) throws FileNotFoundException, ParserException, IOException {
        ArrayList<String> formulas = this.readFileLines(filePath);
        DefeasibleFormulaCollection parsedFormulas = Parsing.parseFormulas(formulas);
        return new DefeasibleKnowledgeBase(parsedFormulas);
    }

}
