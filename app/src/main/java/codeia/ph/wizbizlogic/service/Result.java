package codeia.ph.wizbizlogic.service;

import android.os.AsyncTask;

public class Result<Ok, Fail> {
    public interface Consume<T> {
        void apply(T value);
    }

    public interface Produce<T> {
        T apply();
    }

    public interface Convert<From, To> {
        To apply(From value);
    }

    public interface Chain<Ok, NewOk, NewFail> extends Convert<Ok, Result<NewOk, NewFail>> {}

    public static class Ignore<Ok, Fail> extends Result<Ok, Fail> {
        @Override
        public void ok(Ok value) {}

        @Override
        public void fail(Fail value) {}

        @Override
        public Result<Ok, Fail> then(Consume<Ok> cb) {
            return this;
        }

        @Override
        public <NewOk> Result<NewOk, Fail> then(Convert<Ok, NewOk> map) {
            return new Result.Ignore<>();
        }

        @Override
        public <NewOk, NewFail> Result<NewOk, NewFail>
        then(Chain<Ok, NewOk, NewFail> bind, Convert<Fail, NewFail> mapError) {
            return new Result.Ignore<>();
        }

        @Override
        public Result<Ok, Fail> orElse(Consume<Fail> callback) {
            return this;
        }

        @Override
        public <NewFail> Result<Ok, NewFail> orElse(Convert<Fail, NewFail> map) {
            return new Result.Ignore<>();
        }
    }

    public static <Ok, Fail> Result<Ok, Fail> success(Ok value) {
        Result<Ok, Fail> result = new Result<>();
        result.result = value;
        return result;
    }

    public static <Ok, Fail> Result<Ok, Fail> error(Fail value) {
        Result<Ok, Fail> result = new Result<>();
        result.error = value;
        return result;
    }

