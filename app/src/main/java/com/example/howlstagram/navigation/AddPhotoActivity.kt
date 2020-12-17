package com.example.howlstagram.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.howlstagram.R
import com.example.howlstagram.navigation.model.ContentDTO
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_add_photo.*
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {
    var PICK_IMAGE_FROM_ALBUM = 0
    var storage : FirebaseStorage? = null
    var photoUri : Uri? = null
    var auth : FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        //Initiate
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        //open the album

        var photoPickIntent = Intent(Intent.ACTION_PICK)
        photoPickIntent.type = "image/*"
        startActivityForResult(photoPickIntent, PICK_IMAGE_FROM_ALBUM)

        //add image upload event
        addphoto_btn.setOnClickListener {
            contentUpload()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_FROM_ALBUM) {
            //이미지 선택시
            if(resultCode == Activity.RESULT_OK){
                //이미지뷰에 이미지 세팅
                println("DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD")
                println(data?.data)
                photoUri = data?.data
                addphoto_image.setImageURI(data?.data)
            }
            else{
                println("YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY")
                finish()
            }
        }
    }
    fun contentUpload(){
        //Make FileName
        var timestamp = SimpleDateFormat("yyyyMMDD_HHmmss").format(Date())
        var ImageFileName = "Image_" + timestamp + "_.png"

        var storageRef = storage?.reference?.child("images")?.child(ImageFileName)

        //promise
        storageRef?.putFile(photoUri!!)?.continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
            return@continueWithTask storageRef.downloadUrl
        }?.addOnSuccessListener { uri ->
            var contentDTO = ContentDTO()

            contentDTO.imageUrl = uri.toString()
            contentDTO.uid = auth?.currentUser?.uid
            contentDTO.userId = auth?.currentUser?.email
            contentDTO.explain = addphoto_edit_explain.text.toString()
            contentDTO.timestamp = System.currentTimeMillis()

            firestore?.collection("images")?.document()?.set(contentDTO)

            setResult(Activity.RESULT_OK)
            finish()
        }
        //Callback method
//        storageRef?.putFile(photoUri!!)?.addOnSuccessListener {
//            storageRef?.downloadUrl.addOnSuccessListener {uri ->
//                var contentDTO = ContentDTO()
//
//                contentDTO.imageUrl = uri.toString()
//                //insert uid of userid
//                contentDTO.uid = auth?.currentUser?.uid
//                contentDTO.userId = auth?.currentUser?.email
//                contentDTO.explain = addphoto_edit_explain.text.toString()
//                contentDTO.timestamp = System.currentTimeMillis()
//                firestore?.collection("images")?.document()?.set(contentDTO)
//
//                setResult(Activity.RESULT_OK)
//                finish()
//            }
//        }
    }
}