package com.mbdr;

import java.io.*;
import java.util.*;

import org.tweetyproject.logics.pl.parser.PlParser;
import org.tweetyproject.commons.ParserException;
import org.tweetyproject.logics.pl.syntax.Implication;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;

import com.mbdr.formulabased.BaseRank;
import com.mbdr.utils.parsing.KnowledgeBaseReader;
import com.mbdr.utils.parsing.KnowledgeBase;
import com.mbdr.utils.parsing.Parser;

import org.tweetyproject.logics.pl.semantics.NicePossibleWorld;

public class App {

    public final static String KNOWLEDGE_BASE_DIR = "knowledge_bases/";

    public static void main(String[] args) {
        // TODO: Need to investigate normalising knowledge bases as twiddle statements

        String fileName = "penguins.txt";

        try {
            KnowledgeBaseReader reader = new KnowledgeBaseReader(KNOWLEDGE_BASE_DIR);
            ArrayList<String> rawFormulas = reader.readFormulasFromFile(fileName);

            System.out.println("----------------------------");
            System.out.println("KB file lines:");
            System.out.println("----------------------------");
            for (String rawFormula : rawFormulas) {
                System.out.println(rawFormula);
            }

            KnowledgeBase knowledgeBase = Parser.parseFormulas(rawFormulas);

            System.out.println("----------------------------");
            System.out.println("KB_C:\t" + knowledgeBase.getPropositionalKnowledge());
            System.out.println("KB_D:\t" + knowledgeBase.getDefeasibleKnowledge());
            System.out.println("----------------------------");

            ArrayList<PlBeliefSet> ranked_KB = BaseRank.BaseRankDirectImplementation(knowledgeBase);

            System.out.println("BaseRank of KB: ");
            System.out.println("----------------------------");
            Object[] ranked_KB_Arr = ranked_KB.toArray();
            System.out.println("∞" + " :\t" + ranked_KB_Arr[ranked_KB_Arr.length - 1]);
            for (int rank_Index = ranked_KB_Arr.length - 2; rank_Index >= 0; rank_Index--) {
                System.out.println(rank_Index + " :\t" + ranked_KB_Arr[rank_Index]);
            }
            System.out.println("----------------------------");
            System.out.println("Testing query:\t" + "p |~ f");

            PlParser parser = new PlParser();
            Implication query = (Implication) parser
                    .parseFormula(Parser.materialiseDefeasibleImplication("p |~ f"));

            System.out.println("Materialised query:\t" + query.toString());
            System.out.println(
                    "Answer to query:\t" + com.mbdr.formulabased.RationalClosure
                            .RationalClosureDirectImplementation(knowledgeBase, query));
            System.out.println("----------------------------");
            System.out.println("Rational Closure Ranked Model:");

            ArrayList<Set<NicePossibleWorld>> RC_Minimal_Model = com.mbdr.modelbased.RationalClosure
                    .ConstructRankedModel(knowledgeBase);

            // Print out formatted Rational Closure Ranked Model
            System.out.println("∞" + " :\t" + RC_Minimal_Model.get(RC_Minimal_Model.size() - 1));
            for (int rank_Index = RC_Minimal_Model.size() - 2; rank_Index >= 0; rank_Index--) {
                System.out.println(rank_Index + " :\t" + RC_Minimal_Model.get(rank_Index));
            }
        } catch (FileNotFoundException e) {
            System.out.println("Could not find knowledge base file!");
            e.printStackTrace();
        } catch (ParserException e) {
            System.out.println("Invalid formula in knowledge base!");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IO issue during formula parsing!");
            e.printStackTrace();
        }
    }
}
