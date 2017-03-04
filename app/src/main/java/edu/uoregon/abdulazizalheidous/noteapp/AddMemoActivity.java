package edu.uoregon.abdulazizalheidous.noteapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Aziz
 */
public class AddMemoActivity extends AppCompatActivity {

    private String folderName;
    TextView title, text;
    private boolean editMode;
    private int memoId;
    private Dal dal;
    private RealmConfiguration realmConfig;
    Memo memo;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_memo);

        realmConfig = new RealmConfiguration
                .Builder(this)
                .deleteRealmIfMigrationNeeded()
                .build();
        // Open the Realm for the UI thread.

        Realm.setDefaultConfiguration(realmConfig);

        dal = new Dal(realmConfig);


        title = (TextView) findViewById(R.id.AddMemoTitleTextView);
        text = (TextView) findViewById(R.id.AddMemoTextTextView);

        Intent intent = getIntent();
        folderName = intent.getStringExtra("folderName");
        editMode = intent.getBooleanExtra("editMode", false);
        memoId = intent.getIntExtra("memoId", -1);

        setTitle(folderName);

        if (editMode){
            memo = dal.getMemoFromDb(memoId);
            title.setText(memo.getTitle());
            text.setText(memo.getText());
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_add_memo_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menuDone:
                String _title = title.getText().toString();
                String _text = text.getText().toString();
                if (_text.trim().equals("") || _title.trim().equals("")){
                    if (_text.trim().equals("") && _title.trim().equals("")){
//                        Toast.makeText(this,
//                                "\"Please enter the title of the memo memo\"",
//                                Toast.LENGTH_LONG).show();
                        finish();
                    }
                    else if (_text.trim().equals("")){
                        Toast.makeText(this,
                                "\"Please enter some text in the memo\"",
                                Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toast.makeText(this,
                                "\"Please enter the title of the memo memo\"",
                                Toast.LENGTH_LONG).show();
                    }
                    break;
                }
                if (editMode){
                    dal.updateMemo(memo, _title, _text);
                }
                else {
                    dal.insertMemo(folderName, _title, _text);
                }
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


}
