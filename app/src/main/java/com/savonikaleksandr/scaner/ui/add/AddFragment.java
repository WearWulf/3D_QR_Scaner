package com.savonikaleksandr.scaner.ui.add;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.WriterException;
import com.savinik.engine_model.util.android.AndroidUtils;
import com.savinik.engine_model.util.android.ContentUtils;
import com.savonikaleksandr.scaner.My_tools.ItemSave;
import com.savonikaleksandr.scaner.R;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import me.itangqi.waveloadingview.WaveLoadingView;

public class AddFragment extends Fragment {

    //СТВОРЕННЯ ПОТРІБНИХ ЗМІНИХ
    private Map<String, Object> loadModelParameters = new HashMap<>();
    private static final int REQUEST_CODE_LOAD_MODEL = 1101;
    private static final int REQUEST_READ_CONTENT_PROVIDER = 1002;
    private Uri UriFile;
    private Uri UriImag;

    //створення зміних об'єктів інтерфейсу
    private TextView text_rash;
    private Button add_img, but_suces, add_obj;
    private ImageView image, ico_obj;
    private EditText title, subtitle;
    private StorageReference mStorageImag;
    private StorageReference mStorageObj;
    private StorageReference mStorageQR;
    private DatabaseReference mRefData;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private Uri uploadUrImage;
    private Uri uploadUriOBJ;
    private View root;
    String rasherenie;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_add_object, container, false);
        setupUi(); //виклик методу ініціалізації
        return root;
    }
//метод ініціалізації
    private void setupUi() {

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mStorageImag = FirebaseStorage.getInstance().getReference("ObjectIm"); //підключення до Storage з зображенями
        mStorageQR = FirebaseStorage.getInstance().getReference("QRCode"); //підключення до Storage з QR-кодами
        mStorageObj = FirebaseStorage.getInstance().getReference("ObjectDB"); //підключення до Storage з 3D-моделями
        mRefData = FirebaseDatabase.getInstance().getReference(user.getUid()); //підключення до Realtime Database
        if (user.getUid()==null){
            Snackbar.make(root,"No user",BaseTransientBottomBar.LENGTH_SHORT).show();
        }
        //знаходження об'єктів інтерфейсу
        add_img = root.findViewById(R.id.add_obj_but);
        add_obj = root.findViewById(R.id.but_add_obj);
        but_suces = root.findViewById(R.id.but_sucses);
        image = root.findViewById(R.id.imageView2);
        ico_obj = root.findViewById(R.id.object_ico);
        title = root.findViewById(R.id.title_obj);
        subtitle = root.findViewById(R.id.Subtitle_obj);
        text_rash = root.findViewById(R.id.text_rash);
        //Встановлення слухачів натискання
        add_img.setOnClickListener(this::onClickAdd);
        add_obj.setOnClickListener(this::loadModelFromContentProvider);
        but_suces.setOnClickListener(this::onClickSucesAdd);
        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_menu_camera));
    }

    //метод перевірки на коректність даних
    private void onClickSucesAdd(View view) {
        if (TextUtils.isEmpty(title.getText())){//перевірка на порожність
            title.setError("Enter title");//якщо так то виводиться помилка
            return;
        }
        if (TextUtils.isEmpty(subtitle.getText())){//перевірка на порожність
            subtitle.setError("Enter subtitle");//якщо так то виводиться помилка
            return;
        }
        if (UriFile==null){
            Snackbar.make(root,"ERROR!, please choice 3D-model!",BaseTransientBottomBar.LENGTH_LONG).show();
            return;
        }
        if (UriImag == null){
            Snackbar.make(root,"ERROR!, please choice image 3D-model!",BaseTransientBottomBar.LENGTH_LONG).show();
            return;
        }
        uploadOBG(); //виклик методу завантаженя моделі
    }
    //перенавантажений метод для отримання посилань на обьекти
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && data!=null && data.getData()!=null){//перевірка прийнятого результату
            if (resultCode == RESULT_OK){
                UriImag = data.getData();
                image.setImageURI(data.getData());//завантаження картинки для показу
            }
        }
        if (requestCode == REQUEST_CODE_LOAD_MODEL && data != null && data.getData() != null){//перевірка прийнятого результату
            if (resultCode == RESULT_OK){
                UriFile = data.getData(); //отримання посилання на модель
                ico_obj.setVisibility(View.VISIBLE);
                String name = UriFile.getLastPathSegment();
                int index = name.indexOf(".");
                if (index!=-1) {
                    rasherenie = name.substring(index);
                }else {
                    rasherenie = "dae";
                }
                text_rash.setText("."+rasherenie);
            }
        }
    }
    //метод завантаження 3Д-моделі
    private void uploadOBG() {
        WaveLoadingView mloading;

        final Dialog dialog = new Dialog(getActivity(), R.style.df_dialog);
        dialog.setContentView(R.layout.dialog_download);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        mloading = (WaveLoadingView) dialog.findViewById(R.id.download_stat);
        mloading.setShapeType(WaveLoadingView.ShapeType.CIRCLE);
        mloading.setAnimDuration(3000);
        mloading.startAnimation();
        mloading.setCenterTitleColor(R.color.white);

        //беремо назву як ключ
        String title_text = title.getText().toString();
        //відкриваємо референс для завантаження

        final StorageReference mRefObj = mStorageObj.child(title.getText().toString() + "." + rasherenie);
        UploadTask mUploadTask; //створюємо запит на завантаження
        String saveUri = user.getUid() + "/" + title_text; //формуємо посилання для

        mUploadTask = mRefObj.putFile(UriFile);//завантажуємо файл
        //створюємо слухач прогрусу завантаження
        mUploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        //відображаемо прогрес в прогресбарі
                        double  progress = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                        mloading.setProgressValue((int) progress);
                        mloading.setCenterTitle(String.valueOf((int)progress)+" %");
            }
        });
        //створюємо запит на повернення посилання на 3д-модель
        Task<Uri> task = mUploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                return mRefObj.getDownloadUrl();
            }
        })//встановлення слухача завершення
                .addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        //повертання посилання на 3Д-модель
                        uploadUriOBJ = task.getResult();
                        //виклик методу завантаження зображення
                        saveDataImage(saveUri, title_text);
                        dialog.dismiss();
                    }
                });
        dialog.show();
    }
