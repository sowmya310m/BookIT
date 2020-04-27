package Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.projectfirebase.soen341.root.Helper;
import com.projectfirebase.soen341.root.ItemDescription;
import com.projectfirebase.soen341.root.R;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import static com.projectfirebase.soen341.root.Helper.setImage;

public class ItemDetailsPostingsFragment extends Fragment implements View.OnClickListener{
    public static String itemIDToDisplay;
    public static String itemNameToDisplay;
    private String sellerId;
    public ItemDescription itemToDisplay;
    private final String NO_DESC = "No additional information available";

    private DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference itemsRef = rootRef.child("Items");
    private DatabaseReference userRef = rootRef.child("Users");
    private DatabaseReference currentUserRef;
    private DatabaseReference favRef;
    private DatabaseReference postingsRef;
    private DatabaseReference userBoughtRef;
    private DatabaseReference processingRef;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private TextView name_tv, price_tv, description_tv, seller_name_tv, seller_email_tv, boughtStatus;
    private ImageView item_iv, seller_iv;
    private ToggleButton favorite_tb;
    private Button deleteButton;

    String sellerName, sellerEmail, sellerPhotoURL;

    public ItemDetailsPostingsFragment() {
        // Required empty public constructor
    }

    public static ItemDetailsPostingsFragment newInstance() {
        return new ItemDetailsPostingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_item_details_postings, container, false);

        //Get the views in case we ever want to do anything with them
        name_tv = (TextView) view.findViewById(R.id.item_name);
        price_tv = (TextView) view.findViewById(R.id.item_price);
        description_tv = (TextView) view.findViewById(R.id.item_description);
        item_iv = (ImageView) view.findViewById(R.id.item_photo);
        favorite_tb = (ToggleButton) view.findViewById(R.id.favorite);
        seller_name_tv = (TextView) view.findViewById(R.id.seller_name);
        seller_email_tv = (TextView) view.findViewById(R.id.seller_email);
        seller_iv = (ImageView) view.findViewById(R.id.seller_photo);
        deleteButton = (Button) view.findViewById(R.id.deleteButton);
        boughtStatus = (TextView) view.findViewById(R.id.boughtStatus);

        if (user != null) {
            setDeleteButtonListener();
        } else {
            favorite_tb.setVisibility(View.GONE);
        }

