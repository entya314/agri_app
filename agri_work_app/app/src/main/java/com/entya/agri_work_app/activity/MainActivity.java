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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

import com.entya.agri_work_app.Csv;
import com.entya.agri_work_app.database.DBHelper;
import com.entya.agri_work_app.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private Uri selectedImageUri;
    private final ExecutorService csvExecutor = Executors.newSingleThreadExecutor();
    private final ActivityResultLauncher<String[]> csvPicker = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) importCSV(uri);
            });

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

        findViewById(R.id.import_csv).setOnClickListener(v ->
                csvPicker.launch(new String[]{"text/*", "application/csv", "application/octet-stream"}));

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

    // CSV全体を一つのトランザクションで取り込む。
    private void importCSV(Uri uri) {
        csvExecutor.execute(() -> {
            try {
                int inserted = 0;
                int updated = 0;
                try (InputStream input = getContentResolver().openInputStream(uri)) {
                    if (input == null) throw new IOException("CSVを開けません");
                    try (PushbackReader reader = new PushbackReader(
                            new InputStreamReader(input, StandardCharsets.UTF_8), 1)) {
                        List<String> header = Csv.readRow(reader);
                        if (header == null) throw new IOException("CSVが空です");
                        header.set(0, header.get(0).replaceFirst("^\uFEFF", ""));
                        String[] names = {"id", "date", "job", "memo", "state", "image_path"};
                        int[] indexes = new int[names.length];
                        for (int i = 0; i < names.length; i++) {
                            indexes[i] = header.indexOf(names[i]);
                            if (indexes[i] < 0) throw new IOException("必要な列がありません: " + names[i]);
                            if (header.lastIndexOf(names[i]) != indexes[i])
                                throw new IOException("列名が重複しています: " + names[i]);
                        }
                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        db.beginTransaction();
                        try {
                            List<String> row;
                            int record = 1;
                            while ((row = Csv.readRow(reader)) != null) {
                                record++;
                                if (row.size() == 1 && row.get(0).isEmpty()) continue;
                                if (row.size() != header.size())
                                    throw new IOException(record + "件目: 列数が一致しません");
                                long id;
                                try {
                                    id = Long.parseLong(row.get(indexes[0]).trim());
                                    if (id <= 0) throw new NumberFormatException();
                                } catch (NumberFormatException e) {
                                    throw new IOException(record + "件目: idは正の整数で指定してください");
                                }
                                ContentValues values = new ContentValues();
                                for (int i = 1; i < names.length; i++)
                                    values.put(names[i], row.get(indexes[i]));
                                int count = db.update("work_history", values, "id = ?",
                                        new String[]{Long.toString(id)});
                                if (count == 0) {
                                    values.put("id", id);
                                    db.insertOrThrow("work_history", null, values);
                                    inserted++;
                                } else updated++;
                            }
                            db.setTransactionSuccessful();
                        } finally {
                            db.endTransaction();
                        }
                    }
                }
                String message = "CSV取込完了: 追加 " + inserted + "件、更新 " + updated + "件";
                runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_LONG).show());
            } catch (IOException | RuntimeException e) {
                runOnUiThread(() -> Toast.makeText(this,
                        "CSV取込失敗（変更は保存されていません）: " + e.getMessage(),
                        Toast.LENGTH_LONG).show());
            }
        });
    }

    @Override
    protected void onDestroy() {
        csvExecutor.shutdown();
        super.onDestroy();
    }

    // 列名を明示して出力し、メモ内のカンマ・改行も保持する。
    private void exportCSV() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Toast.makeText(this, "CSV出力はAndroid 10以降に対応しています", Toast.LENGTH_LONG).show();
            return;
        }
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, "work_history.csv");
        values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
        values.put(MediaStore.Downloads.IS_PENDING, 1);
        Uri uri = null;
        try {
            uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri == null) throw new IOException("保存先を作成できません");
            String[] columns = {"id", "date", "job", "memo", "state", "image_path"};
            try (Cursor cursor = dbHelper.getReadableDatabase().query(
                    "work_history", columns, null, null, null, null, "id ASC");
                 OutputStream stream = getContentResolver().openOutputStream(uri)) {
                if (stream == null) throw new IOException("保存先を開けません");
                try (OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
                    Csv.writeRow(writer, columns);
                    while (cursor.moveToNext()) {
                        String[] row = new String[columns.length];
                        for (int i = 0; i < columns.length; i++) row[i] = cursor.getString(i);
                        Csv.writeRow(writer, row);
                    }
                }
            }
            ContentValues ready = new ContentValues();
            ready.put(MediaStore.Downloads.IS_PENDING, 0);
            getContentResolver().update(uri, ready, null, null);
            Toast.makeText(this, "DownloadsにCSVを保存しました", Toast.LENGTH_LONG).show();
        } catch (IOException | RuntimeException e) {
            if (uri != null) getContentResolver().delete(uri, null, null);
            Toast.makeText(this, "CSV出力失敗: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
