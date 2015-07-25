package codeia.ph.wizbizlogic.service;

import android.database.Cursor;

public interface Many<T> extends Cursor {
    T getItem(int position);
}
