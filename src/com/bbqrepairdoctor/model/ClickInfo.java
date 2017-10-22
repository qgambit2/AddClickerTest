package com.bbqrepairdoctor.model;

/**
 * Created by ebendersky on 10/14/17.
 */
public class ClickInfo {
    public String keyWord;
    public String clickDetails;
    public ClickInfo(String keyWord, String clickDetails){
        this.keyWord = keyWord;
        this.clickDetails = clickDetails;
    }
}
