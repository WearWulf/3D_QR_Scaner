package com.savonikaleksandr.scaner.ui.list;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.savonikaleksandr.scaner.ItemActivity;
import com.savonikaleksandr.scaner.My_tools.Constant;
import com.savonikaleksandr.scaner.My_tools.CustomAdapter;
import com.savonikaleksandr.scaner.My_tools.DataModel;
import com.savonikaleksandr.scaner.My_tools.ItemSave;
import com.savonikaleksandr.scaner.R;

import java.util.ArrayList;

public class ListFragment extends Fragment {
    //творення референсів для зчитування даних
    private StorageReference mStorage;
    private DatabaseReference mRefData;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private Uri uploadUri;

    //створення зміних об'єктів інтерфейсу
    private static RecyclerView list;
    private static ArrayList<DataModel> data;
    private RecyclerView.LayoutManager layoutManager;
    CustomAdapter adapter;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_list_object, container, false);
        setupUI(root);//виклик методу ініціалізації
        getDataFromDB();//віклік методу для зчитування з бд
        return root;
    }
//метод ініціалізації
    private void setupUI(View root) {
        //підключення референсів
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance().getReference("ObjectIm");
        mRefData =  FirebaseDatabase.getInstance().getReference(user.getUid());
        //присвоєння списку
        data = new ArrayList<DataModel>();

        //знаходження об'єкту інтерфейсу
        list = root.findViewById(R.id.castom_view);
        //встановлення слухача нтискання на елемент списку
        CustomAdapter.OnItemClickListener itemClickListener = new CustomAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DataModel data, int position) {
                //створення активності для переходу до просмотру всієї інфрммаці
                Intent item = new Intent(getActivity(), ItemActivity.class);
                //додавання екстра даних для передачі
                item.putExtra(Constant.TITLE, data.getTitle());
                item.putExtra(Constant.SUBTITLE, data.getText());
                item.putExtra(Constant.URLIMAGE, data.getImage_obj());
                item.putExtra(Constant.URLOBJECT, data.getObject());
                item.putExtra(Constant.URLQR,data.getImage_qr());
                item.putExtra(Constant.ID,data.getId());
                //виведення активності
                startActivity(item);
            }
        };
        //створення шаблону заповнення списку
        layoutManager = new LinearLayoutManager(getContext());
        //встановлення його до списку
        list.setLayoutManager(layoutManager);
        //ініціалізація адаптеру с даними й слухачем натискання
        adapter = new CustomAdapter(data,getContext(),itemClickListener);
        //встановлення адаптеру
        list.setAdapter(adapter);

    }
    //метод зчитування з БД
    private void getDataFromDB(){
        //встановлюємо слухач на оноленя даних в середені БД
        mRefData.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //перевірка на заповненість списку
                if (data.size() > 0) data.clear(); //як що так то очистка
                //за допомогою forech перебираю всі єлемменти що надійшли
                for (DataSnapshot dataS : snapshot.getChildren()){
                    //створюю й заповнюю клас даними з БД
                    ItemSave itemSave = dataS.getValue(ItemSave.class);
                    //перевірка на порожність
                    assert itemSave != null;
                    //додавання нового елемнту до списку
                    data.add(new DataModel(
                            itemSave.getId(),
                            itemSave.getTitle(),
                            itemSave.getSubtitle(),
                            itemSave.getUriObject(),
                            itemSave.getUriObjImag(),
                            itemSave.getUriQrcode()
                    ));
                    //встановлення слухача на змуну інформаціїї для адаптеру
                    adapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}