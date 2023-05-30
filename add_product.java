package com.example.diplom;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

public class add_product extends AppCompatActivity {
    private ImageView productImage;
    private String  productName, tagName, moneyName, categoryName, saveCurrentDate, saveCurrentTime, ProductRandomKey;
    private String downloadImageUrl;
    private EditText category;
    private EditText name;
    private EditText tag;
    private EditText money;
    private Button addProduct;
    private static final int galleryPick = 1;
    private Uri ImageUri;
    private StorageReference ProductImageRef;
    private DatabaseReference ProductsRef;
    private ProgressDialog progressDialog;


    public add_product() {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        init();

        productImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });
        addProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ValidateProductData();
            }
        });

    }

    private void ValidateProductData() {
        productName = name.getText().toString();
        tagName = tag.getText().toString();
        moneyName = money.getText().toString();
        categoryName = category.getText().toString();


        if(ImageUri == null){
            Toast.makeText(this, "Добавьте изображение", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(productName)){
            Toast.makeText(this, "Добавьте название", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(tagName)){
            Toast.makeText(this, "Введите номер кузова", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty((CharSequence) category)){
            Toast.makeText(this, "Введите номер кузова", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(moneyName)){
            Toast.makeText(this, "Введите стоимость", Toast.LENGTH_SHORT).show();
        }
        else {
            StoreProductInformation();
        }
    }

    private void StoreProductInformation() {
        progressDialog.setTitle("Пожалуйста подождите...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("dd:MM:yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        saveCurrentTime = currentTime.format(calendar.getTime());

        ProductRandomKey = saveCurrentDate + saveCurrentTime;

        final StorageReference filePath = ProductImageRef.child(ImageUri.getLastPathSegment() + ProductRandomKey + ".jpg");

        final UploadTask uploadTask = filePath.putFile(ImageUri);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String message = e.toString();
                Toast.makeText(add_product.this, "Ошибка" + message, Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(add_product.this, "Фото загружено!", Toast.LENGTH_SHORT).show();

                uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if(!task.isSuccessful()){
                            throw Objects.requireNonNull(task.getException());
                        }

                        downloadImageUrl = filePath.getDownloadUrl().toString();
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task){
                        Toast.makeText(add_product.this, "Фото сохранено", Toast.LENGTH_SHORT).show();


                        SaveProductInformationToDatabase();
                    }
                });
            }
        });

    }

    private void SaveProductInformationToDatabase() {
        HashMap<String, Object> productMap = new HashMap<>();
        productMap.put("pid", ProductRandomKey);
        productMap.put("date", saveCurrentDate);
        productMap.put("time", saveCurrentTime);
        productMap.put("name", productName);
        productMap.put("tag", tagName);
        productMap.put("money", moneyName);
        productMap.put("image", downloadImageUrl);

        ProductsRef.child(ProductRandomKey).updateChildren(productMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    progressDialog.dismiss();
                    Toast.makeText(add_product.this, "Добавлено", Toast.LENGTH_SHORT).show();
                    Intent sucIntent = new Intent(add_product.this, sellerActivity.class);
                    startActivity(sucIntent);


                }
                else{
                    String message = task.getException().toString();
                    Toast.makeText(add_product.this, "Ошибка:" + message, Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();

                }

            }
        });
    }

    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,galleryPick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == galleryPick && resultCode == RESULT_OK && data != null){
            ImageUri = data.getData();
            productImage.setImageURI(ImageUri);

        }
    }

    private void init(){
        productImage = findViewById(R.id.photo);
        name = findViewById(R.id.name);
        category = findViewById(R.id.category);
        tag = findViewById(R.id.tag);
        money = findViewById(R.id.money);
        addProduct = findViewById(R.id.addproduct);
        ProductImageRef = FirebaseStorage.getInstance().getReference().child("Product Images");
        ProductsRef = FirebaseDatabase.getInstance().getReference().child("Products");
        progressDialog = new ProgressDialog( this);


    }
}