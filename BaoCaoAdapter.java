package com.example.socialnetwork.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialnetwork.Model.Report;
import com.example.socialnetwork.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class BaoCaoAdapter extends RecyclerView.Adapter<BaoCaoAdapter.ViewHolder> {

    private Context mContext;
    private List<Report> mReport;


    private String saveCurrentData, saveCurrentTime, postName;
    FirebaseUser firebaseUser;
    private DatabaseReference BaoCaoRef;
    FirebaseAuth mAuth;

    public BaoCaoAdapter(Context mContext, List<Report> mReport) {
        this.mContext = mContext;
        this.mReport = mReport;
    }



    @NonNull
    @Override
    public BaoCaoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(mContext).inflate(R.layout.item_baocao,parent,false);
        return new BaoCaoAdapter.ViewHolder(mView);
    }


    @Override
    public void onBindViewHolder(@NonNull BaoCaoAdapter.ViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        Report report = mReport.get(position);
        //khai báo lớp string cho nó
        String uid = mReport.get(position).getUid();
        String postid = mReport.get(position).getPostid();
        String date = mReport.get(position).getDate();
        String time = mReport.get(position).getTime();
        String reason = mReport.get(position).getReason();


    }

    @Override
    public int getItemCount() {
        return mReport.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView pickcherTV,post_image;
        TextView txt_xetbaocao,txt_baocao,txt_post_time,txt_post_date,txt_quocgia,txt_username;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            pickcherTV = itemView.findViewById(R.id.pickcherTV);
            post_image = itemView.findViewById(R.id.post_image);
            txt_xetbaocao = itemView.findViewById(R.id.txt_xetbaocao);
            txt_baocao = itemView.findViewById(R.id.txt_baocao);
            txt_post_time = itemView.findViewById(R.id.txt_post_time);
            txt_post_date = itemView.findViewById(R.id.txt_post_date);
            txt_quocgia = itemView.findViewById(R.id.txt_quocgia);
            txt_username = itemView.findViewById(R.id.txt_username);

        }
    }
}
