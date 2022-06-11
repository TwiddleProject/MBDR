package com.mbdr;

import java.io.*;
import java.util.*;

import org.tweetyproject.logics.pl.parser.PlParser;

import org.tweetyproject.logics.pl.syntax.Implication;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;

import org.tweetyproject.logics.pl.semantics.NicePossibleWorld;

public class App {
    public static void main(String[] args) {
        // TODO: Need to investigate normalising knowledge bases as twiddle statements

        PlParser parser = new PlParser();
        PlBeliefSet KB_C = new PlBeliefSet();
        PlBeliefSet KB_D = new PlBeliefSet();
        String file_name = "penguins.txt";

        try {
            File file = new File("src/main/resources/" + file_name);
            Scanner reader = new Scanner(file);
            System.out.println("----------------------------");
            System.out.println("KB file lines:");
            System.out.println("----------------------------");
            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                System.out.println(line);

                if (line.contains("|~")) {
                    line = Utils.materialiseDefeasibleImplication(line);
                    KB_D.add((PlFormula) parser.parseFormula(line));
                } else {
                    KB_C.add((PlFormula) parser.parseFormula(line));
                }
            }
            System.out.println("----------------------------");
            System.out.println("KB_C:\t" + KB_C);
            System.out.println("KB_D:\t" + KB_D);
            System.out.println("----------------------------");
            ArrayList<PlBeliefSet> ranked_KB = BaseRank.BaseRankDirectImplementation(KB_C, KB_D);
            System.out.println("BaseRank of KB: ");
            System.out.println("----------------------------");
            Object[] ranked_KB_Arr = ranked_KB.toArray();
            System.out.println("∞" + " :\t" + ranked_KB_Arr[ranked_KB_Arr.length - 1]);
            for (int rank_Index = ranked_KB_Arr.length - 2; rank_Index >= 0; rank_Index--) {
                System.out.println(rank_Index + " :\t" + ranked_KB_Arr[rank_Index]);
            }
            System.out.println("----------------------------");
            System.out.println("Testing query:\t" + "p |~ f");
            Implication query = (Implication) parser
                    .parseFormula(Utils.materialiseDefeasibleImplication("p |~ f"));
            System.out.println("Materialised query:\t" + query.toString());
            System.out.println(
                    "Answer to query:\t" + RationalClosure.RationalClosureDirectImplementation(KB_C, KB_D, query));
            System.out.println("----------------------------");
            System.out.println("Rational Closure Ranked Model:");
            ArrayList<Set<NicePossibleWorld>> RC_Mininal_Model = RationalClosure.ConstructRankedModel(KB_C, KB_D);

            // Print out formatted Rational Closure Ranked Model
            System.out.println("∞" + " :\t" + RC_Mininal_Model.get(RC_Mininal_Model.size() - 1));
            for (int rank_Index = RC_Mininal_Model.size() - 2; rank_Index >= 0; rank_Index--) {
                System.out.println(rank_Index + " :\t" + RC_Mininal_Model.get(rank_Index));
            }

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
