package com.entya.agri_work_app.activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.widget.EditText;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.net.Uri;
import android.provider.MediaStore;
import android.app.TimePickerDialog;

import java.util.Calendar;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.entya.agri_work_app.database.DBHelper;
import com.entya.agri_work_app.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private Uri selectedImageUri;

    @Override
    //画面描画時に実行
    protected void onResume() {
        super.onResume();

        //現在時刻を取得し、HH:mm形式に整形して表示
        EditText editTime = findViewById(R.id.t_date);

        String currentTime = new SimpleDateFormat(
                "HH:mm",
                Locale.getDefault()
        ).format(new Date());

        editTime.setText(currentTime);
    }

    @Override
    //初回起動時に実行
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //ナビゲーションバー非表示
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());

        controller.hide(WindowInsetsCompat.Type.navigationBars());
        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );

        //画面設定
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //DB設定
        dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //ボタン遷移設定
        Button button = findViewById(R.id.list_button);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        //時刻設定
        EditText editTime = findViewById(R.id.t_date);
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);

        editTime.setText(String.format("%02d:%02d", hour, minute));

        editTime.setOnClickListener(v -> {
            TimePickerDialog dialog = new TimePickerDialog(
                    this,
                    (view, h, m) -> editTime.setText(
                            String.format("%02d:%02d", h, m)
                    ),
                    hour,
                    minute,
                    true
            );

            dialog.show();
        });

        //CSVボタン
        Button csv_button = findViewById(R.id.export_csv);

        csv_button.setOnClickListener(v -> {
            exportCSV();
        });

        //開始ボタン
        Button start_button = findViewById(R.id.start_button);
        start_button.setOnClickListener(v -> {
            registerData("開始");
        });


        //終了ボタン
        Button finish_button = findViewById(R.id.finish_button);
        finish_button.setOnClickListener(v -> {
            registerData("終了");
        });

        //画面登録ボタン
        Button buttonImage = findViewById(R.id.imageButton);

        buttonImage.setOnClickListener(v -> {
            imagePicker.launch("image/*");
        });

        //テキスト設定
        SharedPreferences prefs = getSharedPreferences("save_data", MODE_PRIVATE);
        String text = prefs.getString("job_save", "");

        EditText editTextWork = findViewById(R.id.t_job);
        editTextWork.setText(text);
    }

    /** テーブル登録*/
    private void registerData(String status_s) {

        EditText t_job = findViewById(R.id.t_job);
        EditText t_memo = findViewById(R.id.t_memo);
        EditText t_date = findViewById(R.id.t_date);

        String t_job_s = t_job.getText().toString();
        String t_memo_s = t_memo.getText().toString();
        String t_date_s = t_date.getText().toString();

        ContentValues values = new ContentValues();

        //現在日付取得
        String currentDate = new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
        ).format(new Date());

        // 日付 + 画面で指定した時刻
        String dateTime = currentDate + " " + t_date_s + ":00";

        values.put("date", dateTime);
        values.put("job", t_job_s);
        values.put("memo", t_memo_s);
        values.put("state", status_s);
        String image_uri = "";
        if (selectedImageUri != null)
        {
            image_uri = selectedImageUri.toString();
        }
        values.put("image_path",image_uri);

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long result = db.insert("work_history", null, values);

        if (result != -1) {
            Toast.makeText(this, "登録完了しました。", Toast.LENGTH_SHORT).show();
        }

        //記載した内容を保存
        SharedPreferences prefs = getSharedPreferences("save_data", MODE_PRIVATE);
        prefs.edit().putString("job_save", t_job_s).apply();

        //終了ならテキストを空にする
        if(Objects.equals(status_s, "終了")){
            t_job.setText("");
            t_memo.setText("");
        }
    }

    //画像取得処理
    private final ActivityResultLauncher<String> imagePicker =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            ImageView imageView = findViewById(R.id.imageView3);
                            imageView.setImageURI(uri);
                            selectedImageUri = uri;
                        }
                    }
            );

    //CSV出力
    private void exportCSV() {

        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM work_history", null);

        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, "work_history.csv");
        values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");

        Uri uri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            uri = getContentResolver().insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    values
            );
        }

        try {
            OutputStream outputStream =
                    getContentResolver().openOutputStream(uri);

            OutputStreamWriter writer =
                    new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);

            writer.write("id,date,job,memo,state,image_url\n");

            while (cursor.moveToNext()) {
                writer.write(cursor.getString(0) + ",");
                writer.write(cursor.getString(1) + ",");
                writer.write(cursor.getString(2) + ",");
                writer.write(cursor.getString(3) + ",");
                writer.write(cursor.getString(4) + ",");
                writer.write(cursor.getString(5) + "\n");
            }

            writer.close();
            cursor.close();

            Toast.makeText(this,
                    "DownloadsにCSVを保存しました",
                    Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}