package com.bbqrepairdoctor.model;

import java.util.Date;

/**
 * Created by ebendersky on 10/14/17.
 */
public class ClickInfo {
    public String keyWord;
    public String clickDetails;
    private Date created = new Date();
    public ClickInfo(String keyWord, String clickDetails){
        this.keyWord = keyWord;
        this.clickDetails = clickDetails;
    }
}