        return view;
    }

    private void setDeleteButtonListener() {
        currentUserRef = rootRef.child("Users").child(user.getUid());
        processingRef = rootRef.child("Processing");
        postingsRef = currentUserRef.child("Postings");
        userBoughtRef = currentUserRef.child("Bought");
        deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Fragment selectedFragment = PostingsFragment.newInstance();
                processingRef.child(itemIDToDisplay).removeValue();
                postingsRef.child(itemIDToDisplay).removeValue();
                Map<String, Object> inProcess = new HashMap<>();
                inProcess.put("InProcess", true);
                itemsRef.child(itemIDToDisplay).updateChildren(inProcess);
                //itemsRef.child(itemIDToDisplay).removeValue();
                Map<String, Object> bought = new HashMap<>();
                bought.put("Bought", true);
                bought.put("BuyerID", user.getUid());
                itemsRef.child(itemIDToDisplay).updateChildren(bought);

                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.frame_layout, selectedFragment);
                transaction.commit();
            }
        });
        userBoughtRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot dataSnapshot) {
                    // do something
                String s = "hello";
            }

            @Override
            public void onCancelled( DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        //make the id string final to make it accessible in the onDataChange listener
        this.populateItem();
    }

    public void populateItem() {
        final DatabaseReference item = itemsRef.child(itemIDToDisplay);
        itemToDisplay = new ItemDescription();

        item.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot dataSnapshot) {
                Map<String, Object> itemsInDB = (HashMap<String, Object>) dataSnapshot.getValue();
                Map<String, Object> itemObj = (Map<String, Object>) itemsInDB;

                //get the data for the item to display
                sellerId = (String) itemObj.get("OwnerID");
                String name = (String) itemObj.get("Name");
                itemNameToDisplay = name;
                String description = (String) itemObj.get("Description");
                String url = (String) itemObj.get("ImageURL");
                Double price = ((Number) itemObj.get("Price")).doubleValue();
                int category = ((Number) itemObj.get("Category")).intValue();
                int subCategory = ((Number) itemObj.get("SubCategory")).intValue();
                boolean processing;
                boolean bought = false;
                if(itemObj.get("Processing")!=null) {
                    processing = ((Boolean) itemObj.get("Processing")).booleanValue();
                }
                String buyerID = null;
                String buyerName = null;
                if(itemObj.get("BuyerID") != null) {
                    buyerID = (String) itemObj.get("BuyerID");
                    if(itemObj.get("BuyerName") != null) {
                        buyerName = (String) itemObj.get("BuyerName");
                    }
                }
                //set it
                itemToDisplay = new ItemDescription(itemIDToDisplay, sellerId, name, price, url, description, category, subCategory);

                setDisplayViews(buyerID, buyerName);
                setSellerDetails();
            }

            @Override
            public void onCancelled( DatabaseError databaseError) {
            }
        });
    }

    public void setDisplayViews(String buyerID, String buyerName) {
        name_tv.setText(this.itemToDisplay.getName());
        if((buyerID != null) && (buyerName != null)) {
            boughtStatus.setText("Bought by " + buyerName);
            deleteButton.setVisibility(View.INVISIBLE);
        } else {
            boughtStatus.setText("Not Bought");
        }

        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        String price = formatter.format(this.itemToDisplay.getPrice());
        price_tv.setText(price);

        if (Helper.isNullOrEmpty(this.itemToDisplay.getDescription())) {
            description_tv.setText(NO_DESC);
        } else {
            description_tv.setText(this.itemToDisplay.getDescription());
        }

        setImage(getActivity(), itemToDisplay.getImageURL(), item_iv);
    }

    public static void setItemIDToDisplay(String id) {
        ItemDetailsPostingsFragment.itemIDToDisplay = id;
    }

    private void setSellerDetails() {
        DatabaseReference sellerRef = rootRef.child("Users").child(sellerId);

        sellerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot dataSnapshot) {
                sellerPhotoURL = dataSnapshot.child("ImageURL").getValue(String.class);
                if (!Helper.isNullOrEmpty(sellerPhotoURL)) {
                    setImage(getActivity(), dataSnapshot.child("ImageURL").getValue(String.class), seller_iv);
                }
                // Getting entire seller details object here -- Has information for seller contact information
                sellerName = dataSnapshot.child("firstName").getValue(String.class) + " " + dataSnapshot.child("lastName").getValue(String.class);
                sellerEmail = dataSnapshot.child("email").getValue(String.class);
                seller_name_tv.setText(sellerName);
                seller_email_tv.setText(sellerEmail);
            }

            @Override
            public void onCancelled( DatabaseError databaseError) {
            }
        });
    }


    private void setFavoriteButtonListener() {
        currentUserRef = rootRef.child("Users").child(user.getUid());
        favRef = currentUserRef.child("Favorites");

        favRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(itemIDToDisplay))
                    setFavToggle(true);
                else
                    setFavToggle(false);
            }

            @Override
            public void onCancelled( DatabaseError databaseError) {
            }
        });
    }

    public void setFavToggle(final boolean isFavorite) {
        rootRef = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            currentUserRef = rootRef.child("Users").child(user.getUid());

            if (isFavorite) {
                favorite_tb.setChecked(true);
                favorite_tb.setBackgroundResource(R.drawable.ic_star_yellow_24dp);
            } else {
                favorite_tb.setChecked(false);
                favorite_tb.setBackgroundResource(R.drawable.ic_star_border_yellow_24dp);
            }

            if (user != null) {
                favorite_tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            //usersRef.child(user.getUid()).child("Favorites").setValue(favString + ";" + holder.id.toString());
                            currentUserRef.child("Favorites").child(itemIDToDisplay).setValue(true);
                            favorite_tb.setBackgroundResource(R.drawable.ic_star_yellow_24dp);
                        } else {
                        /*newFavList.remove(newFavList.indexOf(holder.id.toString()));
                        usersRef.child(user.getUid()).child("Favorites").setValue(android.text.TextUtils.join(";", newFavList));*/
                            currentUserRef.child("Favorites").child(itemIDToDisplay).removeValue();

                            favorite_tb.setBackgroundResource(R.drawable.ic_star_border_yellow_24dp);
                        }
                    }
                });
            }
        }
    }

    private void moveRecord( final DatabaseReference fromPath, final DatabaseReference toPath) {
        fromPath.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot dataSnapshot) {
                toPath.setValue(dataSnapshot.getValue(), new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete( DatabaseError firebaseError, DatabaseReference firebase) {
                        if (firebaseError != null) {
                            System.out.println("Copy failed");
                        } else {
                            System.out.println("Success");

                        }
                    }
                });

            }

            @Override
            public void onCancelled( DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        String s = "Inside class onclick";
    }
}
