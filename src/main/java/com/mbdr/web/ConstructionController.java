package com.mbdr.web;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.mbdr.common.structures.DefeasibleFormulaCollection;
import com.mbdr.common.structures.DefeasibleKnowledgeBase;
import com.mbdr.modelbased.construction.ModelRank;
import com.mbdr.modelbased.structures.RankedInterpretation;
import com.mbdr.utils.parsing.Parsing;

import io.javalin.http.Context;

public class ConstructionController {

    public static void getRankedRCModel(Context ctx){
        System.out.println("Body:" + ctx.body());
        String data = "";

        try {
            JSONObject jsonObject = new JSONObject(ctx.body());
            System.out.println("jsonObject:\t" + jsonObject);
            data = jsonObject.getString("data");
            System.out.println("data:\t" + data);

        } catch (JSONException err) {
            err.printStackTrace();
        }

        RankedInterpretation rankedModel = new RankedInterpretation();

        try {
            ArrayList<String> rawFormulas = Parsing.readFormulasFromString(data);

            for (String raw : rawFormulas) {
                System.out.println(raw);
            }

            DefeasibleFormulaCollection knowledge = Parsing.parseFormulas(rawFormulas);

            System.out.println("----------------------------");
            System.out.println("KB_C:\t" + knowledge.getPropositionalKnowledge());
            System.out.println("KB_D:\t" + knowledge.getDefeasibleKnowledge());
            System.out.println("----------------------------");

            rankedModel = new ModelRank().construct(new DefeasibleKnowledgeBase(knowledge));

        } catch (Exception e) {
            e.printStackTrace();
        }

        String result = "";

        result += rankedModel.toString();
        
        ctx.result(result);
    }
}
