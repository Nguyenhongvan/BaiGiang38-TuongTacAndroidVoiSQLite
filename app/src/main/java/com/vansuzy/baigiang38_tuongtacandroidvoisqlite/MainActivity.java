package com.vansuzy.baigiang38_tuongtacandroidvoisqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    // 1
    String DATABASE_NAME = "dbContact.sqlite";
    private static final String DB_PATH_SUFFIX = "/databases/";
    SQLiteDatabase database = null;

    ListView lvDanhBa;
    ArrayList<String> dsDanhBa;
    ArrayAdapter<String> adapterDanhBa;

    Button btnThemDanhBa, btnChinhSua, btnXoa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        xuLySaoChepCSDLTuAssetsVaoHeThongMobile();  // 2

        addControls();
        addEvents();

        showAllContactOnListView();
    }

    private void showAllContactOnListView() {
        // Bước 1: Mở CSDL trước
        database = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
        Cursor cursor = database.query("Contact", null, null, null, null, null, null);  // lúc này cursor đang trỏ tới NULL (chưa trỏ tới dòng dữ liệu nào trong bảng) => chưa thể truy xuất được đến bất kỳ dòng dữ liệu nào trong bảng
        dsDanhBa.clear();
        while (cursor.moveToNext()) // moveToNext(): di chuyển con trỏ cursor tới dòng kế tiếp và lấy thông tin ra. Khi không còn di chuyển được nữa (đang trỏ tới dòng cuối cùng trong bảng) thì cursor.moveToNext() sẽ bằng false (đồng nghĩa với ngừng vòng lặp)
        {
            int ma = cursor.getInt(0);  // tương ứng với cột Ma trong bảng Contact
            String ten = cursor.getString(1);
            String phone = cursor.getString(2);
            dsDanhBa.add(ma + "-" + ten + "\n" + phone);
        }
        cursor.close();
        adapterDanhBa.notifyDataSetChanged();
    }

    private void addEvents() {
        btnThemDanhBa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                xuLyThemDanhBa();
            }
        });
        btnChinhSua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                xuLyChinhSuaDanhBa();
            }
        });
        btnXoa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                xuLyXoaDanhBa();
            }
        });
    }

    private void xuLyXoaDanhBa() {
        database.delete("CONTACT", "ma = ?", new String[]{"113"});  // xóa danh bạ có mã số = 113
        showAllContactOnListView();
    }

    private void xuLyChinhSuaDanhBa() {
        ContentValues row = new ContentValues();
        row.put("Ten", "Nguyen Hong Vann");
        database.update("CONTACT", row, "ma = ?", new String[]{"1"});   // Lưu ý: tất cả dữ liệu trong SQLite có thể hiểu thành chuỗi (kể cả ngày tháng năm, số)
        showAllContactOnListView();
    }

    private void xuLyThemDanhBa() {
        ContentValues row = new ContentValues();
        row.put("Ma", 113);
        row.put("Ten", "Nguyễn Hồng Thanh");
        row.put("Phone", "0983765515");
        long r = database.insert("CONTACT", null, row); // r: số dòng bị ảnh hưởng
        Toast.makeText(MainActivity.this, "Vừa thêm mới 1 contact, kết quả trạng thái r = " + r, Toast.LENGTH_LONG).show();
        showAllContactOnListView();
    }

    private void addControls() {
        lvDanhBa = (ListView) findViewById(R.id.lvDanhBa);
        dsDanhBa = new ArrayList<>();
        adapterDanhBa = new ArrayAdapter<String>(
                MainActivity.this,
                android.R.layout.simple_list_item_1,
                dsDanhBa
        );
        lvDanhBa.setAdapter(adapterDanhBa);

        btnThemDanhBa = (Button) findViewById(R.id.btnThemDanhBa);
        btnChinhSua = (Button) findViewById(R.id.btnChinhSua);
        btnXoa = (Button) findViewById(R.id.btnXoa);
    }

    private void xuLySaoChepCSDLTuAssetsVaoHeThongMobile() {
        File dbFile = getDatabasePath(DATABASE_NAME);
        if (!dbFile.exists()) {
            try {
                CopyDatabaseFromAssets();
                Toast.makeText(this, "Sao chép CSDL vào hệ thống thành công", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void CopyDatabaseFromAssets() {
        try {
            InputStream myInput;    // myInput là cơ sở dữ liệu chúng ta lấy từ thư mục assets
            myInput = getAssets().open(DATABASE_NAME);  // mở file

            // Path to the just created empty db
            String outFileName = layDuongDanLuuTru();

            // if the path doesn't exists: create it
            File f = new File(getApplicationInfo().dataDir + DB_PATH_SUFFIX);
            if (!f.exists()) {
                f.mkdir();
            }

            // Open the empty db as the output stream
            OutputStream myOutput = new FileOutputStream(outFileName);  // myOutput là nơi mà chúng ta sao chép cơ sở dữ liệu (myInput) vào đó để chúng ta có thể tương tác được với cơ sở dữ liệu

            // Transfer bytes from the input file to the output file (tiến hành sao chép)
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }

            // Close the streams
            myOutput.flush();
            myOutput.close();
            myInput.close();
        } catch (Exception ex) {
            Log.e("Lỗi sao chép", ex.toString());
        }
    }

    // 3
    private String layDuongDanLuuTru() {
        return getApplicationInfo().dataDir + DB_PATH_SUFFIX + DATABASE_NAME;
    }
}
