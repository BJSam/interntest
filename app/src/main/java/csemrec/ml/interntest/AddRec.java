package csemrec.ml.interntest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageActivity;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;
import io.grpc.Context;

public class AddRec extends AppCompatActivity {
private  ImageView imgVpic;
private Uri resultUri;
private FirebaseAuth mAurh;
private FirebaseUser user;
private Uri DownloadURI ;
private FirebaseFirestore db;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                assert result != null;
                 resultUri = result.getUri();
               // startCrop(resultUri);
                imgVpic.setImageURI(resultUri);
                imgVpic.setTag("imgChanged");
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, "Error:"+error, Toast.LENGTH_SHORT).show();
            }
        }

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_rec);
         imgVpic = findViewById(R.id.imgVTakepic);
        final EditText etName = findViewById(R.id.etName);
        final EditText etDesc = findViewById(R.id.etDesc);
        Button btnUpload = findViewById(R.id.btnUpload);
        mAurh =FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAurh.getCurrentUser();
        imgVpic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(AddRec.this);
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nmam = etName.getText().toString();
                String Descc = etDesc.getText().toString();
                if(imgVpic.getTag()!="imgChanged" || nmam.length()==0||Descc.length()==0 ){
                    Toast.makeText(AddRec.this, "All Fields are Mandatory", Toast.LENGTH_SHORT).show();

                }
                else{
                    UploadDataToFirebase(nmam,Descc);
                }
            }
        });
    }

    private void UploadDataToFirebase(String nmam, String descc) {
        final Map<String, Object> Datatata = new HashMap<>();
        Datatata.put("name", nmam);
        Datatata.put("Desc", descc);


        if(resultUri.getPath() != null){

            final SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setTitleText("Uploading");
            pDialog.setContentText("Uploading Image");
            pDialog.setCancelable(false);
            pDialog.show();

            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            final StorageReference riversRef = storageRef.child(user.getUid()).child("imgs").child(Objects.requireNonNull(resultUri.getLastPathSegment()));
            UploadTask  UploadTask = riversRef.putFile(resultUri);
            UploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Toast.makeText(AddRec.this, "Unable to Upload img", Toast.LENGTH_SHORT).show();
                    pDialog.dismiss();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                  riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                      @Override
                      public void onSuccess(Uri uri) {
                          pDialog.setContentText("Image Uploaded, Now saving data");
                          DownloadURI = uri;
                          Datatata.put("Duri",DownloadURI.toString());

                          CollectionReference colRef = db.collection(user.getUid());
                            colRef.add(Datatata).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    pDialog.dismiss();
                                    new SweetAlertDialog(AddRec.this, SweetAlertDialog.SUCCESS_TYPE)
                                            .setTitleText("Success")
                                            .setContentText("Uploaded Successfully")
                                            .show();
                                    Toast.makeText(AddRec.this, "Db Uploaded", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pDialog.dismiss();
                                    new SweetAlertDialog(AddRec.this, SweetAlertDialog.ERROR_TYPE)
                                            .setTitleText("Oops...")
                                            .setContentText("Unable to save data")
                                            .show();
                                    Toast.makeText(AddRec.this, "data not Saved"+ e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                      }
                  });

                }
            });
        }
    }
}
