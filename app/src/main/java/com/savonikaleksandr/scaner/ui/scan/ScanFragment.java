package com.savonikaleksandr.scaner.ui.scan;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.ResultPoint;
import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.savinik.engine_model.util.android.ContentUtils;
import com.savonikaleksandr.scaner.ModelActivity;
import com.savonikaleksandr.scaner.My_tools.ItemSave;
import com.savonikaleksandr.scaner.R;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.itangqi.waveloadingview.WaveLoadingView;


public class ScanFragment extends Fragment implements BarcodeCallback {
    private DecoratedBarcodeView decoratedBarcodeView;
    private Map<String, Object> loadModelParameters = new HashMap<>();

    private StorageReference mStorageOBJ;
    File file;

    private DatabaseReference mRefData;
    View root;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_scaner_object, container, false);
        mRefData = FirebaseDatabase.getInstance().getReference();
        mStorageOBJ = FirebaseStorage.getInstance().getReference("ObjectDB");
        return root;
    }
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Инициализация IntentIntegrator для указания типа сканирования (QR_CODE)
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);

        //Инициализация DecoratedBarcodeView для работы со сканнером QR кодов
        decoratedBarcodeView = view.findViewById(R.id.decorated_barcode_view);
        decoratedBarcodeView.initializeFromIntent(integrator.createScanIntent());
        decoratedBarcodeView.decodeContinuous(this);
    }

    @Override
    public void barcodeResult(BarcodeResult result) {

        if (result.getText()!=null){
            decoratedBarcodeView.pause();
            // Приостанавливаем DecoratedBarcodeView
            if (Patterns.WEB_URL.matcher(result.getText()).matches()){
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(result.getText().toString()));
                startActivity(intent);
                return;
            }else {
                decoratedBarcodeView.pause();
                int index = result.toString().indexOf("/");
                String admin_id = result.toString().substring(0, index);
                String id_item = result.toString().substring(index + 1);
                mRefData.child(admin_id).child(id_item).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (task.isSuccessful()) {
                            ItemSave itemSave = task.getResult().getValue(ItemSave.class);
                            showModel(itemSave);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            }
        }
    }

    private void showModel(ItemSave itemSave) {
        Uri uriObj = Uri.parse(itemSave.getUriObject());
        String name = uriObj.getLastPathSegment();
        int index = name.indexOf(".");

        String rasherenie = name.substring(index);
        try {
            file = File.createTempFile("model_object","."+rasherenie);
        }catch (Exception e){
            e.getMessage();
        }
        mStorageOBJ = FirebaseStorage.getInstance().getReferenceFromUrl(String.valueOf(uriObj)); //підключення до Storage з 3D-моделями
        WaveLoadingView mloading;

        final Dialog dialog = new Dialog(getContext(), R.style.df_dialog);
        dialog.setContentView(R.layout.dialog_download);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        mloading = (WaveLoadingView) dialog.findViewById(R.id.download_stat);
        mloading.setShapeType(WaveLoadingView.ShapeType.CIRCLE);
        mloading.setAnimDuration(3000);
        mloading.startAnimation();
        mloading.setCenterTitleColor(R.color.white);

        mStorageOBJ.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
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

    @Override
    public void possibleResultPoints(List<ResultPoint> resultPoints) {

    }
    @Override
    public void onPause() {
        //При вызове метода, приостанавливаем DecoratedBarcodeView
        decoratedBarcodeView.pause();
        super.onPause();
    }

    @Override
    public void onResume() {
        //При вызове метода, возобновляем DecoratedBarcodeView
        decoratedBarcodeView.resume();
        super.onResume();
    }
    private void launchModelRendererActivity(Uri uri) {
        Intent intent = new Intent(getActivity(), ModelActivity.class);
        try {
            URI.create(uri.toString());
            intent.putExtra("uri", uri.toString());

        } catch (Exception e) {
            // info: filesystem url may contain spaces, therefore we re-encode URI
            try {
                intent.putExtra("uri", new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), uri.getQuery(), uri.getFragment()).toString());
            } catch (URISyntaxException ex) {
                Toast.makeText(getActivity(), "Error: " + uri.toString(), Toast.LENGTH_LONG).show();
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

}