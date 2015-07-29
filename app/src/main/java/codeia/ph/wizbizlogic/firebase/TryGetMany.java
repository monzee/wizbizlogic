package codeia.ph.wizbizlogic.firebase;

import android.database.Cursor;
import android.database.MatrixCursor;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.yahoo.squidb.data.TableModel;
import com.yahoo.squidb.sql.Field;

import codeia.ph.wizbizlogic.R;
import codeia.ph.wizbizlogic.service.Result;
import codeia.ph.wizbizlogic.service.TypedCursor;

class TryGetMany<T extends TableModel> implements ValueEventListener, TypedCursor<T> {
    private final Result<TypedCursor<T>, Integer> result = new Result<>();
    private final Field<?>[] fields;
    private final String[] columns;
    private final MatrixCursor data;

    public TryGetMany(Field<?>... fields) {
        this.fields = fields;
        columns = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            columns[i] = fields[i].getName();
        }
        data = new MatrixCursor(columns);
    }

    @Override
    public Cursor getCursor() {
        return data;
    }

    @Override
    public Field<?>[] getFields() {
        return fields;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        for (DataSnapshot current : dataSnapshot.getChildren()) {
            MatrixCursor.RowBuilder row = data.newRow();
            for (String key : columns) {
                if (key.equals("_id")) {
                    long id = DataService.COUNTER.getAndIncrement();
                    DataService.UID_MAP.put(id, current.getKey());
                    row.add(id);
                } else {
                    row.add(current.child(key).getValue());
                }
            }
        }
        result.ok(this);
    }

    @Override
    public void onCancelled(FirebaseError error) {
        DataService.lastError = error;
        result.fail(R.string.error_general);
    }

    public Result<TypedCursor<T>, Integer> start(Query ref) {
        ref.addListenerForSingleValueEvent(this);
        return result;
    }
}
