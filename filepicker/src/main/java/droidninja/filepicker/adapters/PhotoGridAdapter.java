package droidninja.filepicker.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.ArrayList;

import droidninja.filepicker.FilePickerConst;
import droidninja.filepicker.PickerManager;
import droidninja.filepicker.R;
import droidninja.filepicker.models.Media;
import droidninja.filepicker.utils.AndroidLifecycleUtils;
import droidninja.filepicker.views.SmoothCheckBox;

public class PhotoGridAdapter extends SelectableAdapter<PhotoGridAdapter.PhotoViewHolder, Media>{

  private final Context context;
  private final RequestManager glide;
  private final boolean showCamera;
  private final FileAdapterListener mListener;
  private int imageSize;

  public final static int ITEM_TYPE_CAMERA = 100;
  public final static int ITEM_TYPE_PHOTO  = 101;
  private View.OnClickListener cameraOnClickListener;

  public PhotoGridAdapter(Context context,
                          RequestManager requestManager,
                          ArrayList<Media> medias,
                          ArrayList<String> selectedPaths,
                          boolean showCamera,
                          FileAdapterListener photoGridAdapterListener)
  {
    super(medias, selectedPaths);
    this.context = context;
    this.glide = requestManager;
    this.showCamera = showCamera;
    this.mListener = photoGridAdapterListener;
    setColumnNumber(context,3);
  }

  @Override
  public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View itemView = LayoutInflater.from(context).inflate(R.layout.item_photo_layout, parent, false);

    return new PhotoViewHolder(itemView);
  }

  @Override
  public int getItemViewType(int position) {
    if(showCamera)
      return (position == 0) ? ITEM_TYPE_CAMERA : ITEM_TYPE_PHOTO;
    else
      return ITEM_TYPE_PHOTO;
  }

  @Override
  public void onBindViewHolder(final PhotoViewHolder holder, int position) {
    if(getItemViewType(position) == ITEM_TYPE_PHOTO) {

      final Media media = getItems().get(showCamera?position-1:position);

      if(AndroidLifecycleUtils.canLoadImage(holder.imageView.getContext())) {
        glide.load(new File(media.getPath()))
                .apply(RequestOptions
                        .centerCropTransform()
                        .override(imageSize, imageSize)
                        .placeholder(R.drawable.image_placeholder))
                .thumbnail(0.5f)
                .into(holder.imageView);
      }


      if(media.getMediaType()==FilePickerConst.MEDIA_TYPE_VIDEO)
        holder.videoIcon.setVisibility(View.VISIBLE);
      else
        holder.videoIcon.setVisibility(View.GONE);

      holder.itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          onItemClicked(holder,media);
        }
      });

      //in some cases, it will prevent unwanted situations
      holder.checkBox.setVisibility(View.GONE);
      holder.checkBox.setOnCheckedChangeListener(null);
      holder.checkBox.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          onItemClicked(holder,media);
        }
      });

      //if true, your checkbox will be selected, else unselected
      holder.checkBox.setChecked(isSelected(media));

      holder.selectBg.setVisibility(isSelected(media) ? View.VISIBLE : View.GONE);
      holder.checkBox.setVisibility(isSelected(media) ? View.VISIBLE : View.GONE);

      holder.checkBox.setOnCheckedChangeListener(new SmoothCheckBox.OnCheckedChangeListener() {
        @SuppressLint("StringFormatMatches")
        @Override
        public void onCheckedChanged(SmoothCheckBox checkBox, boolean isChecked) {
          if(media.getFileSize() > (PickerManager.getInstance()
                  .getMaxMediaSize() * 1000000)) {
            AlertDialog.Builder aBuilder = new AlertDialog.Builder(context, R.style.MyDialogTheme);
            aBuilder.setTitle(R.string.app_name);
            aBuilder.setMessage(String.format(context.getString(R.string.max_file_size_alert_dialog), PickerManager.getInstance()
                    .getMaxMediaSize()));
            aBuilder.setPositiveButton(R.string.default_positive_button_text, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
              }
            });
            aBuilder.show();
            return;
          }
          toggleSelection(media);
          holder.selectBg.setVisibility(isChecked ? View.VISIBLE : View.GONE);

          if (isChecked)
          {
              holder.checkBox.setVisibility(View.VISIBLE);
              PickerManager.getInstance().add(media.getPath(), FilePickerConst.FILE_TYPE_MEDIA);
          }
          else
          {
            holder.checkBox.setVisibility(View.GONE);
            PickerManager.getInstance().remove(media.getPath(),FilePickerConst.FILE_TYPE_MEDIA);
          }

          if(mListener!=null)
            mListener.onItemSelected();
        }
      });

    }
    else
    {
      holder.imageView.setImageResource(PickerManager.getInstance().getCameraDrawable());
      holder.checkBox.setVisibility(View.GONE);
      holder.itemView.setOnClickListener(cameraOnClickListener);
      holder.videoIcon.setVisibility(View.GONE);
    }
  }

  private void onItemClicked(PhotoViewHolder holder, Media media) {
    if(PickerManager.getInstance().getMaxCount()==1)
    {
      PickerManager.getInstance().add(media.getPath(), FilePickerConst.FILE_TYPE_MEDIA);
      if(mListener!=null)
        mListener.onItemSelected();
    }
    else if (holder.checkBox.isChecked() || PickerManager.getInstance().shouldAdd()) {
      holder.checkBox.setChecked(!holder.checkBox.isChecked(), true);
    }
  }

  private void setColumnNumber(Context context, int columnNum) {
    WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    DisplayMetrics metrics = new DisplayMetrics();
    wm.getDefaultDisplay().getMetrics(metrics);
    int widthPixels = metrics.widthPixels;
    imageSize = widthPixels / columnNum;
  }

  @Override
  public int getItemCount() {
    if(showCamera)
      return getItems().size()+1;
    return getItems().size();
  }

  public void setCameraListener(View.OnClickListener onClickListener)
  {
    this.cameraOnClickListener = onClickListener;
  }

  public static class PhotoViewHolder extends RecyclerView.ViewHolder {

      SmoothCheckBox checkBox;

      ImageView imageView;

      ImageView videoIcon;

      View selectBg;

    public PhotoViewHolder(View itemView) {
      super(itemView);
      checkBox = itemView.findViewById(R.id.checkbox);
      imageView = itemView.findViewById(R.id.iv_photo);
      videoIcon = itemView.findViewById(R.id.video_icon);
      selectBg = itemView.findViewById(R.id.transparent_bg);
    }
  }
}
