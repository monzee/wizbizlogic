package codeia.ph.wizbizlogic.service;

public class Result<Ok, Fail> {
    public interface Consume<T> {
        void apply(T value);
    }

    public interface Convert<From, To> {
        To apply(From value);
    }

    public interface Chain<Ok, NewOk, NewFail> extends Convert<Ok, Result<NewOk, NewFail>> {}

    public Result<Ok, Fail> then(Consume<Ok> callback) {
        return null;
    }

    public <NewOk> Result<NewOk, Fail> map(Convert<Ok, NewOk> map) {
        return null;
    }

    public <NewOk, NewFail> Result<NewOk, NewFail> bind(Chain<Ok, NewOk, NewFail> bind) {
        return null;
    }

    public Result<Ok, Fail> orElse(Consume<Fail> callback) {
        return null;
    }

    public <NewFail> Result<Ok, NewFail> orElseMap(Convert<Fail, NewFail> map) {
        return null;
    }
}
