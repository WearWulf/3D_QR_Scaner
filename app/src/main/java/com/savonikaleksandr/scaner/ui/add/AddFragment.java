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

    //?????????????????? ?????????????????? ????????????
    private Map<String, Object> loadModelParameters = new HashMap<>();
    private static final int REQUEST_CODE_LOAD_MODEL = 1101;
    private static final int REQUEST_READ_CONTENT_PROVIDER = 1002;
    private Uri UriFile;
    private Uri UriImag;

    //?????????????????? ???????????? ????'?????????? ????????????????????
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
        setupUi(); //???????????? ???????????? ??????????????????????????
        return root;
    }
//?????????? ??????????????????????????
    private void setupUi() {

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mStorageImag = FirebaseStorage.getInstance().getReference("ObjectIm"); //?????????????????????? ???? Storage ?? ??????????????????????
        mStorageQR = FirebaseStorage.getInstance().getReference("QRCode"); //?????????????????????? ???? Storage ?? QR-????????????
        mStorageObj = FirebaseStorage.getInstance().getReference("ObjectDB"); //?????????????????????? ???? Storage ?? 3D-????????????????
        mRefData = FirebaseDatabase.getInstance().getReference(user.getUid()); //?????????????????????? ???? Realtime Database
        if (user.getUid()==null){
            Snackbar.make(root,"No user",BaseTransientBottomBar.LENGTH_SHORT).show();
        }
        //?????????????????????? ????'?????????? ????????????????????
        add_img = root.findViewById(R.id.add_obj_but);
        add_obj = root.findViewById(R.id.but_add_obj);
        but_suces = root.findViewById(R.id.but_sucses);
        image = root.findViewById(R.id.imageView2);
        ico_obj = root.findViewById(R.id.object_ico);
        title = root.findViewById(R.id.title_obj);
        subtitle = root.findViewById(R.id.Subtitle_obj);
        text_rash = root.findViewById(R.id.text_rash);
        //???????????????????????? ???????????????? ????????????????????
        add_img.setOnClickListener(this::onClickAdd);
        add_obj.setOnClickListener(this::loadModelFromContentProvider);
        but_suces.setOnClickListener(this::onClickSucesAdd);
        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_menu_camera));
    }

    //?????????? ?????????????????? ???? ?????????????????????? ??????????
    private void onClickSucesAdd(View view) {
        if (TextUtils.isEmpty(title.getText())){//?????????????????? ???? ????????????????????
            title.setError("Enter title");//???????? ?????? ???? ???????????????????? ??????????????
            return;
        }
        if (TextUtils.isEmpty(subtitle.getText())){//?????????????????? ???? ????????????????????
            subtitle.setError("Enter subtitle");//???????? ?????? ???? ???????????????????? ??????????????
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
        uploadOBG(); //???????????? ???????????? ?????????????????????? ????????????
    }
    //???????????????????????????????? ?????????? ?????? ?????????????????? ???????????????? ???? ??????????????
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && data!=null && data.getData()!=null){//?????????????????? ???????????????????? ????????????????????
            if (resultCode == RESULT_OK){
                UriImag = data.getData();
                image.setImageURI(data.getData());//???????????????????????? ???????????????? ?????? ????????????
            }
        }
        if (requestCode == REQUEST_CODE_LOAD_MODEL && data != null && data.getData() != null){//?????????????????? ???????????????????? ????????????????????
            if (resultCode == RESULT_OK){
                UriFile = data.getData(); //?????????????????? ?????????????????? ???? ????????????
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
    //?????????? ???????????????????????? 3??-????????????
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

        //???????????? ?????????? ???? ????????
        String title_text = title.getText().toString();
        //?????????????????????? ???????????????? ?????? ????????????????????????

        final StorageReference mRefObj = mStorageObj.child(title.getText().toString() + "." + rasherenie);
        UploadTask mUploadTask; //?????????????????? ?????????? ???? ????????????????????????
        String saveUri = user.getUid() + "/" + title_text; //???????????????? ?????????????????? ??????

        mUploadTask = mRefObj.putFile(UriFile);//???????????????????????? ????????
        //?????????????????? ???????????? ???????????????? ????????????????????????
        mUploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        //???????????????????????? ?????????????? ?? ??????????????????????
                        double  progress = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                        mloading.setProgressValue((int) progress);
                        mloading.setCenterTitle(String.valueOf((int)progress)+" %");
            }
        });
        //?????????????????? ?????????? ???? ???????????????????? ?????????????????? ???? 3??-????????????
        Task<Uri> task = mUploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                return mRefObj.getDownloadUrl();
            }
        })//???????????????????????? ?????????????? ????????????????????
                .addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        //???????????????????? ?????????????????? ???? 3??-????????????
                        uploadUriOBJ = task.getResult();
                        //???????????? ???????????? ???????????????????????? ????????????????????
                        saveDataImage(saveUri, title_text);
                        dialog.dismiss();
                    }
                });
        dialog.show();
    }
