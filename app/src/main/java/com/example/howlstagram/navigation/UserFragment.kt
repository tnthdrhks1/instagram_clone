   package com.example.howlstagram.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.howlstagram.R
import com.example.howlstagram.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_user.view.*
class UserFragment : Fragment(){

    var fragmentview : View? = null
    var firestore : FirebaseFirestore? = null
    var uid : String? = null
    var auth : FirebaseAuth? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentview = LayoutInflater.from(activity).inflate(R.layout.fragment_user,container,false)

        uid = arguments?.getString("destinationUid")
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        println("******************************************************")
        fragmentview?.account_recyclerview?.adapter = UserFragmentRecyclerViewAdapter()
        fragmentview?.account_recyclerview?.layoutManager = GridLayoutManager(activity!!,3)

        return fragmentview
    }
    inner class UserFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()

        init{
            firestore?.collection("images")?.whereEqualTo("uid",uid)?.addSnapshotListener { QuerySnapshot, FirebaseFirestoreException ->

                if(QuerySnapshot == null) return@addSnapshotListener
                for(snapshot in QuerySnapshot.documents){
                    contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                }
                println("******************************************************")
                println(contentDTOs.size)

                fragmentview?.account_tv_post_count?.text = contentDTOs.size.toString()
                notifyDataSetChanged()
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var width = resources.displayMetrics.widthPixels / 3
            var imageView = ImageView(parent.context)
            imageView.layoutParams = LinearLayoutCompat.LayoutParams(width, width)
            return CustomViewHolder(imageView)
        }

        inner class CustomViewHolder(var imageView: ImageView) : RecyclerView.ViewHolder(imageView)

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var imageview = (holder as CustomViewHolder).imageView
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl).apply(RequestOptions().centerCrop()).into(imageview)
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }
    }
}