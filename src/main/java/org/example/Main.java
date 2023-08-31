package org.example;

import io.reactivex.rxjava3.core.Observable;

import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        Observable<TempInfo> observable = getTemperatures2("NewYork");
        observable.blockingSubscribe(new TempObserver());
    }

    private static Flow.Publisher<TempInfo> getTemperatures(String town) {
        return subscriber -> subscriber.onSubscribe(new TempSubscription(subscriber, town));
    }
    private static Observable<TempInfo> getTemperatures2(String town) {
        return Observable.create(emitter ->
                    Observable.interval(1, TimeUnit.SECONDS)
                            .subscribe(i -> {
                                if (!emitter.isDisposed()) {
                                    if (i >=5) {
                                        emitter.onComplete();
                                    }else {
                                        try {
                                            emitter.onNext(TempInfo.fetch(town));
                                        }catch (Exception e){
                                            emitter.onError(e);
                                        }
                                    }
                                }
                            })
                );
    }

    private static Flow.Publisher<TempInfo> getCelsiusTemperatures(String town) {
        return subscriber -> {
            TempProcessor processor = new TempProcessor();
            processor.subscribe(subscriber);
            subscriber.onSubscribe(new TempSubscription(processor, town));
        };
    }
}