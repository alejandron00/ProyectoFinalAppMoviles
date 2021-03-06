package com.example.proyectofinalappmoviles.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.proyectofinalappmoviles.R;
import com.example.proyectofinalappmoviles.adapter.PublicationAdapter;
import com.example.proyectofinalappmoviles.model.Publication;
import com.example.proyectofinalappmoviles.ui.PublicationFragment;
import com.example.proyectofinalappmoviles.util.HTTPSWebUtilDomi;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private ListView publications;
    FirebaseDatabase db;
    PublicationAdapter adapter;
    private EditText searchBar;
    private ImageButton searchBtn;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        return root;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db=FirebaseDatabase.getInstance();
        publications=getView().findViewById(R.id.publications_lv);
        adapter=new PublicationAdapter();
        publications.setAdapter(adapter);
        searchBar = view.findViewById(R.id.homeSearchTxt);
        searchBtn = view.findViewById(R.id.homeSearchBtn);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String search = searchBar.getText().toString();
                searchByName(search);

            }


        });

        ArrayList<Publication> pubs=new ArrayList<>();
        db.getReference().child("publications").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data: dataSnapshot.getChildren()
                ) {
                    pubs.add(data.getValue(Publication.class));

                }
                adapter.setPubs(pubs);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        publications.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Publication publication = pubs.get(position);
                PublicationFragment fragment = new PublicationFragment();
                FragmentManager fragmentManager = getFragmentManager();
                Bundle bundle = new Bundle();
                Gson gson = new Gson();
                String jsonPub = gson.toJson(publication);
                bundle.putString("publication", jsonPub);
                fragment.setArguments(bundle);
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.nav_host_fragment, fragment);
                fragmentTransaction.commit();
            }
        });

}

    @Override
    public void onResume() {
        super.onResume();
        String search = "";
        try {
            search = getArguments().getString("category");
            Log.e("categoty", search);
        } catch (Exception e) {

        }

        if (!search.equals("")) {
            searchByCategory(search);
        }
    }

    private void searchByName(String name) {
        HTTPSWebUtilDomi utilDomi = new HTTPSWebUtilDomi();

        new Thread(() -> {

            try {
                String data = utilDomi.GETrequest("https://us-central1-marketicesiappsmoviles-2019-2.cloudfunctions.net/functions/pubsByName?name=" + name);
                Log.e("data", data);
                Gson gson = new Gson();
                Type listType = new TypeToken<ArrayList<Publication>>() {
                }.getType();

                final ArrayList<Publication> searchPubs = gson.fromJson(data, listType);
                getActivity().runOnUiThread(() -> {
                    adapter.setPubs(searchPubs);
                    adapter.notifyDataSetChanged();
                });

            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();
    }

    private void searchByCategory(String cat) {
        HTTPSWebUtilDomi utilDomi = new HTTPSWebUtilDomi();

        new Thread(() -> {

            try {
                String data = utilDomi.GETrequest("https://us-central1-marketicesiappsmoviles-2019-2.cloudfunctions.net/functions/pubsByCategory?category=" + cat);
                Log.e("data", data);
                Gson gson = new Gson();
                Type listType = new TypeToken<ArrayList<Publication>>() {
                }.getType();

                final ArrayList<Publication> searchPubs = gson.fromJson(data, listType);
                getActivity().runOnUiThread(() -> {
                    adapter.setPubs(searchPubs);
                    adapter.notifyDataSetChanged();
                });

            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();
    }
}