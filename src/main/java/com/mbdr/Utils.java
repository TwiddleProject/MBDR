package com.mbdr;

import org.tweetyproject.logics.pl.syntax.PlBeliefSet;

public class Utils {

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
    static String materialiseDefeasibleImplication(String DI) {
        return DI.replace("|~", "=>");
    }

    /**
     * Forms the union of two knowledge bases
     * 
     * @param A
     * @param B
     * @return
     */
    static PlBeliefSet Union(PlBeliefSet A, PlBeliefSet B) {
        PlBeliefSet temp = new PlBeliefSet();
        temp.addAll(A);
        temp.addAll(B);
        return temp;
    }

}
