package org.example;

import io.reactivex.rxjava3.core.Observable;

import java.util.Arrays;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        Observable<TempInfo> observable = getCelsiusTemperatures3("NewYork", "Seoul", "Chicago");
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

    private static Observable<TempInfo> getCelsiusTemperatures3(String... towns) {
        return Observable.merge(Arrays.stream(towns).map(Main::getCelsiusTemperatures2).collect(Collectors.toList()));
    }

    private static Observable<TempInfo> getCelsiusTemperatures2(String town) {
        return getTemperatures2(town).map(tempInfo -> new TempInfo(tempInfo.getTown(), (tempInfo.getTemp() - 32) * 5 / 9));
    }
}