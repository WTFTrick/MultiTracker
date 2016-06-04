package app.multitracker;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class MultiTracker extends AppCompatActivity {

    private MapView osm;
    private MapController mc;
    ArrayList<GeoPoint> geoPointsArray = new ArrayList<GeoPoint>();
    ArrayList<Polyline> polylineArray = new ArrayList<Polyline>();
    ArrayList<String> keyslist = new ArrayList<String>();
    ArrayList<Marker> markersArray = new ArrayList<Marker>();
    ArrayList<ArrayList<GeoPoint>> arrayOfGeoPoints = new ArrayList<ArrayList<GeoPoint>>();
    ArrayList<GeoPoint> geoPoints;
    Firebase ref;
    Marker marker;
    RoadManager roadManager;
    Road road;
    Polyline roadOverlay;
    int polylineColor = Color.parseColor("#2E7D32");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_tracker);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        osm = (MapView) findViewById(R.id.mapView);
        osm.setUseDataConnection(true);
        osm.setTileSource(TileSourceFactory.MAPNIK);
        osm.setBuiltInZoomControls(true);
        osm.setMultiTouchControls(true);

        mc = (MapController) osm.getController();
        osm.setMinZoomLevel(3);
        osm.setMaxZoomLevel(18);
        mc.setZoom(18);

        osm.invalidate();

        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(MultiTracker.CONNECTIVITY_SERVICE);

        NetworkInfo i = cm.getActiveNetworkInfo();

        if ((i.isConnected()) && (i.isAvailable())) {
            Firebase.setAndroidContext(this);
            ref = new Firebase("https://locmanager.firebaseio.com/");
            ref.addChildEventListener(new ChildEventListener() {

                @Override
                public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {

                    Map<String, Double> newPost = (Map<String, Double>) snapshot.getValue();
                    System.out.println("Latitude: " + newPost.get("Latitude"));
                    System.out.println("Longitude: " + newPost.get("Longitude"));
                    Double lat = (Double) snapshot.child("Latitude").getValue();
                    Double lon = (Double) snapshot.child("Longitude").getValue();
                    Toast.makeText(getApplicationContext(), "Lat: \n" + lat + "\nLon: \n" + lon, Toast.LENGTH_LONG).show();
                    String Key = snapshot.getKey();

                    GeoPoint GP = new GeoPoint(lat, lon);

                    keyslist.add(Key);
                    System.out.println("IdKey: " + Key);

                    createMarker(GP, keyslist.indexOf(Key));
                    createPolyline(GP, keyslist.indexOf(Key));

                    mc.animateTo(GP);
                    mc.setZoom(18);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Double lat = (Double) dataSnapshot.child("Latitude").getValue();
                    Double lon = (Double) dataSnapshot.child("Longitude").getValue();
                    GeoPoint GP = new GeoPoint(lat, lon);
                    String Key = dataSnapshot.getKey();

                    geoPointsArray.add(GP);

                    changeMarkerPosition(GP, keyslist.indexOf(Key));
                    DrawPolyline(GP, keyslist.indexOf(Key));
                    //createPolyline(GP, keyslist.indexOf(Key));

                    System.out.println("IdKey when update: " + Key);
                    System.out.println("IdKey from array of Keys: " + keyslist.indexOf(Key));

                /*if (countOfClients >= 1) {
                    addMarker(GP);
                }*/

                    osm.invalidate();
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }

            });
        } else {
            System.out.println("No internet connection");
        }
    }

    public void createMarker(GeoPoint GP, int index)
    {
        marker = new Marker(osm);
        marker.setIcon(getResources().getDrawable(R.drawable.locmarker));
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setEnabled(true);
        osm.getOverlays().add(marker);
        marker.setPosition(GP);
        osm.invalidate();

        markersArray.add(index, marker);
    }

    public void createPolyline(GeoPoint GP, int index) {
        geoPoints = new ArrayList<GeoPoint>();
        geoPoints.add(GP);
        roadManager = new OSRMRoadManager();
        road = roadManager.getRoad(geoPoints);
        roadOverlay = RoadManager.buildRoadOverlay(road, polylineColor, 6, getApplicationContext());
        polylineArray.add(index, roadOverlay);
        arrayOfGeoPoints.add(index, geoPoints);
        osm.getOverlays().add(roadOverlay);
        osm.invalidate();
    }

    public void DrawPolyline(GeoPoint GP, int index) {
        arrayOfGeoPoints.get(index).add(GP);
        polylineArray.get(index).setPoints(arrayOfGeoPoints.get(index));
        osm.getOverlays().add(polylineArray.get(index));
        osm.invalidate();
    }

    public void changeMarkerPosition(GeoPoint GP, int index) {
        System.out.println("Index from array: " + index);
        markersArray.get(index).setPosition(GP);
        osm.invalidate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_exit: {
                quit();
                return true;
            }
            case R.id.action_clear: {
                ref.setValue(null);
                osm.getOverlays().clear();

                    /*geoPointsArray.clear();
                    keyslist.clear();
                    arr_route1.clear();
                    arr_route2.clear();*/

                osm.invalidate();
                return true;
            }
            default:
                return true;
        }
    }

    public void quit() {
        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);
        System.exit(0);
        finish();
    }
}
