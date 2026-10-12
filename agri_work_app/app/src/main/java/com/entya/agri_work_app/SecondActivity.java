package com.entya.agri_work_app;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_second);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //ボタン遷移設定
        Button button = findViewById(R.id.button_se);
        button.setOnClickListener(v -> {
            Intent intent = new Intent( SecondActivity.this,MainActivity.class);
            startActivity(intent);
        });


        RecyclerView recyclerView = findViewById(R.id.table_id);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DBHelper helper = new DBHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();

        ArrayList<WorkItem> list = new ArrayList<>();

        Cursor cursor = db.query(
                "work_history",
                null,
                null,
                null,
                null,
                null,
                "id DESC"
        );

        while (cursor.moveToNext()) {

            String date_s = cursor.getString(
                    cursor.getColumnIndexOrThrow("date")
            );

            String job_s = cursor.getString(
                    cursor.getColumnIndexOrThrow("job")
            );

            String memo_s = cursor.getString(
                    cursor.getColumnIndexOrThrow("memo")
            );

            String image_url = cursor.getString(
                    cursor.getColumnIndexOrThrow("image_url")
            );


            String state_s = cursor.getString(
                    cursor.getColumnIndexOrThrow("state")
            );

            list.add(new WorkItem(date_s, job_s, memo_s,image_url,state_s));
        }

        cursor.close();

        WorkAdapter adapter = new WorkAdapter(list);
        recyclerView.setAdapter(adapter);
    }
}