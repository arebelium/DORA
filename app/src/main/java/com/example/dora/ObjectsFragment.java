package com.example.dora;
import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class ObjectsFragment extends Fragment {

    public ObjectsFragment(){
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_objects,
                container, false);

        CheckBox cParks = view.findViewById(R.id.parks);
        CheckBox cRestaurant = view.findViewById(R.id.restaurants);
        CheckBox cMuseum = view.findViewById(R.id.museums);
        SharedPreferences prefs = getActivity().getSharedPreferences("prefs", MODE_PRIVATE);
        boolean park = prefs.getBoolean("park", false);
        boolean restaurant = prefs.getBoolean("restaurant", false);
        boolean museum = prefs.getBoolean("museum", false);
        cParks.setChecked(park);
        cRestaurant.setChecked(restaurant);
        cMuseum.setChecked(museum);
        Button button = view.findViewById(R.id.save);
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("park", cParks.isChecked());
                editor.putBoolean("restaurant", cRestaurant.isChecked());
                editor.putBoolean("museum", cMuseum.isChecked());
                editor.commit();
                Toast.makeText(getActivity(),"Preferences saved!",
                        Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }
}
