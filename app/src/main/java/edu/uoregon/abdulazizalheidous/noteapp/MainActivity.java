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
import android.widget.EditText;
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
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private final Context context = this;
    private Dal dal;
    private RealmConfiguration realmConfig;
    private ListView folderListView;
    private ArrayList<HashMap<String, String>> data = null;
    private SimpleAdapter adapter = null;
    private Menu menu;
    private int visibility = View.GONE;
    private SharedPreferences settings;
    private boolean editMode =false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        folderListView  = (ListView) findViewById(R.id.FolderListView);

        realmConfig = new RealmConfiguration
                .Builder(this)
                .deleteRealmIfMigrationNeeded()
                .build();
        // Open the Realm for the UI thread.

        Realm.setDefaultConfiguration(realmConfig);

        dal = new Dal(realmConfig);

        settings = getSharedPreferences("aziz", Context.MODE_PRIVATE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        data = new ArrayList<HashMap<String, String>>();
        dal.getFolderList(data);
        if (data.isEmpty()){
            dal.insertFolder("Notes");
            dal.getFolderList(data);
        }
        // set the adapter for the spinner
        adapter = new RunAdapter(this,
                data,
                R.layout.folder_list,
                new String[]{"Name", "MemoNum"},
                new int[]{R.id.FolderNameTextView, R.id.MemoNumTextView}
        );
        folderListView.setAdapter(adapter);
        folderListView.setOnItemClickListener(this);

        editMode = settings.getBoolean("FolderEditMode", false);

    }

    @Override
    protected void onPause() {
        settings.edit().putBoolean("FolderEditMode", editMode).commit();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.activity_task_list, menu);
        if (editMode){
            menu.findItem(R.id.menuAddFolder).setVisible(false);
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
            case R.id.menuAddFolder:
                getFoldername(false, null);
                break;
            case R.id.menuEdit:
                editMode = true;
                menu.findItem(R.id.menuAddFolder).setVisible(false);
                menu.findItem(R.id.menuEdit).setVisible(false);
                menu.findItem(R.id.menuDone).setVisible(true);
                visibility = View.VISIBLE;
                adapter.notifyDataSetChanged();
                break;
            case R.id.menuDone:
                editMode = false;
                menu.findItem(R.id.menuAddFolder).setVisible(true);
                menu.findItem(R.id.menuEdit).setVisible(true);
                menu.findItem(R.id.menuDone).setVisible(false);
                visibility = View.GONE;
                adapter.notifyDataSetChanged();

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getFoldername(final boolean editMode, final String oldFolderName) {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.prompts, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);

        if (editMode){
            ((TextView) promptsView.findViewById(R.id.PromotTextView1)).setText("Rename Folder");
            ((TextView) promptsView.findViewById(R.id.PromotTextView2)).setText("Enter a new name for this folder");
        }

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Save",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user input and set it to result
                                // edit text
                                String newFolderName = userInput.getText().toString();
                                if( (! dal.doesFolderExist(newFolderName)) && !newFolderName.trim().equals("")  ){
                                    if (editMode){
                                        dal.updateFolderName(oldFolderName, newFolderName);
                                    }
                                    else {
                                        dal.insertFolder(newFolderName);
                                    }
                                    dal.getFolderList(data);
                                    adapter.notifyDataSetChanged();
                                }
                                else {
                                    String err = "Name Taken\nPlease choose a different name.";
                                    Toast.makeText(context,
                                            err,
                                            Toast.LENGTH_LONG).show();

                                }

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

    private void deleteFolder(final String folderName) {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.prompt_delete, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);


        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Delete",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                onDeleteClicked(folderName);
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

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Realm realm = Realm.getDefaultInstance();
        Folder f = realm.where(Folder.class).findAll().get(i);
        Intent intent = new Intent(this, MemoActivity.class);
        intent.putExtra("folderName", f.getName());
        settings.edit().putBoolean("MemoEditMode", false).commit();
        startActivity(intent);
    }


    public class RunAdapter extends SimpleAdapter {

        public RunAdapter(Context context, List<HashMap<String, String>> items,
                          int resource, String[] from, int[] to) {
            super(context, items, resource, from, to);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View view = super.getView(position, convertView, parent);
            Button editButton = (Button) view.findViewById(R.id.EditButton);
            Button deleteButton = (Button) view.findViewById(R.id.DeleteButton);
            final String folderName = ((TextView)view.findViewById(R.id.FolderNameTextView)).getText().toString();

            editButton.setVisibility(visibility);
            deleteButton.setVisibility(visibility);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    deleteFolder(folderName);
                }
            });
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getFoldername(true, folderName);
                }
            });
            return view;
        }
    }

    
private void onDeleteClicked(String folderName) {
    Toast.makeText(context,
            folderName + " deleted",
            Toast.LENGTH_LONG).show();
            dal.deleteFolder(folderName);
            dal.getFolderList(data);
            adapter.notifyDataSetChanged();
    }}
