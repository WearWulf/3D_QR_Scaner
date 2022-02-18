package com.savonikaleksandr.scaner;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.ViewCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.savinik.engine_model.util.android.AndroidURLStreamHandlerFactory;
import com.savonikaleksandr.scaner.My_tools.Constant;

import java.net.URL;

public class Login extends AppCompatActivity {
    //ініціалізація файлу с первинними наалаштуваннями
    SharedPreferences mSettings;
    //створення едіторудля запису й считування з файлу
    SharedPreferences.Editor editor;
    static {
        System.setProperty("java.protocol.handler.pkgs", "com.savinik.engine_model.util.android");
        URL.setURLStreamHandlerFactory(new AndroidURLStreamHandlerFactory());
    }

    View view;
    private static long back_pressed;

    private FirebaseAuth mAuth; //створення об'єкту аунтифікації
    private FirebaseUser user; //створення об'єкта користувача
    //Створення змінних лб'єктів інтерфейсу
    private EditText login, pass;
    private TextView logInBut_AdminSucces, log_admin, log_user, back_login_choise;
    private ConstraintLayout container_button_login, container_admin_login;
    //Головний методт який спрацьовує при створенні вікна

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setupUI(); //виклик методу ініціалізації
        //перевизначаемо файл налаштуань
        mSettings = getSharedPreferences("start", Context.MODE_PRIVATE);
        //відкриваємо файл налаштування для редагування
        editor = mSettings.edit();
    }
    //головний метод що спрацьовує при запуску вікна
    @Override
    protected void onStart() {
        super.onStart();
        if (mSettings.contains("VIEW")) { //переівркана найвність
            int view1 = mSettings.getInt("VIEW", 0);//зчитуання з файлу налаштувань
            user = mAuth.getCurrentUser();//отримання авторизованого користувача

            if (user != null) { //перевірка на порожність
                //виведенння пвімлення
                Snackbar.make(Login.this, view, "User not null", BaseTransientBottomBar.LENGTH_LONG).show();
                int index = user.getEmail().toString().indexOf("@");//пошук номеру символу
                String admin = user.getEmail().toString().substring(0, index); //обрізання рядка для отримання
                //створення переходу на нову активність
                Intent intent = new Intent(Login.this, Admin_BaseActivity.class);
                //додавання ектра даних для передачі на активність що запантажуемо
                intent.putExtra(Constant.ADMIN, admin);
                intent.putExtra(Constant.EMAIL, user.getEmail().toString());
                intent.putExtra(Constant.UID, user.getUid());
                //запуск вікна
                startActivity(intent);
                finish();
            } else {
                //Запуск активності в резим адміністратора
                Intent intent = new Intent(Login.this, User_BaseActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }
    //мметод ініціалізації
    private void setupUI() {

        //Знахоження елементів інтерфейсу
        mAuth = FirebaseAuth.getInstance();
        login = (EditText) findViewById(R.id.login_log);
        pass = (EditText) findViewById(R.id.password_log);
        container_button_login = (ConstraintLayout) findViewById(R.id.authorization_container_choise);
        container_admin_login = (ConstraintLayout) findViewById(R.id.authorization_container_sign_in);
        view = findViewById(R.id.authorization_button).getRootView();
        logInBut_AdminSucces = (TextView) findViewById(R.id.authorization_button);
        log_admin = ( TextView) findViewById(R.id.authorizathion_button_admin);
        log_user = (TextView) findViewById(R.id.authorizathion_button_user);
        back_login_choise = (TextView) findViewById(R.id.authorization_button_back_choise);

        //налаштування анімації
        ViewCompat.animate(container_admin_login).translationY(+1500).setDuration(1)
                .setInterpolator(new DecelerateInterpolator(1.5f)).start();
        //встановлення слухачів натискання на кнопку
        logInBut_AdminSucces.setOnClickListener(this::LoginInAcc);
        log_admin.setOnClickListener(this::AdminLogin);
        log_user.setOnClickListener(this::UserLogin);
        back_login_choise.setOnClickListener(this::BackToChoise);
        /* Пытаемся запросить у пользователя необходимые разрешения */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                    || checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 0);
            }
        }
    }
    // Метод анімаціїї виїзду текстових полей для вводу логіну та пароля
    private void BackToChoise(View view) {
        ViewCompat.animate(container_admin_login).translationY(1500).setDuration(1500)
                .setInterpolator(new DecelerateInterpolator(1.5f)).start();
        ViewCompat.animate(container_button_login).translationY(0).setStartDelay(400).setDuration(1500)
                .setInterpolator(new DecelerateInterpolator(1.5f)).start();
    }

    // Метод анімаціїї виїзду текстових полей для вводу логіну та пароля

    private void AdminLogin(View view) {
        ViewCompat.animate(container_button_login).translationY(1500).setDuration(1500)
                .setInterpolator(new DecelerateInterpolator(1.5f)).start();
        ViewCompat.animate(container_admin_login).translationY(0).setStartDelay(400).setDuration(1500)
                .setInterpolator(new DecelerateInterpolator(1.5f)).start();
    }
    //метод для встановлення режиму користувача
    private void UserLogin(View view) {
        editor.putInt("VIEW",1);//запис до файлу налаштувань
        editor.apply();//збереження
        //створення активності для режиму корис
        Intent intent = new Intent(Login.this, User_BaseActivity.class);
        //запуск активності
        startActivity(intent);
        finish();
    }
    //иетод входу до акаунту
    private void LoginInAcc(View view) {

        String email = login.getText().toString(); //зчитування з поля
        String password = pass.getText().toString(); //зчитування з поля
        if (TextUtils.isEmpty(email)){//перевірка  на порожність
            login.setError(getResources().getString(R.string.error_email));//якшо так тоді виведення попередження про помилку
            return;
        }else if (TextUtils.isEmpty(password)){//перевірка  на порожність
            pass.setError(getResources().getString(R.string.error_pass)); //якшо так тоді виведення попередження про помилку
            return;
        }
        //виведенн повідомлення по вход
        Snackbar.make(Login.this, view,getString(R.string.snack_succes), BaseTransientBottomBar.LENGTH_LONG).show();
        //авторизація за допомогою логіну та паролю, встановлення слушачів успіху та помилки
        mAuth.signInWithEmailAndPassword(email,password).addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                editor.putInt("VIEW",1);
                editor.apply();
                int index = email.indexOf("@");
                String admin = email.substring(0,index);
                Intent intent = new Intent(Login.this, Admin_BaseActivity.class);
                intent.putExtra("admin", admin);
                intent.putExtra("email",email);
                startActivity(intent);
                finish();
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar.make(Login.this, view,getString(R.string.snack_error), BaseTransientBottomBar.LENGTH_LONG).show();
            }
        });
    }
    //перевантажений метод для налаштування виходу з прогрпми
    @Override
    public void onBackPressed() {
        if (back_pressed + 2000 < System.currentTimeMillis()) {
            Toast.makeText(getApplicationContext(), "Нажмите еще раз для выхода", Toast.LENGTH_SHORT).show();
            back_pressed = System.currentTimeMillis();
            ViewCompat.animate(container_admin_login).translationY(1500).setDuration(1500)
                    .setInterpolator(new DecelerateInterpolator(1.5f)).start();
            ViewCompat.animate(container_button_login).translationY(0).setStartDelay(400).setDuration(1500)
                    .setInterpolator(new DecelerateInterpolator(1.5f)).start();
        } else if (back_pressed + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
        }
    }
}