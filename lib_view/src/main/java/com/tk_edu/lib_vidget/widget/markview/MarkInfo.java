package com.tk_edu.lib_vidget.widget.markview;


public class MarkInfo {

    private float startX, startY, endX, endY;
    private String text;

    private float RecstartX, RecstartY, RecWidth, RecHight;


    public MarkInfo(float startX, float startY, float endX, float endY, String text) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.text = text;
    }

    public void setRecT(float RecstartX, float RecstartY, float RecWidth, float RecHight) {
        this.RecstartX = RecstartX;
        this.RecstartY = RecstartY;
        this.RecWidth = RecWidth;
        this.RecHight = RecHight;
    }


    public float getRecstartX() {
        return RecstartX;
    }

    public void setRecstartX(float recstartX) {
        RecstartX = recstartX;
    }

    public float getRecstartY() {
        return RecstartY;
    }

    public void setRecstartY(float recstartY) {
        RecstartY = recstartY;
    }

    public float getRecWidth() {
        return RecWidth;
    }

    public void setRecWidth(float recWidth) {
        RecWidth = recWidth;
    }

    public float getRecHight() {
        return RecHight;
    }

    public void setRecHight(float recHight) {
        RecHight = recHight;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public float getStartX() {
        return startX;
    }

    public void setStartX(float startX) {
        this.startX = startX;
    }

    public float getStartY() {
        return startY;
    }

    public void setStartY(float startY) {
        this.startY = startY;
    }

    public float getEndX() {
        return endX;
    }

    public void setEndX(float endX) {
        this.endX = endX;
    }

    public float getEndY() {
        return endY;
    }

    public void setEndY(float endY) {
        this.endY = endY;
    }


}

