   package com.example.howlstagram.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.howlstagram.R
import com.example.howlstagram.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_detail.view.*
import kotlinx.android.synthetic.main.item_detail.view.*

class DetailViewFragment : Fragment(){
    var uid : String? = null
    var firestore : FirebaseFirestore? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid

        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_detail,container,false)
        view.detailviewfragment_recyclerview.adapter = DetailviewRecyclerViewAdapter()
        view.detailviewfragment_recyclerview.layoutManager = LinearLayoutManager(activity)
        return view
    }
    inner class DetailviewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        var contentUidList : ArrayList<String> = arrayListOf()

        init {
            firestore?.collection("images")?.orderBy("timestamp")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                contentDTOs.clear()
                contentUidList.clear()

                for(snapshot in querySnapshot!!.documents){
                    var item = snapshot.toObject(ContentDTO::class.java)
                    contentDTOs.add(item!!)
                    contentUidList.add(snapshot.id)
                }
                notifyDataSetChanged()
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_detail, parent, false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewholder = (holder as CustomViewHolder).itemView
            viewholder.detailviewitem_profile_text.text = contentDTOs!![position].userId
            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUrl).into(viewholder.detailviewitem_imageview_content)
            viewholder.detailviewitem_explain_textview.text = contentDTOs!![position].explain
            viewholder.detailviewitem_favoritecounter_textview.text = "Likes " + contentDTOs!![position].favoriteCount

            viewholder.detailviewitem_favorite_imageview.setOnClickListener {
                favoriteEvent(position)
            }

            if(contentDTOs!![position].favorite.containsKey(uid)){
                viewholder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite)
            }
            else{
                viewholder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite_border)
            }
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        fun favoriteEvent(position: Int){
            var tsDoc = firestore?.collection("images")?.document(contentUidList[position])
            firestore?.runTransaction { transaction ->

                var contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)
                if (contentDTO!!.favorite.containsKey(uid)){
                    //버튼이 클릭 되면
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount - 1
                    contentDTO?.favorite.remove(uid)
                }
                else{
                    //클릭됮 않았을 때
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount + 1
                    contentDTO?.favorite[uid!!] = true
                }
                transaction.set(tsDoc,contentDTO)
            }
        }
    }
}