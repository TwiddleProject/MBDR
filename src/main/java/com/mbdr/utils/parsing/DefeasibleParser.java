package com.mbdr.utils.parsing;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.tweetyproject.commons.ParserException;

import com.mbdr.common.structures.DefeasibleFormulaCollection;

/**
 * This is the Generic class interface to be shared by all parsers that read in knowledge base text files and return
 * DefeasibleFormulaCollections, which serve as the basis for DefeasibleKnowledgeBases.
 * @param <T>
 */
public interface DefeasibleParser<T extends DefeasibleFormulaCollection> {

    /**
     * Parse method interface
     * @param filePath - path to the knowledge base file
     * @return extension of DefeasibleFormulaCollection
     * @throws FileNotFoundException
     * @throws ParserException
     * @throws IOException
     */
    T parse(String filePath) throws FileNotFoundException, ParserException, IOException ;

}
