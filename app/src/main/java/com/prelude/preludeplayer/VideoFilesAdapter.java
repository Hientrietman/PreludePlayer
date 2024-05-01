package com.prelude.preludeplayer;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

public class VideoFilesAdapter extends RecyclerView.Adapter<VideoFilesAdapter.ViewHolder> {
    private ArrayList<MediaFiles> videoList;
    private Context context;

    public VideoFilesAdapter(ArrayList<MediaFiles> videoList, Context context) {
        this.videoList = videoList;
        this.context = context;
    }

    @NonNull
    @Override
    public VideoFilesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.video_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoFilesAdapter.ViewHolder holder, int position) {
        holder.videoName.setText(videoList.get(position).getDisplayName());
        String size = videoList.get(position).getSize();
        holder.videoSize.setText(android.text.format.Formatter.formatFileSize(context,
                Long.parseLong(size)));
        double milliSeconds = Double.parseDouble(videoList.get(position).getDuration());
        holder.videoDuration.setText(timeConversion((long) milliSeconds));
        Glide.with(context).load(new File(videoList.get(position).getPath()))
                .into(holder.thumbnail);
        holder.menu_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"menu more",Toast.LENGTH_LONG).show();
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, VideoPlayerActivity.class);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail,menu_more;
        TextView videoName,videoSize,videoDuration;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            menu_more = itemView.findViewById(R.id.video_menu_more);
            videoName = itemView.findViewById(R.id.video_name);
            videoSize = itemView.findViewById(R.id.video_size);
            videoDuration = itemView.findViewById(R.id.video_duration);
        }
    }
    public String timeConversion(Long value){
     String videoTime;
     int duration = Math.toIntExact(value);
     int hrs =(duration/3600000);
     int mns =(duration/60000)%60000;
     int scs =duration%60000/1000;
     if(hrs>0){
         videoTime=String.format("%02d:%02d:%02d",hrs,mns,scs);

     }else {
         videoTime=String.format("%02d:%02d",mns,scs);
     }
        return videoTime;
    }
}
