package com.example.groupstudyingapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Objects;

public class CoursePageActivity extends AppCompatActivity implements CoursePageAdapter.ItemClickListener {
    public static final String IMAGE_UPLOADED = "image_uploaded";

    CoursePageAdapter adapter;
    private ArrayList<Question> questions;
    AppData appData;
    FireStoreHandler fireStoreHandler;
    private Course course;


    /**
     * The broadcast receiver of the activity
     **/
    private BroadcastReceiver br;

    /**
     * The local path of the last image taken by the camera
     **/
    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_page);
        getAppData();
        // data to populate the RecyclerView with
        questions = course.getQuestions();
        setRecyclerView();
        findViewById(R.id.addQuestionButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), AddQuestionActivity.class);
                startActivity(intent);
            }
        });
        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(IMAGE_UPLOADED)) {
                    String questionUrl = intent.getStringExtra("UPDATED URL");
                    Question question = (Question) intent.getSerializableExtra("UPDATED QUESTION");
                    addNewQuestion(Objects.requireNonNull(question), questionUrl);
                    fireStoreHandler.updateData();
                }
            }
        };
        IntentFilter filter = new IntentFilter(IMAGE_UPLOADED);
        this.registerReceiver(br, filter);
    }

    //TODO - Ido - is imagePath needed here?
    private void addNewQuestion(Question newQuestion, String questionLink) { //todo should'nt return Question but id
        newQuestion.setLink(questionLink);
        questions.add(newQuestion);
        adapter.notifyDataSetChanged();
    }


//    private void showInsertDialog() {
//        final FlatDialog flatDialog = new FlatDialog(CoursePageActivity.this);
//        flatDialog.setFirstTextField("");
//        flatDialog.setTitle("Add a question")
//                .setSubtitle("Write your question title here")
//                .setFirstTextFieldHint("Question title")
//                .setFirstButtonText("Camera")
//                .setSecondButtonText("From Gallery")
//                .setThirdButtonText("Done")
//                .withFirstButtonListner(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        dispatchTakePictureIntent();
//                    }
//                })
//                .withSecondButtonListner(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
//                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                        startActivityForResult(pickPhoto, GALLERY_ACTION);
//                    }
//                })
//                .withThirdButtonListner(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        if (flatDialog.getFirstTextField().equals("")) {
//                            Toast.makeText(CoursePageActivity.this, "Please write title", Toast.LENGTH_SHORT).show();
//                        } else {
//                            String questionTitleInput = flatDialog.getFirstTextField();
//                            if (!isPhotoEntered) {
//                                Toast.makeText(CoursePageActivity.this, PLS_UPLOAD_IMG, Toast.LENGTH_SHORT).show();
//                            } else {
//                                Question newQuestion = new Question(questionTitleInput,
//                                        newQuestionImagePath);
//                                fireStoreHandler.uploadQuestionImage(newImageUri,
//                                        newQuestionImagePath,
//                                        newQuestion,
//                                        CoursePageActivity.this);
//                                flatDialog.dismiss();
//                            }
//                        }
//
//                    }
//                })
//                .show();
//        flatDialog.setCanceledOnTouchOutside(true);
//    }

    private void setRecyclerView() {
        // set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CoursePageAdapter(this, questions);
        adapter.setClickListener(CoursePageActivity.this);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getBaseContext(),
                DividerItemDecoration.VERTICAL));
    }


    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(getBaseContext(), QuestionActivity.class);
        intent.putExtra("EXTRA_SESSION_ID", questions.get(position));
        startActivity(intent);
    }

    private void insertSingleItem(Question newQuestion) {
        questions.add(newQuestion);
        adapter.notifyDataSetChanged();
    }


    private void removeSingleItem(int removeIndex) {
        questions.remove(removeIndex);
        adapter.notifyItemRemoved(removeIndex);
    }

    private void removeAllItems() {
        questions.clear();
        adapter.notifyDataSetChanged();
    }

    //TODO - can we delete it from here?
    private void getAppData() {
        //TODO - the data won't be loaded again like this, were gonna send only the relevant course from main activity
        appData = (AppData) getApplicationContext();
        fireStoreHandler = appData.fireStoreHandler;
        course = fireStoreHandler.getCurrentCourse();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(br);
    }
}

