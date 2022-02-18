package com.savonikaleksandr.scaner;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.savonikaleksandr.scaner.fragments.LoginFragment;
import com.savonikaleksandr.scaner.fragments.ScannerFragment;

public class User_BaseActivity extends AppCompatActivity  implements BottomNavigationView.OnNavigationItemSelectedListener {
    private FragmentManager fragmentManager;
    private static long back_pressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user__base);
        /* Вызываем метод для инициализации компонентов графического интерфейса */
        setupUI();
    }
    private void setupUI() {
        //Инициализация BottomNavigationView для работы с элементами меню
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);

        //Инициализация FragmentManager для работы с фрагментами
        fragmentManager = getSupportFragmentManager();

        //Привязываем изначальный PlacesFragment к FragmentManager
        fragmentManager.beginTransaction().replace(R.id.main_frame_layout, new ScannerFragment()).commit();

        //Обрабатываем переходы в BottomNavigationView с помощью NavigationItemSelectedListener
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_fragment_scanner:
                fragmentManager.beginTransaction().replace(R.id.main_frame_layout, new ScannerFragment()).commit();
                break;
            case R.id.action_fragment_login:
                fragmentManager.beginTransaction().replace(R.id.main_frame_layout, new LoginFragment()).commit();
                break;
        }

        return true;
    }
    @Override
    public void onBackPressed() {
        if (back_pressed + 2000 < System.currentTimeMillis()) {
            Toast.makeText(getApplicationContext(), "Нажмите еще раз для выхода", Toast.LENGTH_SHORT).show();
            back_pressed = System.currentTimeMillis();
        } else if (back_pressed + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
        }
    }
}