package com.savonikaleksandr.scaner;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.savonikaleksandr.scaner.My_tools.Constant;

public class Admin_BaseActivity extends AppCompatActivity {

    //змінні об'ектів інтерфейсу
    private DrawerLayout drawer;
    private Toolbar toolbar;
    private AppBarConfiguration mAppBarConfiguration;
    private TextView adminName, adminEmail;
    private View navHeader;
    private Button but_out;
    //головний метод, який перевантажується кожного разу при створені активності
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Знашодження лойаута вікна
        setContentView(R.layout.activity_admin__base);
        setupUI();//метод для ініціалізації всіх об'єктів
        setupEx();//метод отримання даних з іншої активності

    }
//метод ініціалізаціїї
    private void setupUI() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navHeader = navigationView.getHeaderView(0);
        adminName = (TextView) navHeader.findViewById(R.id.name_admin);
        adminEmail = (TextView) navHeader.findViewById(R.id.target_admin);
        but_out = findViewById(R.id.logout_button);
        //встановлення слухача натискання на кнопку
        but_out.setOnClickListener(new View.OnClickListener() {
            //метод натискання на кнопку
            @Override
            public void onClick(View v) {
                //показ діалогового вікна с попереченнями
                showBeautifulDialog(getString(R.string.masage_exit_title),getString(R.string.mesage_exit_text));
            }
        });
        //налаштування панелі підказки
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        //Налаштування навігації для бокового меню
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);//знаходження елементу на лойаут
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);//під'єднання до навігаціїї
        NavigationUI.setupWithNavController(navigationView, navController);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the main_menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_help, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case R.id.action_help:
                Intent help = new Intent(Admin_BaseActivity.this, HelpActivity.class);
                help.putExtra("id","1");
                startActivity(help);
                finish();
                break;
            case R.id.action_program:

                AlertDialog.Builder dialog = new AlertDialog.Builder(Admin_BaseActivity.this);
                dialog.setTitle("Информация о приложение")
                        .setMessage("QD Scanner - сканер qr-кодов для вывода 3D-моделей \n" +
                                "Версия: 1.0.6.7 \n" +
                                "Разработчик: Савоник Александр");
                dialog.show();

                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }
    //метод отримання екстра даних з іншої активності
    private void setupEx(){
        Bundle extras = getIntent().getExtras(); //отримання пакету
        if (extras != null){ //перевірка на порожність
            //заповнення полей даними с пакету
            adminName.setText(extras.getString(Constant.ADMIN));
            adminEmail.setText(extras.getString(Constant.EMAIL));
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
    //метод виводу діалогового вікна з попередженням
    private void showBeautifulDialog(String title, String description){
        //створення діалогового віна
        final Dialog dialog = new Dialog(this, R.style.df_dialog);
        dialog.setContentView(R.layout.dialog_beautiful); //встановлення розмітки
        //налаштування
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        //знаходження текстових полів й заповнення їх текстом
        ((TextView) dialog.findViewById(R.id.dialog_title)).setText(title);
        ((TextView) dialog.findViewById(R.id.dialog_description)).setText(description);
        //знаходження кнопки й встановлення на нех слухача
        dialog.findViewById(R.id.dialog_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //закриття діалогового ікна
                dialog.dismiss();
                //виклик методу виходу з аккаунту
                Logout();
            }
        });
        //знаходження кнопки й встановлення на нех слухача
        dialog.findViewById(R.id.dialog_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        //показ діалогового вікна
        dialog.show();
    }
//метод виходу з аккаунту адміністора
    private void Logout() {
        FirebaseAuth.getInstance().signOut(); //Отримання екземплеру адміністратору
        //створення новох активності
        Intent intent = new Intent(Admin_BaseActivity.this, User_BaseActivity.class);
        startActivity(intent); //виклик вікна
        finish();
    }
}