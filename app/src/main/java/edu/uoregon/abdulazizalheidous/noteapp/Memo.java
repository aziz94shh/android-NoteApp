package edu.uoregon.abdulazizalheidous.noteapp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Aziz
 */
public class Memo extends RealmObject {
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @PrimaryKey
    private int id;

    private String title;
    private String text;
    private Date date;

    @Ignore
    private static DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyy 'at' hh:mm aaa");

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        String temp = title.replaceAll("\n", " ");
        if (temp.length() > 23) {
            return temp.substring(0, 23) + "...";
        } else {
            return temp;
        }
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    public String getShortText() {
        String temp = text.replaceAll("\n", " ");
        if (temp.length() > 30) {
            return temp.substring(0, 30) + "...";
        } else {
            return temp;
        }
    }

    public void setDate(){
        this.date = new Date();
    }

    public String getDate() {
        return dateFormat.format(date);
    }

}
