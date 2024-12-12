package com.ec337.facescanpayment.core.notify;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ec337.facescanpayment.MainActivity;
import com.ec337.facescanpayment.R;
import com.ec337.facescanpayment.core.utils.NavigationUtils;

public class NotifyPage extends AppCompatActivity {
    Button btnStore;
    TextView tv;
    ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.app__notify_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnStore = findViewById(R.id.btnStore);
        tv = findViewById(R.id.textView1);
        iv = findViewById(R.id.imageView);

        Intent intent = getIntent();
        String text = intent.getStringExtra("text");
        int imageResId = intent.getIntExtra("imageSrc", -1);

        if (text != null) {
            tv.setText(text);
        }

        if (imageResId != -1) {
            iv.setImageResource(imageResId);
        }

        btnStore.setOnClickListener(v -> NavigationUtils.navigateTo(this, MainActivity.class, true));
    }
}