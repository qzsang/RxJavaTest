package com.qzsang.rxjavatest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    String[] itemNames = new String[]{
            "rxjava最简单的使用(显示1 ~ 3)",
            "Observable.just 方法  和Subscriber局部定义",
            "Observable.from 方法  和Subscriber的全部局部定义",
            "Rxjava schedule，线程切换",
            "Observable.map方法，即类型转换",
            "Observable.flatMap方法，个人理解是 拆分重发",
            "Rxjava schedule，线程多次切换",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView listView = (ListView) findViewById(R.id.lv_list);
        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return itemNames.length;
            }

            @Override
            public String getItem(int position) {
                return itemNames[position];
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = new TextView(MainActivity.this);
                }
                TextView textView = (TextView) convertView;
                textView.setText(getItem(position));
                return convertView;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        test0();
                        break;
                    case 1:
                        test1();
                        break;

                    case 2:
                        test2();
                        break;

                    case 3:
                        test3();
                        break;
                    case 4:
                        test4();
                        break;
                    case 5:
                        test5();
                        break;
                    case 6:
                        test6();
                        break;
                }
            }
        });
    }

    //多次转换
    public void test6() {

        Observable.just(1, 2, 3, 4) // IO 线程，由 subscribeOn() 指定
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(new Func1<Integer, Person>() {
                    @Override
                    public Person call(Integer integer) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Person p = new Person();
                        p.age = integer + 20;
                        p.name = "name->" + integer;
                        return p;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Action1<Person>() {
                    @Override
                    public void call(Person person) {
                        showToast(person.toString());
                    }
                })
                .observeOn(Schedulers.io())
                .map(new Func1<Person, String>() {
                    @Override
                    public String call(Person person) {
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return person.name;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        showToast(s);
                    }
                });
    }

    //拆分重发
    public void test5() {
        Person[] ps = new Person[2];
        for (int i = 0; i < ps.length; i++) {
            ps[i] = new Person();
            ps[i].childrens = new Integer[]{10 * (i + 1), 10 * (i + 1) + 1};
        }

        Observable.from(ps)
               // .observeOn(Schedulers.io())
                .flatMap(new Func1<Person, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(Person person) {
                        /*try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }*/
                        return Observable.from(person.childrens);
                    }
                })
               // .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        showToast(integer + "");
                    }
                });
    }

    //类型转换
    public void test4() {
        Observable.just(1, 2, 3)
                .map(new Func1<Integer, Person>() {
                    @Override
                    public Person call(Integer integer) {
                        Person person = new Person();
                        person.age = 18 + integer;
                        person.name = "name->" + integer;
                        return person;
                    }
                })
                .map(new Func1<Person, String>() {
                    @Override
                    public String call(Person person) {
                        return person.toString();
                    }
                })
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        showToast(s);
                    }
                });
    }

    /**
     * Schedulers.immediate(): 直接在当前线程运行，相当于不指定线程。这是默认的 Scheduler。
     * Schedulers.newThread(): 总是启用新线程，并在新线程执行操作。
     * Schedulers.io(): I/O 操作（读写文件、读写数据库、网络信息交互等）所使用的 Scheduler。
     * 行为模式和 newThread() 差不多，区别在于 io() 的内部实现是是用一个无数量上限的线程池，
     * 可以重用空闲的线程，因此多数情况下 io() 比 newThread() 更有效率。不要把计算工作放在 io() 中，
     * 可以避免创建不必要的线程。
     * Schedulers.computation(): 计算所使用的 Scheduler。这个计算指的是 CPU 密集型计算，
     * 即不会被 I/O 等操作限制性能的操作，例如图形的计算。这个 Scheduler 使用的固定的线程池，
     * 大小为 CPU 核数。不要把 I/O 操作放在 computation() 中，否则 I/O 操作的等待时间会浪费 CPU。
     * 另外， Android 还有一个专用的 AndroidSchedulers.mainThread()，它指定的操作将在 Android 主线程运行。
     * <p>
     * 通过 subscribeOn 和 observeOn 切换线程
     * subscribeOn ：指定 在订阅的时候 线程 ，即
     * observeOn ：指定 在观察中的 线程， 即
     */
    public void test3() {
        Observable observable = Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                try {
                    Thread.sleep(1000 * 5);//模拟耗时操作
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (int i = 1; i <= 3; i++) {
                    subscriber.onNext(i);
                }
                subscriber.onCompleted();
            }
        });

        observable
                .subscribeOn(Schedulers.io())//指定 线程
                .observeOn(AndroidSchedulers.mainThread())//指定 线程
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {
                        showToast("subscriber - onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        showToast("subscriber - onError : " + e);
                    }

                    @Override
                    public void onNext(Integer o) {
                        showToast("subscriber - onNext : " + o);
                    }
                });
    }

    public void test2() {
        Observable
                .from(new Integer[]{1, 2, 3})
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        showToast("subscriber - onNext:" + integer);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        showToast("subscriber - onError");
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        showToast("subscriber - onCompleted");
                    }
                });
    }

    public void test1() {
        Observable
                .just(1, 2, 3)
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        showToast("subscriber - onNext:" + integer);
                    }
                });
    }

    //显示1 ~ 3
    public void test0() {

        // onCompleted 和 onError 只会调用一次  。如果在onNext或在call 有错  则结果会执行onError  否则结果执行onCompeleted
        Subscriber<Integer> subscriber = new Subscriber<Integer>() {
            @Override
            public void onStart() {
                super.onStart();
                showToast("subscriber - onStart");
            }

            @Override
            public void onCompleted() {
                showToast("subscriber - onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                showToast("subscriber - onError");
            }

            @Override
            public void onNext(Integer integer) {
                showToast("subscriber - onNext:" + integer);
               /* if (integer > 3)
                    throw new RuntimeException();*/
            }
        };
        Observable<Integer> observable = Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                for (int i = 1; i <= 3; i++)
                    subscriber.onNext(i);
                subscriber.onCompleted();
            }
        });
        observable.subscribe(subscriber);

        //：要在不再使用的时候尽快在合适的地方（例如 onPause() onStop() 等方法中）
        // 调用 unsubscribe() 来解除引用关系，以避免内存泄露的发生。
//        if (!subscriber.isUnsubscribed())
//            subscriber.unsubscribe();

    }


    private void showToast(String str) {
        Toast.makeText(this, str + "", Toast.LENGTH_SHORT).show();
    }


    class Person {
        public String name;
        public int age;
        public Integer[] childrens;

        @Override
        public String toString() {
            return "Person{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    '}';
        }
    }
}