    public static <Ok> Result<Ok, Throwable> background(final Produce<Ok> task) {
        final Result<Ok, Throwable> result = new Result<>();
        new AsyncTask<Void, Void, Ok>() {
            private Throwable error;
            @Override
            protected Ok doInBackground(Void... voids) {
                try {
                    return task.apply();
                } catch (Throwable e) {
                    error = e;
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Ok ok) {
                if (error != null) {
                    result.fail(error);
                } else {
                    result.ok(ok);
                }
            }
        }.execute();
        return result;
    }

    public static <Ok, Fail> Result<Ok, Fail>
    background(final Produce<Result<Ok, Fail>> task, final Convert<Throwable, Fail> mapError) {
        final Result<Ok, Fail> result = new Result<>();
        new AsyncTask<Void, Void, Result<Ok, Fail>>() {
            private Throwable error;

            @Override
            protected Result<Ok, Fail> doInBackground(Void... voids) {
                try {
                    return task.apply();
                } catch (Throwable e) {
                    error = e;
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Result<Ok, Fail> innerResult) {
                if (error != null) {
                    if (mapError != null) {
                        result.fail(mapError.apply(error));
                    } else {
                        result.fail(null);
                    }
                } else {
                    innerResult.then(new Consume<Ok>() {
                        @Override
                        public void apply(Ok value) {
                            result.ok(value);
                        }
                    }).orElse(new Consume<Fail>() {
                        @Override
                        public void apply(Fail value) {
                            result.fail(value);
                        }
                    });
                }
            }
        }.execute();
        return result;
    }

    public static <T> Convert<T, T> identity() {
        return new Result.Convert<T, T>() {
            @Override
            public T apply(T value) {
                return value;
            }
        };
    }

    private Ok result;
    private Fail error;
    private Consume<Ok> onResult;
    private Consume<Fail> onError;

    public void ok(Ok value) {
        if (result != null || error != null) {
            return;
        }
        if (onResult != null) {
            onResult.apply(value);
        }
        result = value;
        onResult = null;
        onError = null;
    }

    public void fail(Fail value) {
        if (result != null || error != null) {
            return;
        }
        if (onError != null) {
            onError.apply(value);
        }
        error = value;
        onResult = null;
        onError = null;
    }

    public Result<Ok, Fail> then(final Runnable proc) {
        return then(new Consume<Ok>() {
            @Override
            public void apply(Ok value) {
                proc.run();
            }
        });
    }

    public <NewOk> Result<NewOk, Fail> then(final Produce<NewOk> generate) {
        return then(new Convert<Ok, NewOk>() {
            @Override
            public NewOk apply(Ok value) {
                return generate.apply();
            }
        });
    }

    public Result<Ok, Fail> then(final Consume<Ok> callback) {
        if (error != null) {
            return this;
        }
        if (result != null) {
            callback.apply(result);
        } else if (onResult == null) {
            onResult = callback;
        } else {
            final Consume<Ok> oldCallback = onResult;
            onResult = new Consume<Ok>() {
                @Override
                public void apply(Ok value) {
                    oldCallback.apply(value);
                    callback.apply(value);
                }
            };
        }
        return this;
    }

    public <NewOk> Result<NewOk, Fail> then(final Convert<Ok, NewOk> map) {
        if (error != null) {
            return Result.error(error);
        }
        if (result != null) {
            return Result.success(map.apply(result));
        } else {
            final Result<NewOk, Fail> next = new Result<>();
            then(new Consume<Ok>() {
                @Override
                public void apply(Ok value) {
                    next.ok(map.apply(value));
                }
            });
            return next;
        }
    }

    public <NewOk, NewFail> Result<NewOk, NewFail> thenSurely(Chain<Ok, NewOk, NewFail> bind) {
        return then(bind, null);
    }

    public <NewOk> Result<NewOk, Fail> then(Chain<Ok, NewOk, Fail> bind) {
        return then(bind, Result.<Fail>identity());
    }

    public <NewOk, NewFail> Result<NewOk, NewFail>
    then(final Chain<Ok, NewOk, NewFail> bind, Convert<Fail, NewFail> mapError) {
        final Result<NewOk, NewFail> next = new Result<>();
        then(new Consume<Ok>() {
            @Override
            public void apply(Ok value) {
                bind.apply(value).then(new Consume<NewOk>() {
                    @Override
                    public void apply(NewOk value) {
                        next.ok(value);
                    }
                }).orElse(new Consume<NewFail>() {
                    @Override
                    public void apply(NewFail value) {
                        next.fail(value);
                    }
                });
            }
        });
        if (mapError != null) {
            orElse(mapError);
        } else {
            orElse(new Consume<Fail>() {
                @Override
                public void apply(Fail value) {
                    next.fail(null);
                }
            });
        }
        return next;
    }

    public Result<Ok, Fail> orElse(final Runnable proc) {
        return orElse(new Consume<Fail>() {
            @Override
            public void apply(Fail value) {
                proc.run();
            }
        });
    }

    public <NewFail> Result<Ok, NewFail> orElse(final Produce<NewFail> generate) {
        return orElse(new Convert<Fail, NewFail>() {
            @Override
            public NewFail apply(Fail value) {
                return generate.apply();
            }
        });
    }

    public Result<Ok, Fail> orElse(final Consume<Fail> callback) {
        if (result != null) {
            return this;
        }
        if (error != null) {
            callback.apply(error);
        } else if (onError == null) {
            onError = callback;
        } else {
            final Consume<Fail> oldErrback = onError;
            onError = new Consume<Fail>() {
                @Override
                public void apply(Fail value) {
                    oldErrback.apply(value);
                    callback.apply(value);
                }
            };
        }
        return this;
    }

    public <NewFail> Result<Ok, NewFail> orElse(final Convert<Fail, NewFail> map) {
        if (result != null) {
            return Result.success(result);
        }
        if (error != null) {
            return Result.error(map.apply(error));
        } else {
            final Result<Ok, NewFail> next = new Result<>();
            orElse(new Consume<Fail>() {
                @Override
                public void apply(Fail value) {
                    next.fail(map.apply(value));
                }
            });
            return next;
        }
    }

    public <NewOk> Result<NewOk, ?> recover(final Produce<NewOk> generate) {
        return recover(new Convert<Fail, NewOk>() {
            @Override
            public NewOk apply(Fail value) {
                return generate.apply();
            }
        });
    }

    public <NewOk> Result<NewOk, ?> recover(final Convert<Fail, NewOk> map) {
        final Result<NewOk, ?> next = new Result<>();
        orElse(new Consume<Fail>() {
            @Override
            public void apply(Fail value) {
                next.ok(map.apply(value));
            }
        });
        return next;
    }

    public <NewOk, NewFail> Result<NewOk, NewFail>
    recover(final Chain<Fail, NewOk, NewFail> chain) {
        final Result<NewOk, NewFail> next = new Result<>();
        orElse(new Consume<Fail>() {
            @Override
            public void apply(Fail value) {
                chain.apply(value).then(new Consume<NewOk>() {
                    @Override
                    public void apply(NewOk value) {
                        next.ok(value);
                    }
                }).orElse(new Consume<NewFail>() {
                    @Override
                    public void apply(NewFail value) {
                        next.fail(value);
                    }
                });
            }
        });
        return next;
    }
}
