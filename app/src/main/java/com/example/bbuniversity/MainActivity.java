package com.example.bbuniversity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.bbuniversity.admin_panel.AdminActivity;
import com.example.bbuniversity.etudiant.StudentActivity;
import com.example.bbuniversity.teacher.TeacherActivity;

public class MainActivity extends AppCompatActivity {

    CardView teacher, student, admin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        teacher = findViewById(R.id.teacher);
        student = findViewById(R.id.student);
        admin = findViewById(R.id.admin);


        admin.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminActivity.class);
            startActivity(intent);
        });

        student.setOnClickListener(v-> {
            Intent intent = new Intent(this , StudentActivity.class);
            startActivity(intent);
        });

        teacher.setOnClickListener(v -> {
            Intent intent = new Intent(this, TeacherActivity.class);
            startActivity(intent);
        });

    }
}