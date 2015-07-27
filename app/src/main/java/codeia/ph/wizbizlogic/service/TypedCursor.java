package codeia.ph.wizbizlogic.service;

import android.database.Cursor;

import com.yahoo.squidb.sql.Field;

public interface TypedCursor<T> {
    Cursor getCursor();
    Field<?>[] getFields();
}
