package codeia.ph.wizbizlogic.service;

import android.content.Context;

public interface Repeater<T> {
    TypedAdapter<T> buildAdapter(Context c, T model);
}
