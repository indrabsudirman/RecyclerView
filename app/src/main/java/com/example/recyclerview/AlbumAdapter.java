package com.example.recyclerview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recyclerview.databinding.DetailItemBinding;

import java.util.ArrayList;
import java.util.List;


public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {
    private final ArrayList<Album> albumList;
    private ItemClickListener itemClickListener;

    //Constructor
    public AlbumAdapter(ArrayList<Album> albumList, ItemClickListener itemClickListener) {
        this.albumList = albumList;
        this.itemClickListener = itemClickListener;
    }

    /**
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     * <p>
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     * <p>
     * The new ViewHolder will be used to display items of the adapter using
     * {@link #onBindViewHolder(ViewHolder, int, List)}. Since it will be re-used to display
     * different items in the data set, it is a good idea to cache references to sub views of
     * the View to avoid unnecessary {@link View#findViewById(int)} calls.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(ViewHolder, int)
     */
    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AlbumViewHolder(DetailItemBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent, false), itemClickListener);
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link ViewHolder#itemView} to reflect the item at the given
     * position.
     * <p>
     * Note that unlike {@link ListView}, RecyclerView will not call this method
     * again if the position of the item changes in the data set unless the item itself is
     * invalidated or the new position cannot be determined. For this reason, you should only
     * use the <code>position</code> parameter while acquiring the related data item inside
     * this method and should not keep a copy of it. If you need the position of an item later
     * on (e.g. in a click listener), use {@link ViewHolder#getAdapterPosition()} which will
     * have the updated adapter position.
     * <p>
     * Override {@link #onBindViewHolder(ViewHolder, int, List)} instead if Adapter can
     * handle efficient partial bind.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        final Album album = albumList.get(position);
        //Setting view
        //Resources can set from JSON Response
        holder.detailItemBinding.image.setImageResource(album.getImageAlbum());
        holder.detailItemBinding.imageTitle.setText(album.getTitleAlbum());
        holder.detailItemBinding.imageDesc.setText(album.getDescAlbum());
        holder.detailItemBinding.duration.setText(album.getDuration());
        holder.detailItemBinding.imageDownload.setImageResource(album.getAlbumDownload());

    }

    /**
     * Return the stable ID for the item at <code>position</code>. If {@link #hasStableIds()}
     * would return false this method should return {@link #NO_ID}. The default implementation
     * of this method returns {@link #NO_ID}.
     *
     * @param position Adapter position to query
     * @return the stable ID of the item at position
     */
    @Override
    public long getItemId(int position) {
        Album album = albumList.get(position);
        // return in real stable id from here
        return album.getAlbumId();
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return (null != albumList ? albumList.size() : 0);
    }


    public interface ItemClickListener {
        void onItemClick(int position);
    }

    public class AlbumViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final DetailItemBinding detailItemBinding;
        ItemClickListener itemClickListener;

        public AlbumViewHolder(DetailItemBinding detailItemBinding, ItemClickListener itemClickListener) {
            super(detailItemBinding.getRoot());
            this.detailItemBinding = detailItemBinding;
            this.itemClickListener = itemClickListener;

            //Set view that implement click
            detailItemBinding.imageDownload.setOnClickListener(this);
            detailItemBinding.imageDesc.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            itemClickListener.onItemClick(getAdapterPosition());
        }
    }

}