//метод завантаження зображення
    private void saveDataImage(String saveUri, String title_text) {
        //конвертування зображеня в масив байтів
        Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] byteArray = baos.toByteArray();
        //відкриваємо референс для завантаження
        final StorageReference mRef = mStorageImag.child(title.getText().toString());
        UploadTask upT = mRef.putBytes(byteArray); //завантажуємо зображення
        //створюємо запит на повернення посилання на зображення
        Task<Uri> task = upT.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                return mRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                //повертаємо посилання
                uploadUrImage = task.getResult();
                //викликаємоо метод зберігання в БД
                QRCode(saveUri, title_text);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //виводимо повідомлення про помилку
                Snackbar.make(getContext(),root,"Not upload image!", BaseTransientBottomBar.LENGTH_LONG).show();
            }
        });
    }

//метод-обробник на кнопку, завантаженн зображення
    private void onClickAdd(View v){
        //створення активності для вибору зображення
        Intent intImageAdd = new Intent();
        //встановлення ттипу даних які можуть бути використанні
        intImageAdd.setType("image/*");
        intImageAdd.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intImageAdd,1);
    }
//метод збереження даних в БД
    private void QRCode(String saveUri, String title_text){
        //генерування Qr-коду, створення об'єктів:
        //зображення і самого генератору  Qr-кодів
        Bitmap bitmap;
        QRGEncoder qrgEncoder;
        //встановлення розміріз  Qr-коду
        int width = 300;
        int height = 300;
        int smallerDimension = width < height ? width : height;
        smallerDimension = smallerDimension * 3 / 4;
        //генерація  Qr-коду
        qrgEncoder = new QRGEncoder(
                saveUri, null,
                QRGContents.Type.TEXT,
                smallerDimension);

        try {
            //кнонфертування  Qr-коду до масиву байт
            bitmap = qrgEncoder.encodeAsBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
            byte[] byteArray = baos.toByteArray();
            //відкриття референсу для запису
            final StorageReference mRef = mStorageQR.child(title.getText().toString());
            //створення запиту на сбереження  Qr-коду
            UploadTask upT = mRef.putBytes(byteArray);
            //створення запиту для відстеження завершя й повернення посилання на Qr-код
            Task<Uri> task = upT.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    return mRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    //запис інформації до классу серіалізації
                    ItemSave item = new ItemSave(title_text, title_text, subtitle.getText().toString(), uploadUriOBJ.toString(),
                            uploadUrImage.toString(), task.getResult().toString());
                    //запис даних у БД
                    mRefData.child(title_text).setValue(item).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //виведення повідомлення про збереження
                            title.setText("");
                            subtitle.setText("");
                            UriFile=null;
                            ico_obj.setVisibility(View.GONE);
                            text_rash.setText("");
                            image.setImageDrawable(getResources().getDrawable(R.drawable.ic_menu_camera));
                            Snackbar.make(getContext(), root, "Save in database", BaseTransientBottomBar.LENGTH_LONG).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //виведення повідомлення про помилку
                            Snackbar.make(getContext(), root, "Don`t save in data", BaseTransientBottomBar.LENGTH_LONG).show();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });

        } catch (WriterException e) {
            Snackbar.make(root,e.toString(),BaseTransientBottomBar.LENGTH_LONG).show();
        }
    }

//Обробник напикання кнопки Додавання 3Д-моделі
    private void loadModelFromContentProvider(View view) {
        //перевірка на дозвіл
        if (AndroidUtils.checkPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_READ_CONTENT_PROVIDER)) {
            loadModelParameters.clear();
            ContentUtils.clearDocumentsProvided();
            ContentUtils.setCurrentDir(null);
            askForFile(REQUEST_CODE_LOAD_MODEL, "*/*");
        }
        // check permission starting from android API 23 - Marshmallow
    }
//метод для зчитування й повернення посилання на 3д модель
    private void askForFile(int requestCode, String mimeType) {
        Intent target = ContentUtils.createGetContentIntent(mimeType);
        Intent intent = Intent.createChooser(target, "Select file");
        try {
            startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), "Error. Please install a file content provider", Toast.LENGTH_LONG).show();
        }
    }
}