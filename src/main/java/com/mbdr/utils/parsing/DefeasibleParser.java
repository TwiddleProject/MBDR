package com.mbdr.utils.parsing;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.tweetyproject.commons.ParserException;

import com.mbdr.common.structures.DefeasibleFormulaCollection;

public interface DefeasibleParser<T extends DefeasibleFormulaCollection> {
    
    T parse(String filePath) throws FileNotFoundException, ParserException, IOException ;

}
