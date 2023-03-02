package com.kodetr.apppost;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MainActivity extends AppCompatActivity implements
        SwipeRefreshLayout.OnRefreshListener,
        AdapterPost.MClickListener {

    private RecyclerView rv;
    private SwipeRefreshLayout swipe;
    private ProgressDialog prgDialog;
    private AdapterPost adapterPost;

    private DatabaseReference mDReference;
    private FirebaseDatabase mFInstance;
    private StorageReference sReference;

    private LinearLayout llcontainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setTitle("POST LIST");

        init();
        initFirebase();
    }

    private void init() {
        llcontainer = findViewById(R.id.llcontainer);

        rv = findViewById(R.id.rv);
        swipe = findViewById(R.id.swipe_refresh);
        swipe.setColorSchemeColors(getResources().getColor(android.R.color.white), getResources().getColor(android.R.color.white), getResources().getColor(android.R.color.white));
        swipe.setProgressBackgroundColorSchemeColor(getResources().getColor(R.color.purple_200));
        swipe.setOnRefreshListener(this);
        swipe.post(() -> {
                    swipe.setRefreshing(true);
                    readData();
                }
        );

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AddActivity.class));
            }
        });

    }

    private void initFirebase() {
        FirebaseApp.initializeApp(this);
        mFInstance = FirebaseDatabase.getInstance();
        mDReference = mFInstance.getReference(Constants.POST);
        sReference = FirebaseStorage.getInstance().getReference(Constants.POST);
    }

    @Override
    public void onRefresh() {
        readData();
    }

    private void readData() {
        configRecycle();

        mDReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getChildrenCount() > 0) {
                    for (DataSnapshot mDataSnapshot : dataSnapshot.getChildren()) {
                        ModelPost mp = mDataSnapshot.getValue(ModelPost.class);
                        adapterPost.addModelPost(mp);
                    }

                    llcontainer.setVisibility(View.GONE);
                } else {
                    llcontainer.setVisibility(View.VISIBLE);
                }

                swipe.setRefreshing(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, databaseError.getDetails() + " " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                swipe.setRefreshing(false);
            }
        });
    }

    public void configRecycle() {
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapterPost = new AdapterPost(this);
        rv.setAdapter(adapterPost);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        readData();
    }

    @Override
    public void onClick(int position) {
        ModelPost modelPost = adapterPost.getModelPost(position);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Action");
        builder.setItems(new String[]{"Update", "Delete"}, (dialog, item) -> {
            if (item == 0) {
//              TODO: UPDATE
                Intent in = new Intent(MainActivity.this, UpdateActivity.class);
                in.putExtra("id", modelPost.getId());
                in.putExtra("image_url", modelPost.getImage_url());
                in.putExtra("note", modelPost.getNote());
                startActivity(in);

            } else {
//              TODO: DELETE

                final android.app.AlertDialog.Builder builder2 = new android.app.AlertDialog.Builder(this);
                builder2.setMessage("Sure you want to delete");
                builder2.setPositiveButton("Yes", (dialogInterface, ii) -> {

                    if (mDReference != null) {
                        prgDialog = new ProgressDialog(this);
                        prgDialog.setMessage("Process delete...");
                        prgDialog.setCancelable(false);
                        prgDialog.show();

                        mDReference.child(modelPost.getId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void mVoid) {
                                // delete file storage
                                sReference.child(modelPost.getId()).delete();

                                Toast.makeText(MainActivity.this, "Successfully deleted", Toast.LENGTH_LONG).show();
                                prgDialog.dismiss();
                                readData();
                            }
                        });
                    }

                });
                builder2.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
                builder2.show();

            }
        });
        builder.show();
    }

    private void searchPost(String qry) {
        configRecycle();

        Query query = mDReference.orderByChild("note").startAt(qry).endAt(qry+"\uf8ff");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot mDataSnapshot : dataSnapshot.getChildren()) {
                    ModelPost mp = mDataSnapshot.getValue(ModelPost.class);
                    adapterPost.addModelPost(mp);
                }
                swipe.setRefreshing(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, databaseError.getDetails() + " " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                swipe.setRefreshing(false);
            }
        });
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.options_menu, menu);
//
//        // Associate searchable configuration with the SearchView
//        MenuItem myActionMenuItem = menu.findItem(R.id.action_search);
//        SearchView searchView = (SearchView) myActionMenuItem.getActionView();
//
//                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                searchPost(query);
//                if (!searchView.isIconified()) {
//                    searchView.setIconified(true);
//                }
////                myActionMenuItem.collapseActionView();
//                return false;
//
//            }
//
//            @Override
//            public boolean onQueryTextChange(String s) {
//                // UserFeedback.show( "SearchOnQueryTextChanged: " + s);
//                return false;
//            }
//        });
//        return true;
//    }

}