//?????????? ???????????????????????? ????????????????????
    private void saveDataImage(String saveUri, String title_text) {
        //?????????????????????????? ?????????????????? ?? ?????????? ????????????
        Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] byteArray = baos.toByteArray();
        //?????????????????????? ???????????????? ?????? ????????????????????????
        final StorageReference mRef = mStorageImag.child(title.getText().toString());
        UploadTask upT = mRef.putBytes(byteArray); //???????????????????????? ????????????????????
        //?????????????????? ?????????? ???? ???????????????????? ?????????????????? ???? ????????????????????
        Task<Uri> task = upT.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                return mRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                //???????????????????? ??????????????????
                uploadUrImage = task.getResult();
                //?????????????????????? ?????????? ???????????????????? ?? ????
                QRCode(saveUri, title_text);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //???????????????? ???????????????????????? ?????? ??????????????
                Snackbar.make(getContext(),root,"Not upload image!", BaseTransientBottomBar.LENGTH_LONG).show();
            }
        });
    }

//??????????-???????????????? ???? ????????????, ?????????????????????? ????????????????????
    private void onClickAdd(View v){
        //?????????????????? ???????????????????? ?????? ???????????? ????????????????????
        Intent intImageAdd = new Intent();
        //???????????????????????? ?????????? ?????????? ?????? ???????????? ???????? ????????????????????????
        intImageAdd.setType("image/*");
        intImageAdd.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intImageAdd,1);
    }
//?????????? ???????????????????? ?????????? ?? ????
    private void QRCode(String saveUri, String title_text){
        //?????????????????????? Qr-????????, ?????????????????? ????'??????????:
        //???????????????????? ?? ???????????? ????????????????????  Qr-??????????
        Bitmap bitmap;
        QRGEncoder qrgEncoder;
        //???????????????????????? ????????????????  Qr-????????
        int width = 300;
        int height = 300;
        int smallerDimension = width < height ? width : height;
        smallerDimension = smallerDimension * 3 / 4;
        //??????????????????  Qr-????????
        qrgEncoder = new QRGEncoder(
                saveUri, null,
                QRGContents.Type.TEXT,
                smallerDimension);

        try {
            //????????????????????????????  Qr-???????? ???? ???????????? ????????
            bitmap = qrgEncoder.encodeAsBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
            byte[] byteArray = baos.toByteArray();
            //?????????????????? ?????????????????? ?????? ????????????
            final StorageReference mRef = mStorageQR.child(title.getText().toString());
            //?????????????????? ???????????? ???? ????????????????????  Qr-????????
            UploadTask upT = mRef.putBytes(byteArray);
            //?????????????????? ???????????? ?????? ?????????????????????? ?????????????? ?? ???????????????????? ?????????????????? ???? Qr-??????
            Task<Uri> task = upT.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    return mRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    //?????????? ???????????????????? ???? ???????????? ????????????????????????
                    ItemSave item = new ItemSave(title_text, title_text, subtitle.getText().toString(), uploadUriOBJ.toString(),
                            uploadUrImage.toString(), task.getResult().toString());
                    //?????????? ?????????? ?? ????
                    mRefData.child(title_text).setValue(item).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //?????????????????? ???????????????????????? ?????? ????????????????????
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
                            //?????????????????? ???????????????????????? ?????? ??????????????
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

//???????????????? ?????????????????? ???????????? ?????????????????? 3??-????????????
    private void loadModelFromContentProvider(View view) {
        //?????????????????? ???? ????????????
        if (AndroidUtils.checkPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_READ_CONTENT_PROVIDER)) {
            loadModelParameters.clear();
            ContentUtils.clearDocumentsProvided();
            ContentUtils.setCurrentDir(null);
            askForFile(REQUEST_CODE_LOAD_MODEL, "*/*");
        }
        // check permission starting from android API 23 - Marshmallow
    }
//?????????? ?????? ???????????????????? ?? ???????????????????? ?????????????????? ???? 3?? ????????????
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