package com.mbdr.utils.parsing;

import java.io.IOException;
import java.util.ArrayList;

import org.tweetyproject.commons.ParserException;
import org.tweetyproject.logics.pl.parser.PlParser;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;

public class Parser {

    public final static String TWIDDLE = "|~";

    public static KnowledgeBase parseFormulas(ArrayList<String> formulas) throws ParserException, IOException{
        KnowledgeBase knowledgeBase = new KnowledgeBase();
        PlParser parser = new PlParser();
        for(String rawFormula : formulas){
            // Defeasible implication
            if(rawFormula.contains(TWIDDLE)){
                String materialised = materialiseDefeasibleImplication(rawFormula);
                PlFormula formula = parser.parseFormula(materialised);
                knowledgeBase.addDefeasibleFormula(formula);
            }
            // Propositional formula
            else{
                PlFormula formula = parser.parseFormula(rawFormula);
                knowledgeBase.addPropositionalFormula(formula);
            }
        }
        return knowledgeBase;
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
     * @param DI
     * @return
     */
    public static String materialiseDefeasibleImplication(String DI) {
        return DI.replace(TWIDDLE, "=>");
    }

    // /**
    //  * Forms the union of two knowledge bases
    //  * 
    //  * @param A
    //  * @param B
    //  * @return
    //  */
    // public static PlBeliefSet Union(PlBeliefSet A, PlBeliefSet B) {
    //     PlBeliefSet temp = new PlBeliefSet();
    //     temp.addAll(A);
    //     temp.addAll(B);
    //     return temp;
    // }

}
