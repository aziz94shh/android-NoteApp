package edu.uoregon.abdulazizalheidous.noteapp;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Aziz
 */
public class Folder extends RealmObject {
    @PrimaryKey
    private String  name;


    public RealmList<Memo> getMemoes() {
        return memoes;
    }

    public void setMemoes(RealmList<Memo> memoes) {
        this.memoes = memoes;
    }

    private RealmList<Memo> memoes;

    public String getName() { return name; }
    public void   setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return name;   // used for add/edit spinner
    }
}
