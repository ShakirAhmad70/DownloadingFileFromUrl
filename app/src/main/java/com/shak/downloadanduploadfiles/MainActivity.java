package com.shak.downloadanduploadfiles;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        AppCompatButton downloadBtn = findViewById(R.id.downloadBtn);

        downloadBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DownloadService.class);
            intent.putExtra(DownloadService.EXTRA_FILE_URL, "https://drive.google.com/uc?export=download&id=1c3qP15yFrJuhdRv1TmdQuqQS3cAFz8bK");
            intent.putExtra(DownloadService.EXTRA_FILE_NAME, "dummy_file.pdf");
            startService(intent);
        });

    }
}