package lim.jhsoft.com.firebasestore;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

import lim.jhsoft.com.firebasestore.model.CitiesSfDTO;

public class MainActivity extends AppCompatActivity {
    static final String TAG = "firestore";
    private Button AddandManageData;
    private Button Query_getdata;
    private Button queryRealitimeUpdate;
    private TextView  queryRealitimeUpdateText;
    private Button queryRealitimeUpdateAdd;
    private Button queryViewChangeBetweenSnapshots;
    private Button performSimpleAndCompoundQueries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        AddandManageData = findViewById(R.id.AddandManageData);
        Query_getdata = findViewById(R.id.Query_getdata);
        queryRealitimeUpdate = findViewById(R.id.query_realitime_update);
        queryRealitimeUpdateText = findViewById(R.id.query_realitime_update_text);
        queryViewChangeBetweenSnapshots = findViewById(R.id.query_view_change_between_snapshots);
        queryRealitimeUpdateAdd = findViewById(R.id.query_realitime_update_add);
        performSimpleAndCompoundQueries = findViewById(R.id.perform_simple_and_compound_queries);


        AddandManageData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddandManageDataActivity.class);
                startActivity(intent);
            }
        });

        queryRealitimeUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final DocumentReference docRef = db.collection("cities").document("SF");
                docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot,
                                        @Nullable FirebaseFirestoreException e) {

                        //CitiesSfDTO citiesSfDTO = (CitiesSfDTO) snapshot.getData();
                        //System.out.println(citiesSfDTO.population);


                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        if (snapshot != null && snapshot.exists()) {
                            queryRealitimeUpdateText.setText(snapshot.getData().toString());
                            Log.d(TAG, "Current data: " + snapshot.getData());
                        } else {
                            Log.d(TAG, "Current data: null");
                        }
                    }
                });
            }
        });

        queryRealitimeUpdateAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final DocumentReference sfDocRef = db.collection("cities").document("SF");

                db.runTransaction(new Transaction.Function<Void>() {
                    @Override
                    public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                        DocumentSnapshot snapshot = transaction.get(sfDocRef);
                        double newPopulation = snapshot.getDouble("population") + 1;
                        transaction.update(sfDocRef, "population", newPopulation);

                        // Success
                        return null;
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Transaction success!");
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Transaction failure.", e);
                            }
                        });
            }
        });

        queryViewChangeBetweenSnapshots.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.collection("cities")
                        .whereEqualTo("state", "CA")
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot snapshots,
                                                @Nullable FirebaseFirestoreException e) {
                                if (e != null) {
                                    Log.w(TAG, "listen:error", e);
                                    return;
                                }

                                for (DocumentChange dc : snapshots.getDocumentChanges()) {
                                    switch (dc.getType()) {
                                        case ADDED:
                                            Log.d(TAG, "New city: " + dc.getDocument().getData());
                                            break;
                                        case MODIFIED:
                                            Log.d(TAG, "Modified city: " + dc.getDocument().getData());
                                            break;
                                        case REMOVED:
                                            Log.d(TAG, "Removed city: " + dc.getDocument().getData());
                                            break;
                                    }
                                }

                            }
                        });
            }
        });

        performSimpleAndCompoundQueries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*CollectionReference cities = db.collection("cities");

                Map<String, Object> data1 = new HashMap<>();
                data1.put("name", "San Francisco");
                data1.put("state", "CA");
                data1.put("country", "USA");
                data1.put("capital", false);
                data1.put("population", 860000);
                cities.document("SF").set(data1);

                Map<String, Object> data2 = new HashMap<>();
                data2.put("name", "Los Angeles");
                data2.put("state", "CA");
                data2.put("country", "USA");
                data2.put("capital", false);
                data2.put("population", 3900000);
                cities.document("LA").set(data2);

                Map<String, Object> data3 = new HashMap<>();
                data3.put("name", "Washington D.C.");
                data3.put("state", null);
                data3.put("country", "USA");
                data3.put("capital", true);
                data3.put("population", 680000);
                cities.document("DC").set(data3);

                Map<String, Object> data4 = new HashMap<>();
                data4.put("name", "Tokyo");
                data4.put("state", null);
                data4.put("country", "Japan");
                data4.put("capital", true);
                data4.put("population", 9000000);
                cities.document("TOK").set(data4);

                Map<String, Object> data5 = new HashMap<>();
                data5.put("name", "Beijing");
                data5.put("state", null);
                data5.put("country", "China");
                data5.put("capital", true);
                data5.put("population", 21500000);
                cities.document("BJ").set(data5);*/

                // Create a reference to the cities collection
                CollectionReference citiesRef = db.collection("cities");

// Create a query against the collection.
                Query query = citiesRef.whereEqualTo("state", "CA");
                Query capitalCities = db.collection("cities").whereEqualTo("capital", true);

                citiesRef.whereEqualTo("state", "CA");
                citiesRef.whereLessThan("population", 100000);
                citiesRef.whereGreaterThanOrEqualTo("name", "San Francisco");


            }
        });


    }
}
