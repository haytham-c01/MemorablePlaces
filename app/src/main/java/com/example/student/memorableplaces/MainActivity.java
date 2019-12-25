package com.example.student.memorableplaces;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ListView listView;
    static ArrayList<String> places;
    static ArrayList<LatLng> locations;
    static ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView= findViewById(R.id.listView);

        places= new ArrayList<>();
        locations= new ArrayList<>();
        restoreSavedLocations();

        arrayAdapter= new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, places);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent= new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("placeNumber", i);

                startActivity(intent);
            }
        });

    }

    private void restoreSavedLocations(){
        ArrayList<String> latitudes= new ArrayList<>();
        ArrayList<String> longitudes= new ArrayList<>();

        SharedPreferences sharedPreferences= this.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);

        try {
            places= (ArrayList<String>)ObjectSerializer.deserialize(sharedPreferences.getString("places", ObjectSerializer.serialize(new ArrayList<String>())));
            latitudes= (ArrayList<String>)ObjectSerializer.deserialize(sharedPreferences.getString("latitudes", ObjectSerializer.serialize(new ArrayList<String>())));
            longitudes= (ArrayList<String>)ObjectSerializer.deserialize(sharedPreferences.getString("longitudes", ObjectSerializer.serialize(new ArrayList<String>())));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(places.size()>0 && latitudes.size()> 0 && longitudes.size()> 0){
            Log.i("flow", "first if block working");
            if(places.size() == latitudes.size() && places.size() == longitudes.size()){
                for(int i=0; i<places.size(); i++){
                    locations.add(new LatLng(Double.parseDouble(latitudes.get(i)), Double.parseDouble(longitudes.get(i))));
                }
            }
        }else{
            Log.i("flow", "else block working");
            places.add("Add a new place...");
            locations.add(new LatLng(0,0));
        }
    }
}
