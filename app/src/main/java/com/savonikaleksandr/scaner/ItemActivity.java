package com.savonikaleksandr.scaner;

import android.Manifest;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.savinik.engine_model.util.android.AndroidUtils;
import com.savinik.engine_model.util.android.ContentUtils;
import com.savonikaleksandr.scaner.My_tools.Constant;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import me.itangqi.waveloadingview.WaveLoadingView;

public class ItemActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_LOAD_MODEL = 1101;
    private static final int REQUEST_READ_CONTENT_PROVIDER = 1002;
    private   String file_name = "image.png";
    ImageView imageObj, imageQR;
    EditText title, subtitle;
    Button watch_but, changeIm_but, changeObj_but, changeInf_but, dell_but, send_but;
    Uri uriObj, uriImag, uriQr;
    File file;
    String id;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private StorageReference mStorageImag;
    private StorageReference mStorageObj;
    private StorageReference mStorageQR;
    private DatabaseReference mRefData;
    private String rasherenie;
    private StorageReference mStorageObj_Load;
    private StorageReference mStorageQR_Load;
    private static String folderToSave = Environment.getExternalStorageDirectory().toString();
    private Map<String, Object> loadModelParameters = new HashMap<>();

    private View root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        setupUI();
    }

    private void setupUI() {
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mStorageImag = FirebaseStorage.getInstance().getReference("ObjectIm"); //підключення до Storage з зображенями
        mStorageQR = FirebaseStorage.getInstance().getReference("QRCode"); //підключення до Storage з QR-кодами
        mStorageObj = FirebaseStorage.getInstance().getReference("ObjectDB"); //підключення до Storage з 3D-моделями
        mRefData = FirebaseDatabase.getInstance().getReference(user.getUid()); //підключення до Realtime Database

        imageQR = findViewById(R.id.imageQR);
        imageObj = findViewById(R.id.object_image);
        title = findViewById(R.id.title_obj_info) ;
        subtitle = findViewById(R.id.sub_text);

        send_but = findViewById(R.id.send_button);
        watch_but = findViewById(R.id.view_but);
        changeIm_but = findViewById(R.id.but_edit_im);
        changeObj_but = findViewById(R.id.change_object);
        changeInf_but = findViewById(R.id.edit_but);
        dell_but = findViewById(R.id.dell_but);

        watch_but.setOnClickListener(this::onClickWatch);
        changeIm_but.setOnClickListener(this::onClickChangeIm);
        changeObj_but.setOnClickListener(this::onClickChangeObj);
        changeInf_but.setOnClickListener(this::onClickChangeInf);
        send_but.setOnClickListener(this::onClickSend);
        dell_but.setOnClickListener(this::onClickDell);

        root = dell_but.getRootView();

        Intent i = getIntent();
        if (i!=null){
            id = i.getStringExtra(Constant.ID);
            Picasso.get().load(i.getStringExtra(Constant.URLIMAGE)).into(imageObj);
            Picasso.get().load(i.getStringExtra(Constant.URLQR)).into(imageQR);
            uriImag = Uri.parse(i.getStringExtra(Constant.URLIMAGE));
            title.setText(i.getStringExtra(Constant.TITLE));
            subtitle.setText(i.getStringExtra(Constant.SUBTITLE));
            uriObj = Uri.parse(i.getStringExtra(Constant.URLOBJECT));
            uriQr = Uri.parse(i.getStringExtra(Constant.URLQR));

        }


        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) { changeInf_but.setEnabled(true); }
        });
        subtitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) { changeInf_but.setEnabled(true); }
        });
    }

    private void onClickWatch(View view) {
        String name = uriObj.getLastPathSegment();
        int index = name.indexOf(".");

        String rasherenie = name.substring(index);
        try {
            file = File.createTempFile("model_object","."+rasherenie);
        }catch (Exception e){
            e.getMessage();
        }
        mStorageObj_Load = FirebaseStorage.getInstance().getReferenceFromUrl(String.valueOf(uriObj)); //підключення до Storage з 3D-моделями

        WaveLoadingView mloading;

        final Dialog dialog = new Dialog(this, R.style.df_dialog);
        dialog.setContentView(R.layout.dialog_download);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        mloading = (WaveLoadingView) dialog.findViewById(R.id.download_stat);
        mloading.setShapeType(WaveLoadingView.ShapeType.CIRCLE);
        mloading.setAnimDuration(3000);
        mloading.startAnimation();
        mloading.setCenterTitleColor(R.color.white);

        mStorageObj_Load.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
              //  Snackbar.make(watch_but.getRootView(), file.toString(), BaseTransientBottomBar.LENGTH_LONG).show();
                Uri fil = Uri.fromFile(file);
                ContentUtils.setCurrentDir(file.getParentFile());
                dialog.dismiss();
                launchModelRendererActivity(fil);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull FileDownloadTask.TaskSnapshot snapshot) {
                double  progress = (100.0 * snapshot.getBytesTransferred()/snapshot.getTotalByteCount());
             //   Snackbar.make(watch_but.getRootView(), "Load >> " + (int) progress, BaseTransientBottomBar.LENGTH_LONG).show();
                mloading.setProgressValue((int) progress);
                mloading.setCenterTitle(String.valueOf((int)progress)+" %");
            }
        });
        dialog.show();
    }

    private void onClickDell(View view){
        mStorageObj = FirebaseStorage.getInstance().getReferenceFromUrl(String.valueOf(uriObj));
        mStorageImag = FirebaseStorage.getInstance().getReferenceFromUrl(String.valueOf(uriImag));
        mStorageQR = FirebaseStorage.getInstance().getReferenceFromUrl(String.valueOf(uriQr));
        mRefData.child(id).removeValue();
        mStorageObj.delete();
        mStorageImag.delete();
        mStorageQR.delete();
        super.onBackPressed();
        super.finish();
    }
    private void onClickChangeIm(View view){
        //створення активності для вибору зображення
        Intent intImageAdd = new Intent();
        //встановлення ттипу даних які можуть бути використанні
        intImageAdd.setType("image/*");
        intImageAdd.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intImageAdd,1);
    }
    private void onClickChangeObj(View view){
        //перевірка на дозвіл
        if (AndroidUtils.checkPermission(ItemActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_READ_CONTENT_PROVIDER)) {
            loadModelParameters.clear();
            ContentUtils.clearDocumentsProvided();
            ContentUtils.setCurrentDir(null);
            askForFile(REQUEST_CODE_LOAD_MODEL, "*/*");
        }
    }
    private void onClickChangeInf(View view){
        if (TextUtils.isEmpty(title.getText())){//перевірка на порожність
            title.setError("Enter title");//якщо так то виводиться помилка
            return;
        }
        if (TextUtils.isEmpty(subtitle.getText())){//перевірка на порожність
            subtitle.setError("Enter subtitle");//якщо так то виводиться помилка
            return;
        }
        mRefData.child(id).child("title").setValue(String.valueOf(title.getText()));
        mRefData.child(id).child("subtitle").setValue(String.valueOf(subtitle.getText()));
        Snackbar.make(ItemActivity.this, root,"Changes saved", BaseTransientBottomBar.LENGTH_LONG).show();
    }
    private void onClickSend(View view){
        try {
            OutputStream fOut = null;

            //File file = new File(folderToSave, title.getText().toString() +".jpg"); // создать уникальное имя для файла основываясь на дате сохранения
           // fOut = new FileOutputStream(file);
            BitmapDrawable drawable = (BitmapDrawable) imageQR.getDrawable();
            Bitmap bitmap = drawable.getBitmap();
            ContextWrapper cw = new ContextWrapper(this.getApplicationContext());
            File directory = cw.getDir("ImagesDir", Context.MODE_PRIVATE);

            File file = new File(directory, title.getText().toString() +".jpg"); // создать уникальное имя для файла основываясь на дате сохранения
            fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut); // сохранять картинку в jpeg-формате с 85% сжатия.
            fOut.flush();
            fOut.close();
            MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
            Snackbar.make(root,"Изображение сохранено",BaseTransientBottomBar.LENGTH_LONG).show();
        }catch (Exception e){
            e.getMessage();
        }


    }

    private void launchModelRendererActivity(Uri uri) {
        Intent intent = new Intent(getApplicationContext(), ModelActivity.class);
        try {
            URI.create(uri.toString());
            intent.putExtra("uri", uri.toString());

        } catch (Exception e) {
            // info: filesystem url may contain spaces, therefore we re-encode URI
            try {
                intent.putExtra("uri", new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), uri.getQuery(), uri.getFragment()).toString());
            } catch (URISyntaxException ex) {
                Toast.makeText(this, "Error: " + uri.toString(), Toast.LENGTH_LONG).show();
                return;
            }
        }
        intent.putExtra("immersiveMode", "true");

        // content provider case
        if (!loadModelParameters.isEmpty()) {
            intent.putExtra("type", loadModelParameters.get("type").toString());
            intent.putExtra("backgroundColor", "0.25 0.25 0.25 1");
            loadModelParameters.clear();
        }

        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && data!=null && data.getData()!=null){//перевірка прийнятого результату
            if (resultCode == RESULT_OK){
                imageObj.setImageURI(data.getData());//завантаження картинки для показу
                SaveIm();
            }
        }
        if (requestCode == REQUEST_CODE_LOAD_MODEL && data != null && data.getData() != null){//перевірка прийнятого результату
            if (resultCode == RESULT_OK){
                uriObj = data.getData(); //отримання посилання на модель
                String name = uriObj.getLastPathSegment();
                int index = name.indexOf(".");
                if (index!=-1) {
                    rasherenie = name.substring(index);
                }else {
                    rasherenie = "dae";
                }
             //   text_rash.setText("."+rasherenie);
                SaveOBJ();
            }
        }
    }
    private void SaveIm(){
        //конвертування зображеня в масив байтів
        Bitmap bitmap = ((BitmapDrawable) imageObj.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] byteArray = baos.toByteArray();
        final StorageReference mRef = mStorageImag.child(id);
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
                uriImag = task.getResult();
                mRefData.child(id).child("uriObjImag").setValue(String.valueOf(uriImag));
                Snackbar.make(ItemActivity.this, root,"Changes saved", BaseTransientBottomBar.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //виводимо повідомлення про помилку
                Snackbar.make(ItemActivity.this, root,"ERROR, please choice again!", BaseTransientBottomBar.LENGTH_LONG).show();
            }
        });
    }
    private void SaveOBJ(){
        WaveLoadingView mloading;

        final Dialog dialog = new Dialog(ItemActivity.this, R.style.df_dialog);
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

        mUploadTask = mRefObj.putFile(uriObj);//завантажуємо файл
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
                        uriObj = task.getResult();
                        //виклик методу завантаження зображення
                        mRefData.child(id).child("uriObject").setValue(String.valueOf(uriObj)).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Snackbar.make(root,"Changes saved",BaseTransientBottomBar.LENGTH_LONG).show();
                                dialog.dismiss();
                            }
                        });
                    }
                });
        dialog.show();
    }
    //метод для зчитування й повернення посилання на 3д модель
    private void askForFile(int requestCode, String mimeType) {
        Intent target = ContentUtils.createGetContentIntent(mimeType);
        Intent intent = Intent.createChooser(target, "Select file");
        try {
            startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(ItemActivity.this, "Error. Please install a file content provider", Toast.LENGTH_LONG).show();
        }
    }

    public Uri getImageContentUri(File imageFile, Context context) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }
}