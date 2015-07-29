package codeia.ph.wizbizlogic.service;

import android.database.DataSetObserver;
import android.widget.ListAdapter;
import android.widget.SpinnerAdapter;

public interface TypedAdapter<T> extends ListAdapter, SpinnerAdapter {
    T getItem(int position);
    void populateWith(TypedCursor<T> cursor);
    void notifyDataSetChanged();
    void notifyDataSetInvalidated();
    void registerDataSetObserver(DataSetObserver observer);
    void unregisterDataSetObserver(DataSetObserver observer);
}
