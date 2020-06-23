package rx;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Blog {

    public static class Account {
        private int balance;

        Account(int balance) { this.balance = balance; }
        public int getBalance() {
            return balance;
        }

        public synchronized int withdraw(String name, int amount) {
            try {
                System.out.println(name + " trying to withdraw "+ amount);
                if(balance < amount)
                    throw new RuntimeException("Not enough balance for "+ name);
                Thread.sleep(1000);
                balance = balance-amount;
                System.out.println(name + " has withdrawn " + amount + " remaining bal " + balance);
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
            return amount;
        }
    }

    public static void main(String args[]) {

        Account jointAcc = new Account(5000);
        final int maxLimit = 5;
        /*
        AtomicInteger count = new AtomicInteger();
        AtomicBoolean turn = new AtomicBoolean();

        Thread T1 = new Thread(() -> {
            while(jointAcc.getBalance() > 0 && count.get() < maxLimit) {
                if(turn.get()) {
                    jointAcc.withdraw("Umesh", 1000);
                    count.incrementAndGet();
                    turn.set(!turn.get());
                }
            }
        });

        Thread T2 = new Thread(() -> {
            while(jointAcc.getBalance() > 0 && count.get() < maxLimit)
                if(!turn.get()) {
                    jointAcc.withdraw("Akansha", 1000);
                    count.incrementAndGet();
                    turn.set(!turn.get());
                }
        });

        T1.start();
        T2.start();
        */
        PublishSubject<Integer> umesh$ = PublishSubject.create();
        PublishSubject<Integer> akansha$ = PublishSubject.create();

        umesh$.subscribe((amount) -> jointAcc.withdraw("Umesh", amount));
        akansha$.subscribe((amount) -> jointAcc.withdraw("Akansha", amount));
        Observable
                .just(true, false).repeat()
                .take(maxLimit)
                .subscribe((flag) -> {
                    if(flag)
                        umesh$.onNext(1000);
                    else
                        akansha$.onNext(1000);
                });

        Observable.never().blockingFirst();
    }
}
