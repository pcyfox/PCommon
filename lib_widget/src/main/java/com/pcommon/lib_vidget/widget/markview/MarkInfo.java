package com.pcommon.lib_vidget.widget.markview;


public class MarkInfo {

    private float startX, startY, endX, endY;
    private String text;

    private float recStartX, recStartY, recWidth, recHeight;


    public MarkInfo(float startX, float startY, float endX, float endY, String text) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.text = text;
    }

    public void setRecT(float RecstartX, float RecstartY, float RecWidth, float RecHight) {
        this.recStartX = RecstartX;
        this.recStartY = RecstartY;
        this.recWidth = RecWidth;
        this.recHeight = RecHight;
    }


    public float getRecStartX() {
        return recStartX;
    }

    public void setRecStartX(float recStartX) {
        this.recStartX = recStartX;
    }

    public float getRecStartY() {
        return recStartY;
    }

    public void setRecStartY(float recStartY) {
        this.recStartY = recStartY;
    }

    public float getRecWidth() {
        return recWidth;
    }

    public void setRecWidth(float recWidth) {
        this.recWidth = recWidth;
    }

    public float getRecHeight() {
        return recHeight;
    }

    public void setRecHeight(float recHeight) {
        this.recHeight = recHeight;
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

