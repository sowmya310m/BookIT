package Fragments;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.projectfirebase.soen341.root.Adapters.ListItemAdapter2;
import com.projectfirebase.soen341.root.Listing;
import com.projectfirebase.soen341.root.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.view.View.OnClickListener;

public class OrderHistoryFragment extends Fragment implements OnClickListener {
    private FirebaseAuth authRef = FirebaseAuth.getInstance();
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference itemsRef = rootRef.child("Items");
    private DatabaseReference currentUserRef;
    private DatabaseReference boughtRef;

    private ArrayList<Listing> listingsList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ListItemAdapter2 mAdapter;

    private TextView fav_message_tv;

    public OrderHistoryFragment() {
        // Required empty public constructor
    }

    public static OrderHistoryFragment newInstance() {
        return new OrderHistoryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_postings, container, false);

        currentUserRef = rootRef.child("Users").child(user.getUid());
        boughtRef = currentUserRef.child("Bought");

        final View currentView = view;
        boughtRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren())
                    populateOrdersList(currentView, dataSnapshot);
                else
                    setMessage(currentView, R.string.no_favorites);
            }

            @Override
            public void onCancelled( DatabaseError databaseError) {
            }
        });

        return view;
    }

    public void populateOrdersList(View view, final DataSnapshot ordersRef) {
        recyclerView = (RecyclerView) view.findViewById(R.id.posting_recycler_view);
        mAdapter = new ListItemAdapter2(listingsList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mAdapter);

        boughtRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot dataSnapshot) {
                listingsList.clear();
                Map<String, Object> ordersMap = (HashMap<String, Object>) ordersRef.getValue();
                Map<String, Object> itemsMap = (HashMap<String, Object>) dataSnapshot.getValue();

                for (String key : ordersMap.keySet()) {
                    Object itemMap = itemsMap.get(key);
                    if (itemMap instanceof Map) {
                        Map<String, Object> itemObj = (Map<String, Object>) itemMap;
                        String name = (String) itemObj.get("Name");
                        Double price = ((Number) itemObj.get("Price")).doubleValue();
                        String url = (String) itemObj.get("ImageURL");
                        Listing item = new Listing(key, name, price, url);
                        listingsList.add(item);
                    }
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled( DatabaseError databaseError) {
            }
        });
    }

    public void setMessage(View view, int messageID) {
        fav_message_tv = (TextView) view.findViewById(R.id.fav_message);
        fav_message_tv.setText(messageID);
    }

    @Override
    public void onClick(View v) {
        String s = "hello";
        int i = 10;
        long l = SystemClock.currentThreadTimeMillis();
    }
}
