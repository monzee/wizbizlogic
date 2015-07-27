package codeia.ph.wizbizlogic.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.LayoutRes;
import android.view.View;
import android.view.ViewGroup;

import com.yahoo.squidb.data.AbstractModel;
import com.yahoo.squidb.data.SquidCursor;
import com.yahoo.squidb.utility.SquidCursorAdapter;

import java.util.Arrays;

import codeia.ph.wizbizlogic.service.Repeater;
import codeia.ph.wizbizlogic.service.TypedAdapter;
import codeia.ph.wizbizlogic.service.TypedCursor;

public abstract class AdapterFactory<T extends AbstractModel> implements Repeater<T> {
    public enum ItemType {ITEM, DROPDOWN}

    protected abstract @LayoutRes int getLayout(ItemType type);
    protected abstract void prepare(View view, T item);

    public TypedAdapter<T> buildAdapter(Context c, T model) {
        return new Adapter(c, model);
    }

    private class Adapter extends SquidCursorAdapter<T> implements TypedAdapter<T> {

        public Adapter(Context context, T model) {
            super(context, model);
        }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(getLayout(ItemType.ITEM), parent, false);
            }
            prepare(convertView, getItem(pos));
            return convertView;
        }

        @Override
        public View getDropDownView(int pos, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(getLayout(ItemType.DROPDOWN),
                        parent, false);
            }
            prepare(convertView, getItem(pos));
            return convertView;
        }

        @Override
        public void populateWith(TypedCursor<T> cursor) {
            Cursor c = cursor.getCursor();
            if (c instanceof SquidCursor) {
                @SuppressWarnings("unchecked")
                SquidCursor<T> squidCursor = (SquidCursor) c;
                changeCursor(squidCursor);
            } else {
                changeCursor(new SquidCursor<T>(c, Arrays.asList(cursor.getFields())));
            }
        }
    }
}
