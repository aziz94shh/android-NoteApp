package edu.uoregon.abdulazizalheidous.noteapp;

import java.util.ArrayList;
import java.util.HashMap;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Aziz
 */
public class Dal {
    private Realm realm;

    public Dal(RealmConfiguration realmConfig){
        realm = Realm.getInstance(realmConfig);
    }

    public Folder getFolderFromDb(String folderName){
        Folder folder = realm.where(Folder.class)
                .equalTo("name", folderName)
                .findFirst();
        return folder;
    }

    public boolean doesFolderExist(String folderName){
        return ! realm.where(Folder.class)
                .equalTo("name", folderName).findAll().isEmpty();
    }

    public boolean insertFolder(String folderName){
        boolean bool = false;
        if (folderName.trim() != ""){
            realm = Realm.getDefaultInstance();
            realm.beginTransaction();


            Folder folder = realm.createObject(Folder.class);
            try {
                folder.setName(folderName);
                realm.commitTransaction();
                bool = true;
            }catch (io.realm.exceptions.RealmPrimaryKeyConstraintException e){
                realm.cancelTransaction();
            }
        }
        return bool;
    }

    // public methods
    public void getFolderList(ArrayList<HashMap<String, String>> data) {

        data.clear();
        realm = Realm.getDefaultInstance();
        RealmResults<Folder> result = realm.where(Folder.class).findAll();
        for (Folder folder: result) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("Name", folder.getName());
            map.put("MemoNum", String.valueOf(folder.getMemoes().size()));
            data.add(map);
        }
    }

    public void deleteFolder(String folderName) {
        realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        Folder folder = getFolderFromDb(folderName);
        folder.getMemoes().deleteAllFromRealm();
        folder.deleteFromRealm();
        realm.commitTransaction();
    }

    public Memo getMemoFromDb(int id){
        realm = Realm.getDefaultInstance();
        Memo memo = realm.where(Memo.class)
                .equalTo("id", id)
                .findFirst();

        return memo;
    }

    public void getMemoes(ArrayList<HashMap<String, String>> data, String folderName) {
        data.clear();

        Folder folder = getFolderFromDb(folderName);
        RealmResults<Memo> memoes = folder.getMemoes().sort("date", Sort.DESCENDING);


        for (Memo memo: memoes) {

            HashMap<String, String> map = new HashMap<String, String>();
            map.put("Title", memo.getTitle());
            map.put("Date", memo.getDate());
            map.put("ShortText", memo.getShortText());
            map.put("Id", String.valueOf(memo.getId()));
            data.add(map);
        }

    }

    public void insertMemo(String folderName, String title, String text){
        realm = Realm.getDefaultInstance();
        Folder folder = getFolderFromDb(folderName);

        realm.beginTransaction();
        Memo memo = realm.createObject(Memo.class);
        memo.setDate();
        memo.setTitle(title);
        memo.setText(text);

        int id  = ( realm.where(Memo.class).findAll().max("id")).intValue() +1;

        memo.setId(id);

        folder.getMemoes().add(memo);
        realm.commitTransaction();
    }

    public void deleteMemo(int id) {
        realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        Memo memo = getMemoFromDb(id);
        memo.deleteFromRealm();
        realm.commitTransaction();
    }


    public void updateFolderName(String oldFolderName, String newFolderName) {
        Folder folder = getFolderFromDb(oldFolderName);

        realm.beginTransaction();

        folder.setName(newFolderName);

        realm.commitTransaction();
    }

    public void updateMemo(Memo memo, String title, String text) {

        realm.beginTransaction();

        memo.setTitle(title);
        memo.setText(text);
        memo.setDate();

        realm.commitTransaction();

    }
}
