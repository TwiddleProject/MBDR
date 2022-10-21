package com.mbdr.utils.parsing;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.tweetyproject.commons.ParserException;
import org.tweetyproject.logics.pl.parser.PlParser;
import org.tweetyproject.logics.pl.syntax.Contradiction;
import org.tweetyproject.logics.pl.syntax.Implication;
import org.tweetyproject.logics.pl.syntax.Negation;
import org.tweetyproject.logics.pl.syntax.PlFormula;

import com.mbdr.common.structures.DefeasibleFormulaCollection;

/**
 * Class to provide all parsing utilities needed for reading in knowledge bases represented in "formula per line
 * format"
 */
public class Parsing {

    //Expected representation for twiddle connective
    public final static String TWIDDLE = "|~";

    /**
     * Determines whether string formula is defeasible
     * @param formula
     * @return boolean indicating whether formula is defeasible or not
     */
    public static boolean isDefeasible(String formula){
        return formula.contains(TWIDDLE);
    }

    /**
     * Parse a collection of String formulas
     * @param formulas - collection containing all the formulas as Strings
     * @return DefeasibleFormulaCollection containing all the parsed formulas
     * @throws ParserException
     * @throws IOException
     */
    public static DefeasibleFormulaCollection parseFormulas(List<String> formulas) throws ParserException, IOException{
        DefeasibleFormulaCollection collection = new DefeasibleFormulaCollection();
        for(String rawFormula : formulas){
            // Defeasible implication
            if(isDefeasible(rawFormula)){
                PlFormula formula = parseDefeasibleFormula(rawFormula);
                collection.addDefeasibleFormula(formula);
            }
            // Propositional formula
            else{
                PlFormula formula = parsePropositionalFormula(rawFormula);
                collection.addPropositionalFormula(formula);
            }
        }
        return collection;
    }

    /**
     * Parse a single propositional formula
     * @param propositionalFormula
     * @return PlFormula
     * @throws ParserException
     * @throws IOException
     */
    public static PlFormula parsePropositionalFormula(String propositionalFormula) throws ParserException, IOException{
        PlParser parser = new PlParser();
        return parser.parseFormula(propositionalFormula);
    }

    /**
     * Parse a single defeasible formula (containing a twiddle connective)
     * @param defeasibleFormula
     * @return PlFormula - the parsed formula is a materialised defeasible implication
     * @throws ParserException
     * @throws IOException
     */
    public static PlFormula parseDefeasibleFormula(String defeasibleFormula) throws ParserException, IOException{
        PlParser parser = new PlParser();
        if(!isDefeasible(defeasibleFormula)){
            throw new ParserException("Expected a defeasible formula");
        }
        return parser.parseFormula(materialiseDefeasibleImplication(defeasibleFormula));
    }

    /**
     * Separates string of formulas into individual formulas.
     * Currently used for web interface.
     * @param data The string to parse
     * @return Array of string formulas
     */
    public static ArrayList<String> readFormulasFromString(String data) {
        ArrayList<String> formulas = new ArrayList<String>();
        Scanner reader = new Scanner(data);
        while (reader.hasNext()) {
            formulas.add(reader.nextLine());
        }
        reader.close();
        return formulas;
    }

    /*
     * The BNF for a propositional belief set is given by (starting symbol is
     * FORMULASET)
     * FORMULASET ::== FORMULA ( "\n" FORMULA )*
     * FORMULA ::== PROPOSITION | "(" FORMULA ")" | FORMULA ">>" FORMULA | FORMULA
     * "||" FORMULA | FORMULA "=>" FORMULA | FORMULA "<=>" FORMULA | FORMULA "^^"
     * FORMULA | "!" FORMULA | "+" | "-"
     */

    /**
     * Materialises given defeasible implication (changes twiddle to material
     * implication)
     * 
     * @param DI The defeasible implication
     * @return The materialized implication
     */
    public static String materialiseDefeasibleImplication(String DI) {
        return DI.replace(TWIDDLE, "=>");
    }

    /**
     * Normalises a purely classical propositional as a defeasible implication
     * @param formula - classical propositional formula
     * @return defeasible implication
     */
    public static Implication normalizePropositionalFormula(PlFormula formula){
        return new Implication(new Negation(formula), new Contradiction());
    }

}
