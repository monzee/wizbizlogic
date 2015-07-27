package codeia.ph.wizbizlogic.service;

import android.widget.ListAdapter;
import android.widget.SpinnerAdapter;

public interface TypedAdapter<T> extends ListAdapter, SpinnerAdapter {
    T getItem(int position);
    void populateWith(TypedCursor<T> cursor);
}
