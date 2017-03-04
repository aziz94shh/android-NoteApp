package edu.uoregon.abdulazizalheidous.noteapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Aziz
 */
public class MemoActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener{

    private Dal dal;
    private RealmConfiguration realmConfig;
    private String folderName;
    private final Context context = this;
    private ListView memoListView;
    private ArrayList<HashMap<String, String>> data = null;
    private SimpleAdapter adapter = null;
    private int visibility = View.GONE;
    private Menu menu;
    private boolean editMode =false;
    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo);

        realmConfig = new RealmConfiguration
                .Builder(this)
                .deleteRealmIfMigrationNeeded()
                .build();
        // Open the Realm for the UI thread.

        Realm.setDefaultConfiguration(realmConfig);
        settings = getSharedPreferences("aziz", Context.MODE_PRIVATE);

        dal = new Dal(realmConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        folderName = intent.getStringExtra("folderName");

        if (folderName == null){
            folderName = settings.getString("folderName", "");
        }else {
            settings.edit().putString("folderName", folderName).commit();
        }

        setTitle(folderName);

        memoListView  = (ListView) findViewById(R.id.MemoListView);

        data = new ArrayList<HashMap<String, String>>();
        dal.getMemoes(data, folderName);

        adapter = new RunAdapter(this,
                data,
                R.layout.memo_list,
                new String[]{"Title", "Date", "ShortText"},
                new int[]{R.id.MemoNameTextView, R.id.MemoDateTextView, R.id.MemoShortTextTextView}
        );
        memoListView.setAdapter(adapter);
        memoListView.setOnItemClickListener(this);

        editMode = settings.getBoolean("MemoEditMode", false);
    }

    @Override
    protected void onPause() {
        settings.edit().putBoolean("MemoEditMode", editMode).commit();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.activity_memo_list, menu);
        if (editMode){
            menu.findItem(R.id.menuAddMemo).setVisible(false);
            menu.findItem(R.id.menuEdit).setVisible(false);
            menu.findItem(R.id.menuDone).setVisible(true);
            visibility = View.VISIBLE;
            adapter.notifyDataSetChanged();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menuAddMemo:
                //getFoldername();
                Intent intent = new Intent(this, AddMemoActivity.class);
                intent.putExtra("folderName", folderName);
                startActivity(intent);
                break;
            case R.id.menuEdit:
                editMode = true;
                menu.findItem(R.id.menuAddMemo).setVisible(false);
                menu.findItem(R.id.menuEdit).setVisible(false);
                menu.findItem(R.id.menuDone).setVisible(true);
                visibility = View.VISIBLE;
                adapter.notifyDataSetChanged();
                break;
            case R.id.menuDone:
                editMode = false;
                menu.findItem(R.id.menuAddMemo).setVisible(true);
                menu.findItem(R.id.menuEdit).setVisible(true);
                menu.findItem(R.id.menuDone).setVisible(false);
                visibility = View.GONE;
                adapter.notifyDataSetChanged();

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(this, AddMemoActivity.class);
        intent.putExtra("folderName", folderName);
        intent.putExtra("editMode", true);
        intent.putExtra("memoId", Integer.parseInt(data.get(i).get("Id")));
        startActivity(intent);
    }

    public class RunAdapter extends SimpleAdapter {

        public RunAdapter(Context context, List<HashMap<String, String>> items,
                          int resource, String[] from, int[] to) {
            super(context, items, resource, from, to);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final View view = super.getView(position, convertView, parent);
            //Button editButton = (Button) view.findViewById(R.id.EditButton);
            Button deleteButton = (Button) view.findViewById(R.id.DeleteButton);
            //editButton.setVisibility(visibility);
            deleteButton.setVisibility(visibility);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteMemo(position);
                }
            });
            return view;
        }
    }

    private void deleteMemo(final int position) {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.prompt_delete, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        ((TextView) promptsView.findViewById(R.id.PromotTextView1))
                .setText("Are you sure you want to delete this memo?");

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Delete",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                onMemoDeleteClicked(position);
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }


    private void onMemoDeleteClicked(int position) {
        int id = Integer.parseInt(data.get(position).get("Id"));
        Toast.makeText(context,
                "memo deleted",
                Toast.LENGTH_LONG).show();
        dal.deleteMemo(id);
        dal.getMemoes(data, folderName);
        adapter.notifyDataSetChanged();
    }
}
