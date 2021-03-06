package com.barbar.npkproject;

import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataAnalyze {

    public String getResult (List<JSONObject> list) {
        // calibration
        if (list.size() <= 2) {
            return "NaN";
        }

        List<Double> notes = new ArrayList<>();
        List<Double> priorities = new ArrayList<>();
        List<Double> times = new ArrayList<>();

        try {
            for (JSONObject data : list) {
                notes.add(Double.parseDouble(data.get("value").toString()));
                priorities.add(Double.parseDouble(data.get("apr_rating").toString()));
                times.add(Double.parseDouble(data.get("time").toString()) / 86_400_000.0);
            }
        } catch (Exception e) {
            return "-0";
        }

        double[] rating = new double[notes.size()];
        //for (int i = 0;i < rating.length;i++) {
        //    rating[i] = notes.get(i);
        //}

        rating[0] = (notes.get(0) * 2 + notes.get(1)) / 3.0;
        for (int i = 1;i < rating.length - 1;i++) {
            rating[i] = (notes.get(i - 1) + notes.get(i) * 2 + notes.get(i + 1)) / 4.0;
        }
        rating[rating.length - 1] = (notes.get(notes.size() - 1) * 2 + notes.get(notes.size() - 2)) / 3.0;

        double currentTime = new Date().getTime() / 86_400_000.0;
        double[] influence = new double[notes.size()];

        double sum_of_exponent = 0;

        for (int i = 0;i < influence.length;i++) {
            sum_of_exponent += Math.exp(rating[i]);
        }

        for (int i = 0;i < influence.length;i++) {
            // TODO Не от текущей даты, а от последней
            influence[i] = sigmoid(times.get(i) - currentTime) * (Math.exp(rating[i]) / sum_of_exponent);
        }

        double sum_of_influences = 0;
        for (int i = 0;i < influence.length;i++) {
            sum_of_influences += influence[i];
        }

        double answer = 0;

        for (int i = 0;i < rating.length;i++) {
            answer += rating[i] * (influence[i] / sum_of_influences);
        }

        String result = String.valueOf(answer);
        if (result.length() > 4 && result.contains(".")) {
            result = result.substring(0, 5);
        }
        return result;
    }

    private static double sigmoid (double x) {
        return 1 / (1 + Math.pow(Math.E, -(0.2 * x + 5) ) );
    }
}
