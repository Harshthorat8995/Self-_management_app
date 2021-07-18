package com.example.navigationapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.util.Date;

public class ToDoList extends AppCompatActivity {

//    Variables
    public RecyclerView recyclerView;
    public FloatingActionButton floatingActionButton;
    private TextView textView;

    private DatabaseReference reference;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private String onlineUserID;


    private ProgressDialog loader;

    private String key = "";
    private String task;
    private String description;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_todolist);

        ///Adds toolbar and title on the toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("To do list");





        mAuth = FirebaseAuth.getInstance();



         recyclerView = findViewById(R.id.recyleview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        textView = findViewById(R.id.textView3);

        loader = new ProgressDialog(this);

        mUser = mAuth.getCurrentUser();
        onlineUserID = mUser.getUid();
        reference = FirebaseDatabase.getInstance().getReference().child("tasks").child(onlineUserID);

//        Hook for floating action button and code when button is clicked
        floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                addtask();
                textView.setVisibility(View.GONE);
            }
        });

    }

//    When clicked on floating action button, inflates input_file (dialog)
    private void addtask() {
        AlertDialog.Builder mydialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);

        View myview = inflater.inflate(R.layout.input_file, null);
        mydialog.setView(myview);

        final AlertDialog dialog = mydialog.create();
        dialog.setCancelable(false);

        final EditText task = myview.findViewById(R.id.task);
        final EditText description = myview.findViewById(R.id.decription);
        Button save = myview.findViewById(R.id.savebtm);
        Button cancel = myview.findViewById(R.id.cancelbutton);



//        When cancel button is clicked dialog closes
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

//        When save button is clicked it will show errors if task and description is left empty. If everything is perfect it show message
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mTask = task.getText().toString().trim();
                String mdescription = description.getText().toString().trim();
                String id = reference.push().getKey();
                String date = DateFormat.getDateInstance().format(new Date());

                if(TextUtils.isEmpty(mTask)){
                    task.setError("Task required");
                    return;
                }
                if (TextUtils.isEmpty(mdescription)){
                    description.setError("Description required");
                    return;
                }else {
                    loader.setMessage("Adding your data");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();


//                    Saves data as realtime database on firebase
                    Model model = new Model(mTask, mdescription, id, date);
                    reference.child(id).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {

//                        On complete it will show Toast messages
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(ToDoList.this, "Task has been inserted succesfully", Toast.LENGTH_SHORT).show();
                                loader.dismiss();
                            }else {
                                String error = task.getException().toString();
                                Toast.makeText(ToDoList.this, "Failed: " + error, Toast.LENGTH_SHORT).show();
                                loader.dismiss();
                            }

                        }
                    });

                }

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Model> options = new FirebaseRecyclerOptions.Builder<Model>()
                .setQuery(reference, Model.class)
                .build();

        FirebaseRecyclerAdapter<Model, MyViewHolder> adapter = new FirebaseRecyclerAdapter<Model, MyViewHolder>(options) {

            //Fetches data from model class and displays data at specific position
            @Override
            protected void onBindViewHolder(@NonNull @NotNull MyViewHolder holder, int position, @NonNull @NotNull Model model) {
                holder.setdate(model.getDate());
                holder.setTask(model.getTask());
                holder.setDesc(model.getDescription());

                //when one particular task is clicked, it will start updatetask();
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        key = getRef(position).getKey();
                        task = model.getTask();
                        description = model.getDescription();

                        updateTask();

                    }
                });

            }

//            Inflates retrieved_layout after fetching data of the user from realtime database
            @NonNull
            @NotNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
               View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.retrived_layout, parent, false);
               return  new MyViewHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public MyViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            mView = itemView;

        }
//        Sets task i.e fetches data from realtime database and shows it
        public void setTask(String task){
            TextView tasktextview = mView.findViewById(R.id.tasktv);
            tasktextview.setText(task);
        }

//        Sets description i.e fetches data from realtime database and shows it
        public void setDesc(String desc){
            TextView desctextview = mView.findViewById(R.id.description);
            desctextview.setText(desc);
        }

//        setDate i.e fetches data from realtime database and shows it
        public void setdate(String date){
            TextView datetext = mView.findViewById(R.id.datetv);
            datetext.setText(date);
        }
    }

//    Inflates update_data when clicked on task
    private void updateTask(){
        AlertDialog.Builder mydialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.update_data, null);
        mydialog.setView(view);

        AlertDialog dialog = mydialog.create();

        EditText mtask = view.findViewById(R.id.medittexttask);
        EditText mdescription = view.findViewById(R.id.medittextdescription);

        mtask.setText(task);
        mtask.setSelection(task.length());

        mdescription.setText(description);
        mdescription.setSelection(description.length());

        Button delbutton = view.findViewById(R.id.btndelete);
        Button updatebutton = view.findViewById(R.id.btnupdate);


//        When update button is clicked it will show errors if task and description is left empty. If everything is perfect it show message
        updatebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                task = mtask.getText().toString().trim();
                description = mdescription.getText().toString().trim();

                if(TextUtils.isEmpty(task)){
                    mtask.setError("Task required");
                    return;
                }
                if (TextUtils.isEmpty(description)){
                    mdescription.setError("Description required");
                    return;
                }else {
                    loader.setMessage("Adding your data");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();}


                String date = DateFormat.getDateInstance().format(new Date());

                Model model = new Model(task, description, key, date);

//                When the task and description is updated successfully the user should get any of these two toast messages

                reference.child(key).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {

                        if(task.isSuccessful()){
                            Toast.makeText(ToDoList.this, "Data has been updated succesfully", Toast.LENGTH_SHORT).show();
                        }else {
                            String err = task.getException().toString();
                            Toast.makeText(ToDoList.this, "Update failed " + err, Toast.LENGTH_SHORT).show();
                        }

                    }
                });

                dialog.dismiss();
            }
        });


//        When delete button is clicked it will remove that data from realtime database
        delbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reference.child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {

//                    On complete it will show Toast messages
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(ToDoList.this, "Task deleted successfully", Toast.LENGTH_SHORT).show();
                        }else{
                            String err = task.getException().toString();
                            Toast.makeText(ToDoList.this, "Failed to delete task " + err, Toast.LENGTH_SHORT).show();

                        }

                    }
                });


                dialog.dismiss();
            }
        });

        dialog.show();
    }

    ///opens mainmenu on toolbar which shows logout button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);

    }

    ///User logouts when clicked on logout button
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                mAuth.signOut();
                Intent intent = new Intent(ToDoList.this, Login.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
        }
        return super.onOptionsItemSelected(item);
    }
}