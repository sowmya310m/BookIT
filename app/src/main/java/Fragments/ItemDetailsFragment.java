package Fragments;

import android.app.Activity;
import android.content.Intent;
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
import com.projectfirebase.soen341.root.MapsActivity;
import com.projectfirebase.soen341.root.R;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import static com.projectfirebase.soen341.root.Helper.setImage;

public class ItemDetailsFragment extends Fragment implements View.OnClickListener{
    public static String itemIDToDisplay;
    public static String itemNameToDisplay;
    private String sellerId;
    public ItemDescription itemToDisplay;
    private final String NO_DESC = "No additional information available";

    private DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference itemsRef = rootRef.child("Items");
    private DatabaseReference currentUserRef;
    private DatabaseReference favRef;
    private DatabaseReference postingsRef;
    private DatabaseReference userBoughtRef;
    private DatabaseReference processingRef;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private TextView name_tv, price_tv, description_tv, seller_name_tv, seller_email_tv;
    private ImageView item_iv, seller_iv;
    private ToggleButton favorite_tb;
    private Button buyNow;
    private Button mapsButton;
    Activity context;
    String addressInfo;

    String sellerName, sellerEmail, sellerPhotoURL;

    public ItemDetailsFragment() {
        // Required empty public constructor
    }

    public static ItemDetailsFragment newInstance() {
        return new ItemDetailsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_item_details, container, false);

        //Get the views in case we ever want to do anything with them
        name_tv = (TextView) view.findViewById(R.id.item_name);
        price_tv = (TextView) view.findViewById(R.id.item_price);
        description_tv = (TextView) view.findViewById(R.id.item_description);
        item_iv = (ImageView) view.findViewById(R.id.item_photo);
        favorite_tb = (ToggleButton) view.findViewById(R.id.favorite);
        seller_name_tv = (TextView) view.findViewById(R.id.seller_name);
        seller_email_tv = (TextView) view.findViewById(R.id.seller_email);
        seller_iv = (ImageView) view.findViewById(R.id.seller_photo);
        buyNow = (Button) view.findViewById(R.id.buyItNow);
        mapsButton = (Button) view.findViewById(R.id.mapsButton);

        if (user != null) {
            setFavoriteButtonListener();
            setBuyNowButtonListener();
        } else {
            favorite_tb.setVisibility(View.GONE);
        }

        return view;
    }


    private void setBuyNowButtonListener() {
        currentUserRef = rootRef.child("Users").child(user.getUid());
        processingRef = rootRef.child("Processing");
        postingsRef = currentUserRef.child("Postings");
        userBoughtRef = currentUserRef.child("Bought");
        final Fragment selectedFragment = HomeFragment.newInstance();
        buyNow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Map<String, Object> boughtItems = new HashMap<>();
                boughtItems.put(itemIDToDisplay,itemNameToDisplay);
                userBoughtRef.updateChildren(boughtItems);
                DatabaseReference itemRef = itemsRef.child(itemIDToDisplay).getRoot();
                //processingRef.setValue(itemsRef.child(itemIDToDisplay).g);
                Map<String, Object> userBuying = new HashMap<>();
                userBuying.put(user.getUid(),user.getDisplayName());
                userBoughtRef.child(itemIDToDisplay).updateChildren(userBuying);
                Map<String, Object> inProcess = new HashMap<>();
                inProcess.put("InProcess", true);
                inProcess.put("BuyerID", user.getUid());
                inProcess.put("BuyerName", user.getEmail());
                inProcess.put("Bought",true);
                itemsRef.child(itemIDToDisplay).updateChildren(inProcess);
                moveRecord(itemsRef.child(itemIDToDisplay),processingRef.child(itemIDToDisplay));
                moveRecord(itemsRef.child(itemIDToDisplay),userBoughtRef.child(itemIDToDisplay));
                buyNow.setVisibility(View.INVISIBLE);
                postingsRef.child(itemIDToDisplay).removeValue();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.frame_layout, selectedFragment);
                transaction.commit();
            }
        });
        userBoughtRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // do something
                String s = "hello";
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Button bt = (Button) context.findViewById(R.id.mapsButton);
        bt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){
                //EditText et= (EditText)context.findViewById(R.id.editText1);
                //create an Intent object
                Intent intent=new Intent(context, MapsActivity.class);
                intent.putExtra("address","1381 Palm Dr, Santa Clara, CA");

                //add data to the Intent object
                //intent.putExtra("text", et.getText().toString());
                //start the second activity
                startActivity(intent);
            }

        });
        //make the id string final to make it accessible in the onDataChange listener
        this.populateItem();
    }

    public void populateItem() {
        DatabaseReference item = itemsRef.child(itemIDToDisplay);
        itemToDisplay = new ItemDescription();

        item.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
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
                if(itemObj.get("Processing")!=null) {
                    boolean processing = ((Boolean) itemObj.get("Processing")).booleanValue();
                }
                //set it
                itemToDisplay = new ItemDescription(itemIDToDisplay, sellerId, name, price, url, description, category, subCategory);

                setDisplayViews();
                setSellerDetails();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void setDisplayViews() {
        name_tv.setText(this.itemToDisplay.getName());

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
        ItemDetailsFragment.itemIDToDisplay = id;
    }

    private void setSellerDetails() {
        DatabaseReference sellerRef = rootRef.child("Users").child(sellerId);

        sellerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
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
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    private void setFavoriteButtonListener() {
        currentUserRef = rootRef.child("Users").child(user.getUid());
        favRef = currentUserRef.child("Favorites");

        favRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(itemIDToDisplay))
                    setFavToggle(true);
                else
                    setFavToggle(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
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

    private void moveRecord(final DatabaseReference fromPath, final DatabaseReference toPath) {
        fromPath.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                toPath.setValue(dataSnapshot.getValue(), new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                        if (firebaseError != null) {
                            System.out.println("Copy failed");
                        } else {
                            System.out.println("Success");

                        }
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        String s = "Inside class onclick";
    }
}
