package codeia.ph.wizbizlogic.firebase;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.yahoo.squidb.data.TableModel;

import codeia.ph.wizbizlogic.R;
import codeia.ph.wizbizlogic.service.Result;

class TryGetOne<T extends TableModel> implements ValueEventListener {
    private final Result<T, Integer> result = new Result<>();
    private Class<T> modelClass;

    public TryGetOne(Class<T> modelClass) {
        this.modelClass = modelClass;
    }

    @Override
    public void onDataChange(DataSnapshot snapshot) {
        T model = snapshot.getValue(modelClass);
        if (model == null) {
            result.fail(R.string.error_not_found);
        } else {
            model.setId(DataService.getId(snapshot.getKey()));
            result.ok(model);
        }
    }

    @Override
    public void onCancelled(FirebaseError error) {
        DataService.lastError = error;
        result.fail(R.string.error_cancelled);
    }

    public Result<T, Integer> start(Firebase ref) {
        ref.addListenerForSingleValueEvent(this);
        return result;
    }
}
