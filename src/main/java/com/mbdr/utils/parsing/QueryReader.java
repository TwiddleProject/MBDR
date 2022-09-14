package com.mbdr.utils.parsing;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.tweetyproject.commons.ParserException;

import com.mbdr.common.structures.DefeasibleFormulaCollection;
import com.mbdr.common.structures.DefeasibleQuerySet;

/**
 * Class used for reading in query set files in which each line of the text file is expected to contain a query in the
 * form of a single formula
 */
public class QueryReader extends FileReader implements DefeasibleParser<DefeasibleQuerySet>{

    /**
     * Constructor - requires path to where query set files are located
     * @param path
     */
    public QueryReader(String path) {
        super(path);
    }

    /**
     * Gets the query set file name for a given knowledge base file. It is assumed that for a given knowledge base text
     * file, say "example.txt", its corresponding query set has the name: "example_queries.txt"
     * @param knowledgeBaseFileName
     * @return
     */
    public String getQueryFileName(String knowledgeBaseFileName){
        int dotIndex = knowledgeBaseFileName.lastIndexOf('.');
        String suffix = knowledgeBaseFileName.substring(dotIndex);
        return knowledgeBaseFileName.substring(0, dotIndex) + "_queries" + suffix;
    }

    /**
     * Parse the given query set text file
     * @param filePath - path to the knowledge base file
     * @return DefeasibleQuerySet
     * @throws FileNotFoundException
     * @throws ParserException
     * @throws IOException
     */
    @Override
    public DefeasibleQuerySet parse(String filePath) throws FileNotFoundException, ParserException, IOException {
        ArrayList<String> formulas = this.readFileLines(filePath);
        DefeasibleFormulaCollection parsedFormulas = Parsing.parseFormulas(formulas);
        return new DefeasibleQuerySet(parsedFormulas);
    }

}
