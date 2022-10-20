package com.mbdr.web;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;
import org.tweetyproject.commons.ParserException;

import com.mbdr.common.services.RankConstructor;
import com.mbdr.common.structures.DefeasibleFormulaCollection;
import com.mbdr.common.structures.DefeasibleKnowledgeBase;
import com.mbdr.modelbased.construction.*;
import com.mbdr.modelbased.structures.RankedInterpretation;
import com.mbdr.utils.parsing.Parsing;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
 
import io.javalin.http.Context;

public class ConstructionController {

    private static Map<String, Class<? extends RankConstructor<? extends Object>>> constructors = Map.ofEntries(
        Map.entry("modelrank", ModelRank.class),
        Map.entry("formularank", FormulaRank.class),
        Map.entry("cumulativeformularank", CumulativeFormulaRank.class),
        Map.entry("lexicographicmodelrank", LexicographicCountModelRank.class),
        Map.entry("lexicographicformularank", LexicographicCountFormulaRank.class),
        Map.entry("lexicographiccumulativeformularank", LexicographicCountCumulativeFormulaRank.class)
    );

    public static void getModel(Context ctx){
        System.out.println("GET MODEL CALLED");
        String data = "";
        try {
            JSONObject jsonObject = new JSONObject(ctx.body());
            data = jsonObject.getString("data");
        } 
        catch (JSONException err) {
            ctx.result("Could not parse JSON data!");
            return;
        }

        if (!constructors.containsKey(ctx.pathParam("algorithm"))){
            ctx.result("No such algorithm!");
            return;
        }

        Object model = null;

        try {
            ArrayList<String> rawFormulas = Parsing.readFormulasFromString(data);
            DefeasibleFormulaCollection knowledge = Parsing.parseFormulas(rawFormulas);
            RankConstructor<?> rankConstructor = (RankConstructor<?>) constructors
                .get(ctx.pathParam("algorithm"))
                .getConstructor()
                .newInstance();
            model = rankConstructor.construct(new DefeasibleKnowledgeBase(knowledge));
        } 
        catch(ParserException | IOException e){
            ctx.result("Invalid knowledge base!\nMake sure each line is a valid formula.");
            return;
        }
        catch (Exception e) {
            ctx.result("Could not construct model!");
            return;
        }
        ctx.result(model.toString());
    }
}