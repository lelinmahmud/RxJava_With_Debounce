package com.lelin.rxjavawithdebounce.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;

import com.lelin.rxjavawithdebounce.R;
import com.lelin.rxjavawithdebounce.adapter.ContactsAdapterFilterable;
import com.lelin.rxjavawithdebounce.network.ApiClient;
import com.lelin.rxjavawithdebounce.network.ApiService;
import com.lelin.rxjavawithdebounce.network.model.Contact;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private SearchView searchView;
    private ApiService apiService;


    private CompositeDisposable compositeDisposable=new CompositeDisposable();
    private long timeSinceLastRequest;

    private RecyclerView recyclerView;
    private ContactsAdapterFilterable adapter;
    private List<Contact> contacts=new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        searchView=findViewById(R.id.search_view);
        timeSinceLastRequest=System.currentTimeMillis();
        recyclerView=findViewById(R.id.recycler_view);
        apiService = ApiClient.getClient().create(ApiService.class);
        adapter=new ContactsAdapterFilterable(this,contacts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);
        Observable<String> observableQueryText=
                Observable.create(new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(final ObservableEmitter<String> emitter) throws Exception {
                        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                            @Override
                            public boolean onQueryTextSubmit(String s) {
                                return false;
                            }

                            @Override
                            public boolean onQueryTextChange(String s) {
                                if (!emitter.isDisposed()){
                                    emitter.onNext(s);
                                }
                                return false;
                            }
                        });
                    }
                })
                .debounce(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io());


        observableQueryText.subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
              //  compositeDisposable.add(d);
            }
            @Override
            public void onNext(String s) {
                Log.d(TAG, "onNext: time  since last request: " + (System.currentTimeMillis() - timeSinceLastRequest));
                Log.d(TAG, "onNext: search query: " + s);
                timeSinceLastRequest = System.currentTimeMillis();

                // method for sending a request to the server
                sendRequestToServer(s);
            }
            @Override
            public void onError(Throwable e) {
            }
            @Override
            public void onComplete() {
            }
        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }



    private void sendRequestToServer(String query){
        compositeDisposable.add(apiService.getContacts(null,query)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeWith(new DisposableSingleObserver<List<Contact>>(){

            @Override
            public void onSuccess(List<Contact> contact) {
                Log.e(TAG, "onSuccess: size :"+contact.size() );
                contacts.clear();
                contacts.addAll(contact);
                adapter.notifyDataSetChanged();
                for (int i=0;i<contact.size();i++){
                    Log.e(TAG, "onSuccess: name :"+contact.get(i).getName() );
                }
            }

            @Override
            public void onError(Throwable e) {

            }
        }));
    }
}
