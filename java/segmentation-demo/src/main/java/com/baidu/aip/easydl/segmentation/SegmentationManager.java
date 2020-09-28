package com.baidu.aip.easydl.segmentation;

import java.awt.image.BufferedImage;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class SegmentationManager {

    private final long logID;
    private final int resultsLength;

    private final int imgWidth;
    private final int imgHeight;

    private SegmentResult[] results = null;

    public SegmentationManager(String responseString, int inputImgWidth, int inputImgHeight) throws JSONException {
        JSONObject segRespJSONObj = new JSONObject(responseString);

        logID = segRespJSONObj.getLong("log_id");

        JSONArray resultsJSONArr = segRespJSONObj.getJSONArray("results");
        resultsLength = resultsJSONArr.length();
        results = new SegmentResult[resultsLength];

        imgWidth = inputImgWidth;
        imgHeight = inputImgHeight;

        for (int i = 0; i < resultsLength; i++) {
            JSONObject resultJSONObj = resultsJSONArr.getJSONObject(i);

            JSONObject locJSONObj = resultJSONObj.getJSONObject("location");
            int left = locJSONObj.getInt("left");
            int top = locJSONObj.getInt("top");
            int width = locJSONObj.getInt("width");
            int height = locJSONObj.getInt("height");

            String mask = resultJSONObj.getString("mask");
            String name = resultJSONObj.getString("name");
            double score = resultJSONObj.getDouble("score");

            results[i] = new SegmentResult(left, top, width, height, mask, name, score, imgWidth, imgHeight);
        }
    }

    public long getLogID() {
        return logID;
    }

    public int getResultsLength() {
        return resultsLength;
    }

    public SegmentResult getSegmentResult(int i) {
        return results[i];
    }

    public void applySegmentResultMask(BufferedImage bufImg, int i, double alpha) {
        SegmentResult sr = getSegmentResult(i);
        sr.applyMask(bufImg, alpha);
    }

    public void applyResultMasks(BufferedImage bufImg, double alpha) {
        for (SegmentResult sr : results)
            sr.applyMask(bufImg, alpha);
    }

    public String toString() {
        return "SegmentationManager(" + "log_id=" + this.logID + ", " + "results=SegmentResult[" + this.resultsLength
                + "])";
    }

}
