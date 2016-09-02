package clover_studio.com.supertaxi.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.squareup.picasso.Callback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import clover_studio.com.supertaxi.LastTripDialogLikeActivity;
import clover_studio.com.supertaxi.R;
import clover_studio.com.supertaxi.adapters.AddressAdapter;
import clover_studio.com.supertaxi.api.retrofit.CustomResponse;
import clover_studio.com.supertaxi.api.retrofit.DriverRetroApiInterface;
import clover_studio.com.supertaxi.api.retrofit.UserRetroApiInterface;
import clover_studio.com.supertaxi.dialog.BasicDialog;
import clover_studio.com.supertaxi.dialog.DriverDetailsDialog;
import clover_studio.com.supertaxi.dialog.RequestSentDialog;
import clover_studio.com.supertaxi.dialog.SeatsNumberDialog;
import clover_studio.com.supertaxi.dialog.ShowMoreInMapDialog;
import clover_studio.com.supertaxi.models.BaseModel;
import clover_studio.com.supertaxi.models.CheckOrderStatusModel;
import clover_studio.com.supertaxi.models.NearestDriverResponse;
import clover_studio.com.supertaxi.models.OrderModel;
import clover_studio.com.supertaxi.models.UserModel;
import clover_studio.com.supertaxi.models.post_models.PostCancelTripModel;
import clover_studio.com.supertaxi.models.post_models.PostCheckOrderStatusModel;
import clover_studio.com.supertaxi.models.post_models.PostLatLngModel;
import clover_studio.com.supertaxi.singletons.UserSingleton;
import clover_studio.com.supertaxi.utils.AnimationUtils;
import clover_studio.com.supertaxi.utils.Const;
import clover_studio.com.supertaxi.utils.ImageUtils;
import clover_studio.com.supertaxi.utils.LogCS;
import clover_studio.com.supertaxi.utils.MapsUtils;
import clover_studio.com.supertaxi.utils.Utils;
import clover_studio.com.supertaxi.view.touchable_map.MapStateListener;
import clover_studio.com.supertaxi.view.touchable_map.TouchableMapFragment;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by ivoperic on 13/07/16.
 */
public class UserMainFragment extends MainFragment implements GoogleMap.OnMarkerClickListener {

    public static final int WAIT_FOR_SEARCH_WHILE_TYPING = 500;

    private LatLng pickupLocation = null;
    private String pickupAddress = null;
    private LatLng destinationLocation = null;
    private String destinationAddress = null;

    private ImageButton ibMyLocation;
    private RelativeLayout rlParentOfMyCurrent;
    private TextView tvTextViewInMyCurrent;
    private TextView tvTextViewNearestDriverMinutes;
    private RelativeLayout rlMinutes;
    private ProgressBar pbViewInMyCurrent;
    private View viewForBlockAllClick;
    private RelativeLayout rlFromTo;
    private EditText etFrom;
    private EditText etTo;
    private View viewTimer;
    private View smallPin;
    private Button buttonRequestTaxi;
    private RelativeLayout rlCancelTripLayout;

    private RecyclerView rvFromSearch;
    private RelativeLayout rlForListFrom;
    private RecyclerView rvToSearch;
    private RelativeLayout rlForListTo;
    private RelativeLayout rlTo;
    private RelativeLayout rlFrom;
    private View viewBlackedOut;

    private boolean isUserSetLocationFromWithPin = false;
    private boolean isUserSetLocationToWithPin = false;
    private int nowCharacterFromLength = 0;
    private int nowCharacterToLength = 0;

    private Geocoder geocoder;

    private OrderModel acceptedOrder = null;
    private UserModel acceptedDriver = null;
    private int screenStatus = Const.MainUserStatus.REQUEST_TAXI_SCREEN;
    private Polyline lastPolyline = null;
    private Marker driverMarker = null;
    private Marker driverPickupMarker = null;
    private Marker drivingMarker = null;
    private Marker driverDestinationMarker = null;
    private Polyline lastOfDrivePolyline = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_user_main, container, false);

        initViews(rootView);
        resetView();
        configureRecycler();
        addListeners(rootView);

        AnimationUtils.rotationInfinite(viewTimer, true, 2000);

        mapFragment = new TouchableMapFragment();
        getFragmentManager().beginTransaction().add(layoutForMap.getId(), mapFragment, "TAG").commit();
        mapFragment.getMapAsync(UserMainFragment.this);

        geocoder = new Geocoder(getActivity(), Locale.getDefault());

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(stopChecking == true){
            stopChecking = false;
            if(acceptedOrder != null){
                checkOrderStatus(acceptedOrder);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initViews(View rootView){
        layoutForMap = (FrameLayout) rootView.findViewById(R.id.frameForMap);
        loadingProgress = (ProgressBar) rootView.findViewById(R.id.progressBarLoading);
        ibMyLocation = (ImageButton) rootView.findViewById(R.id.ibMyLocation);
        rlParentOfMyCurrent = (RelativeLayout) rootView.findViewById(R.id.rlParentOfMyCurrentLocationInside);
        tvTextViewInMyCurrent = (TextView) rootView.findViewById(R.id.tvInMarker);
        tvTextViewNearestDriverMinutes = (TextView) rootView.findViewById(R.id.tvMinValue);
        rlMinutes = (RelativeLayout) rootView.findViewById(R.id.rlMinutes);
        pbViewInMyCurrent = (ProgressBar) rootView.findViewById(R.id.pbInMarker);
        viewForBlockAllClick = rootView.findViewById(R.id.viewForBlockAllClick);
        viewTimer = rootView.findViewById(R.id.viewTimer);
        smallPin = rootView.findViewById(R.id.smallPin);
        rlFromTo = (RelativeLayout) rootView.findViewById(R.id.textViews);
        etFrom = (EditText) rootView.findViewById(R.id.etFrom);
        etTo = (EditText) rootView.findViewById(R.id.etTo);
        rvFromSearch = (RecyclerView) rootView.findViewById(R.id.rvFromSearch);
        rlForListFrom = (RelativeLayout) rootView.findViewById(R.id.rlForListFrom);
        rvToSearch = (RecyclerView) rootView.findViewById(R.id.rvToSearch);
        rlForListTo = (RelativeLayout) rootView.findViewById(R.id.rlToListFrom);
        rlTo = (RelativeLayout) rootView.findViewById(R.id.rlTo);
        rlFrom = (RelativeLayout) rootView.findViewById(R.id.rlFrom);
        viewBlackedOut = rootView.findViewById(R.id.allBlackedOut);
        buttonRequestTaxi = (Button) rootView.findViewById(R.id.requestTaxi);
        rlCancelTripLayout = (RelativeLayout) rootView.findViewById(R.id.rlButtonsCancelTrip);
    }

    private void resetView() {
        if(pickupLocation != null){
            isUserSetLocationFromWithPin = true;
        }
        if(destinationLocation != null){
            isUserSetLocationToWithPin = true;
        }
        pickupLocation = null;
        pickupAddress = null;
        destinationLocation = null;
        destinationAddress = null;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                etFrom.setText("");
                etTo.setText("");
            }
        }, 300);

        driverMarker = null;
        driverPickupMarker = null;
        drivingMarker = null;
        driverDestinationMarker = null;

    }

    private void configureRecycler(){
        rvFromSearch.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvFromSearch.setAdapter(new AddressAdapter(new ArrayList<Address>()));
        ((AddressAdapter)rvFromSearch.getAdapter()).setListener(onFromAddressListener);
        rvToSearch.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvToSearch.setAdapter(new AddressAdapter(new ArrayList<Address>()));
        ((AddressAdapter)rvToSearch.getAdapter()).setListener(onToAddressListener);
    }

    private void addListeners(View rootView){
        rootView.findViewById(R.id.ibCloseFrom).setOnClickListener(onCloseFromClicked);
        rootView.findViewById(R.id.buttonShowMore).setOnClickListener(onShowMoreClicked);
        rlParentOfMyCurrent.setOnClickListener(onCurrentPinListener);
        ibMyLocation.setOnClickListener(onMyLocationClicked);
        etFrom.addTextChangedListener(onEtFromTextWatchListener);
        etTo.addTextChangedListener(onEtToTextWatchListener);
        buttonRequestTaxi.setOnClickListener(onRequestClicked);
        viewForBlockAllClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //do nothing
            }
        });
    }

    @SuppressWarnings("MissingPermission")
    @Override
    protected void onMapReadyForOverride() {
        super.onMapReadyForOverride();
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.setOnMarkerClickListener(this);

        new MapStateListener(googleMap, mapFragment, getActivity()) {
            @Override
            public void onMapTouched() {}

            @Override
            public void onMapReleased() {}

            @Override
            public void onMapUnsettled() {
                hideElements(true);
            }

            @Override
            public void onMapSettled() {
                showElements();
                getNearestDriver(googleMap.getCameraPosition().target);
            }

            @Override
            public void onMapChangeCamera(CameraPosition cameraPosition) {}
        };
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(acceptedDriver != null){
            if(marker.getSnippet() != null && marker.getSnippet().equals(acceptedDriver._id)){
                DriverDetailsDialog.startDialog(getActivity(), acceptedOrder, acceptedDriver);
            }
        }
        return false;
    }

    @Override
    protected void onMyLocationChanged(Location myLocationNew) {
        super.onMyLocationChanged(myLocationNew);
        if(screenStatus == Const.MainUserStatus.START_TRIP){
            drawRoute(Const.DrawRouteUserTypes.STARTED_DRIVE, acceptedOrder, 100);
        }
    }

    private View.OnClickListener onCurrentPinListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            new AsyncTask<Void, Void, String>(){

                LatLng current;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();

                    pbViewInMyCurrent.setVisibility(View.VISIBLE);
                    tvTextViewInMyCurrent.setVisibility(View.INVISIBLE);
                    viewForBlockAllClick.setVisibility(View.VISIBLE);

                    if(pickupLocation == null){
                        isUserSetLocationFromWithPin = true;
                    }else{
                        isUserSetLocationToWithPin = true;
                    }

                    current = googleMap.getCameraPosition().target;

                }

                @Override
                protected String doInBackground(Void... params) {
                    String address = getAddress(current.latitude, current.longitude);
                    return address;
                }

                @Override
                protected void onPostExecute(String address) {
                    super.onPostExecute(address);

                    tvTextViewInMyCurrent.setVisibility(View.VISIBLE);
                    pbViewInMyCurrent.setVisibility(View.GONE);
                    viewForBlockAllClick.setVisibility(View.GONE);

                    if (address == null) {
                        BasicDialog.startOneButtonDialog(getActivity(), getString(R.string.error), getString(R.string.failed_to_get_address));
                        isUserSetLocationFromWithPin = false;
                        return;
                    }

                    if(pickupLocation == null){
                        etFrom.setText(address);
                        pickupLocation = current;
                        pickupAddress = address;

                        tvTextViewInMyCurrent.setText(getString(R.string.set_destination_location_capital));
                        rlMinutes.setVisibility(View.INVISIBLE);
                        viewTimer.setVisibility(View.INVISIBLE);
                    }else{
                        etTo.setText(address);
                        destinationLocation = current;
                        destinationAddress = address;

                        drawRoute(Const.DrawRouteUserTypes.PICKUP_AND_DESTINATION_ROUTE, null, null);
                    }

                }

            }.execute();

        }
    };

    private void drawRoute(final int type, OrderModel model, final Integer paddingMap){
        if(type == Const.DrawRouteUserTypes.PICKUP_AND_DESTINATION_ROUTE){
            removeRoute();
            MapsUtils.calculateRoute(pickupLocation, destinationLocation, new MapsUtils.OnRouteCalculated() {
                @Override
                public void onSuccessCalculate(List<LatLng> list, String distance, long distanceValue, String duration, long durationValue, LatLng northeast, LatLng southwest) {
                    MapsUtils.drawPolyLines(list, googleMap, true);
                    googleMap.addMarker(new MarkerOptions().position(pickupLocation));
                    googleMap.addMarker(new MarkerOptions().position(destinationLocation));
                }
            }, getActivity());
        }else if(type == Const.DrawRouteUserTypes.STARTED_DRIVE){
            final LatLng myLocationLatLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            final LatLng destinationLatLng = new LatLng(model.to.location.get(1), model.to.location.get(0));
            final LatLng startedLatLng = new LatLng(model.from.location.get(1), model.from.location.get(0));

            boolean withZoom = false;
            if(lastOfDrivePolyline == null){
                withZoom = true;
                googleMap.clear();
                TextView tvDistance = (TextView) rlCancelTripLayout.findViewById(R.id.tvDistanceLabel);
                tvDistance.setText(getString(R.string.mileage_));
                rlCancelTripLayout.findViewById(R.id.buttonCancelTrip).setVisibility(View.INVISIBLE);
            }

            final boolean finalWithZoom = withZoom;

            MapsUtils.calculateRoute(myLocationLatLng, destinationLatLng, new MapsUtils.OnRouteCalculated() {
                @Override
                public void onSuccessCalculate(List<LatLng> list, String distance, long distanceValue, String duration, long durationValue, LatLng northeast, LatLng southwest) {
                    final Polyline newPolyline = MapsUtils.drawPolyLines(list, googleMap, finalWithZoom, 100, northeast, southwest);

                    if(drivingMarker != null){
                        if(!MapsUtils.isSameLocation(drivingMarker.getPosition(), myLocationLatLng)){
                            drivingMarker.setRotation(MapsUtils.getBearingForLocation(drivingMarker.getPosition(), myLocation));
                        }
                        drivingMarker.setPosition(myLocationLatLng);
                    }else{
                        drivingMarker = googleMap.addMarker(new MarkerOptions().position(myLocationLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.map_car)));
                    }

                    if(driverDestinationMarker == null){
                        driverDestinationMarker = googleMap.addMarker(new MarkerOptions().position(destinationLatLng));
                    }

                    if(lastOfDrivePolyline != null){
                        lastOfDrivePolyline.remove();
                    }
                    lastOfDrivePolyline = newPolyline;

                    TextView tvDistance = (TextView) rlCancelTripLayout.findViewById(R.id.tvDistance);
                    tvDistance.setText(distance);

                }
            }, getActivity());

//            MapsUtils.getDistanceBetween(startedLatLng, myLocationLatLng, new MapsUtils.OnDistanceCalculated() {
//                @Override
//                public void onSuccessCalculate(String distance) {
//                    TextView tvDistance = (TextView) rlCancelTripLayout.findViewById(R.id.tvDistance);
//                    tvDistance.setText(distance);
//                }
//            }, getActivity());
        }else if(type == Const.DrawRouteUserTypes.ACCEPTED_DRIVE){

            boolean withZoom = false;
            if(driverMarker == null){
                withZoom = true;
            }

            if(acceptedDriver.currentLocation != null){

                final LatLng locationPickup = new LatLng(model.from.location.get(1), model.from.location.get(0));
                final LatLng driverLocation = new LatLng(acceptedDriver.currentLocation.get(1), acceptedDriver.currentLocation.get(0));

                final boolean finalWithZoom = withZoom;
                MapsUtils.calculateRoute(driverLocation, locationPickup, new MapsUtils.OnRouteCalculated() {
                    @Override
                    public void onSuccessCalculate(List<LatLng> list, String distance, long distanceValue, String duration, long durationValue, LatLng northeast, LatLng southwest) {
                        final Polyline newPolyline = MapsUtils.drawPolyLines(list, googleMap, finalWithZoom, 100, northeast, southwest);

                        if(driverMarker != null){
                            if(!MapsUtils.isSameLocation(driverMarker.getPosition(), driverLocation)){
                                driverMarker.setRotation(MapsUtils.getBearingForLocation(driverMarker.getPosition(), driverLocation));
                            }
                            driverMarker.setPosition(driverLocation);
                        }else{
                            final ImageView ivTemp = new ImageView(getActivity());
                            ImageUtils.setImageWithPicassoWithListener(ivTemp, Utils.getAvatarUrl(acceptedDriver), new Callback() {
                                @Override
                                public void onSuccess() {
                                    Bitmap bitmap = MapsUtils.getBitmapWithGreenPinAndCar(getActivity(), ((BitmapDrawable)ivTemp.getDrawable()).getBitmap());
                                    if(acceptedDriver != null) driverMarker = googleMap.addMarker(new MarkerOptions().position(driverLocation).snippet(acceptedDriver._id).icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
                                }

                                @Override
                                public void onError() {
                                    if(acceptedDriver != null) driverMarker = googleMap.addMarker(new MarkerOptions().position(driverLocation).snippet(acceptedDriver._id));
                                }
                            });
                        }

                        if(driverPickupMarker == null){
                            driverPickupMarker = googleMap.addMarker(new MarkerOptions().position(locationPickup));
                        }

                        if(lastPolyline != null){
                            lastPolyline.remove();
                        }
                        lastPolyline = newPolyline;

                        TextView tvDistance = (TextView) rlCancelTripLayout.findViewById(R.id.tvDistance);
                        tvDistance.setText(distance + " (" + duration + ")");

                    }
                }, getActivity());

            }
        }

    }

    private void removeRoute(){
        googleMap.clear();
    }

    private View.OnClickListener onMyLocationClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            gotoMyLocation();

        }
    };

    private View.OnClickListener onCloseFromClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            etFrom.setText("");
            etTo.setText("");
            pickupLocation = null;
            pickupAddress = null;
            destinationLocation = null;
            destinationAddress = null;
            tvTextViewInMyCurrent.setText(getString(R.string.set_pickup_location_capital));
            rlMinutes.setVisibility(View.VISIBLE);
            viewTimer.setVisibility(View.VISIBLE);
            hideRlFromList();
            hideRlToList();

            removeRoute();

        }
    };

    private View.OnClickListener onShowMoreClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ShowMoreInMapDialog.startDialog(getActivity(), googleMap.getCameraPosition().target);
        }
    };

    private TextWatcher onEtFromTextWatchListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(final Editable s) {
            if(isUserSetLocationFromWithPin){
                isUserSetLocationFromWithPin = false;
                return;
            }
            nowCharacterFromLength = s.toString().length();
            if(s.toString().length() > 3){
                final int lastCharacterLength = s.toString().length();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(lastCharacterLength == nowCharacterFromLength){
                            if(nowCharacterFromLength <= 3){
                                hideRlFromList();
                            }else{
                                showRlFromList(s.toString());
                            }
                        }
                    }
                }, WAIT_FOR_SEARCH_WHILE_TYPING);

            }else{
                hideRlFromList();
            }
        }
    };

    private TextWatcher onEtToTextWatchListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(final Editable s) {
            if(isUserSetLocationToWithPin){
                isUserSetLocationToWithPin = false;
                return;
            }
            nowCharacterToLength = s.toString().length();
            if(s.toString().length() > 3){
                final int lastCharacterLength = s.toString().length();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(lastCharacterLength == nowCharacterToLength){
                            if(nowCharacterToLength <= 3){
                                hideRlToList();
                            }else{
                                showRlToList(s.toString());
                            }
                        }
                    }
                }, WAIT_FOR_SEARCH_WHILE_TYPING);

            }else{
                hideRlToList();
            }
        }
    };

    private View.OnClickListener onRequestClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            requestTaxi();
        }
    };

    private AddressAdapter.OnAddressItemClicked onFromAddressListener = new AddressAdapter.OnAddressItemClicked() {
        @Override
        public void onAddressClicked(Address item) {
            isUserSetLocationFromWithPin = true;
            String addressString = Utils.formatAddress(item);
            etFrom.setText(addressString);
            pickupLocation = new LatLng(item.getLatitude(), item.getLongitude());
            pickupAddress = addressString;
            tvTextViewInMyCurrent.setText(getString(R.string.set_destination_location_capital));
            rlMinutes.setVisibility(View.INVISIBLE);
            viewTimer.setVisibility(View.INVISIBLE);
            hideRlFromList();
            Utils.hideKeyboard(etFrom, getActivity());
        }
    };

    private AddressAdapter.OnAddressItemClicked onToAddressListener = new AddressAdapter.OnAddressItemClicked() {
        @Override
        public void onAddressClicked(Address item) {
            isUserSetLocationToWithPin = true;
            String addressString = Utils.formatAddress(item);
            etTo.setText(addressString);
            destinationLocation = new LatLng(item.getLatitude(), item.getLongitude());
            destinationAddress = addressString;
            hideRlToList();
            Utils.hideKeyboard(etTo, getActivity());

            drawRoute(Const.DrawRouteUserTypes.PICKUP_AND_DESTINATION_ROUTE, null, null);
        }
    };

    private String getAddress(double latitude, double longitude) {
        StringBuilder result = new StringBuilder();
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                result.append(Utils.formatAddress(address));
            }
        } catch (IOException e) {
            Log.e("tag", e.getMessage());
            return null;
        }

        return result.toString();
    }

    private void showRlToList(String search){
        ((AddressAdapter)rvToSearch.getAdapter()).setData(searchAddresses(search));
        rlForListTo.setVisibility(View.VISIBLE);
        viewBlackedOut.setVisibility(View.VISIBLE);
        rlFrom.setVisibility(View.INVISIBLE);
    }

    private void hideRlToList(){
        ((AddressAdapter)rvToSearch.getAdapter()).clearData();
        rlForListTo.setVisibility(View.GONE);
        viewBlackedOut.setVisibility(View.GONE);
        rlFrom.setVisibility(View.VISIBLE);
    }

    private void showRlFromList(String search){
        ((AddressAdapter)rvFromSearch.getAdapter()).setData(searchAddresses(search));
        rlForListFrom.setVisibility(View.VISIBLE);
        viewBlackedOut.setVisibility(View.VISIBLE);
        rlTo.setVisibility(View.GONE);
    }

    private void hideRlFromList(){
        ((AddressAdapter)rvFromSearch.getAdapter()).clearData();
        rlForListFrom.setVisibility(View.GONE);
        viewBlackedOut.setVisibility(View.GONE);
        rlTo.setVisibility(View.VISIBLE);
    }

    private List<Address> searchAddresses(String search) {
        try {
            List<Address> addresses = geocoder.getFromLocationName(search, 20);
            return addresses;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    private void hideElements(final boolean showSmallPin){
        if(screenStatus != Const.MainUserStatus.REQUEST_TAXI_SCREEN){
            return;
        }
        if(showSmallPin) smallPin.setVisibility(View.VISIBLE);
        AnimationUtils.fade(rlParentOfMyCurrent, 1, 0, 300, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                rlParentOfMyCurrent.setVisibility(View.GONE);
                if(showSmallPin) smallPin.setVisibility(View.VISIBLE);
            }
        });
        AnimationUtils.fadeThenGoneOrVisible(rlFromTo, 1, 0, 300);
    }

    private void showElements(){
        if(screenStatus != Const.MainUserStatus.REQUEST_TAXI_SCREEN){
            return;
        }
        rlParentOfMyCurrent.setVisibility(View.VISIBLE);
        AnimationUtils.fade(rlParentOfMyCurrent, 0, 1, 300, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                smallPin.setVisibility(View.GONE);
            }
        });
        AnimationUtils.fadeThenGoneOrVisible(rlFromTo, 0, 1, 300);
    }

    private void requestTaxi(){
        if(pickupLocation == null || pickupAddress == null){
            BasicDialog.startOneButtonDialog(getActivity(), getString(R.string.error), getString(R.string.please_set_pick_up_location));
            return;
        }
        if(destinationLocation == null || destinationAddress == null){
            BasicDialog.startOneButtonDialog(getActivity(), getString(R.string.error), getString(R.string.please_set_destination_location));
            return;
        }

        SeatsNumberDialog.startDialog(getActivity(), new SeatsNumberDialog.OnItemClickedListener() {
            @Override
            public void onClicked(SeatsNumberDialog dialog, int numberOfSeats) {
                dialog.dismiss();
                RequestSentDialog.startDialog(getActivity(), pickupLocation, destinationLocation, pickupAddress, destinationAddress, numberOfSeats);
            }
        });

    }

    public void cancelTripClearMap(){
        screenStatus = Const.MainUserStatus.REQUEST_TAXI_SCREEN;
        etFrom.setText("");
        etTo.setText("");
        pickupLocation = null;
        pickupAddress = null;
        destinationLocation = null;
        destinationAddress = null;
        tvTextViewInMyCurrent.setText(getString(R.string.set_pickup_location_capital));
        rlMinutes.setVisibility(View.VISIBLE);
        viewTimer.setVisibility(View.VISIBLE);

        acceptedDriver = null;
        acceptedOrder = null;
        driverMarker = null;
        driverPickupMarker = null;
        lastPolyline = null;
        drivingMarker = null;
        driverDestinationMarker = null;
        lastOfDrivePolyline = null;
        googleMap.clear();

        hideRlFromList();
        hideRlToList();

        removeRoute();

        MapsUtils.setMapParentLayoutParams(null, 0, layoutForMap);
        AnimationUtils.translateY(rlCancelTripLayout, 0, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics()), 300, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                rlCancelTripLayout.setVisibility(View.GONE);

                showRequestTaxiScreenElements();
            }
        });
    }

    private void hideRequestTaxiScreenElements(){
        rlFromTo.setVisibility(View.GONE);
        smallPin.setVisibility(View.GONE);
        rlParentOfMyCurrent.setVisibility(View.GONE);
        buttonRequestTaxi.setVisibility(View.GONE);
        ibMyLocation.setVisibility(View.GONE);
    }

    private void showRequestTaxiScreenElements(){
        rlFromTo.setVisibility(View.VISIBLE);
        rlParentOfMyCurrent.setVisibility(View.VISIBLE);
        smallPin.setVisibility(View.VISIBLE);
        buttonRequestTaxi.setVisibility(View.VISIBLE);
        ibMyLocation.setVisibility(View.VISIBLE);
        AnimationUtils.fade(rlFromTo, 0, 1, 300, null);
        AnimationUtils.fade(rlParentOfMyCurrent, 0, 1, 300, null);
        AnimationUtils.fade(smallPin, 0, 1, 300, null);
        AnimationUtils.fade(buttonRequestTaxi, 0, 1, 300, null);
        AnimationUtils.fade(ibMyLocation, 0, 1, 300, null);
    }

    private void showTripLayout(){
        rlCancelTripLayout.setVisibility(View.VISIBLE);
        TextView tvDistance = (TextView) rlCancelTripLayout.findViewById(R.id.tvDistanceLabel);
        tvDistance.setText(getString(R.string.distance_between__driver_and_you_));
        TextView tvDistanceValue = (TextView) rlCancelTripLayout.findViewById(R.id.tvDistance);
        tvDistanceValue.setText("");
        rlCancelTripLayout.findViewById(R.id.buttonCancelTrip).setVisibility(View.VISIBLE);
        AnimationUtils.translateY(rlCancelTripLayout, 0, 0, 0, null);
        MapsUtils.setMapParentLayoutParams(null, rlCancelTripLayout.getHeight(), layoutForMap);

        rlCancelTripLayout.findViewById(R.id.buttonCancelTrip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ignoreOrder(acceptedOrder);
            }
        });
    }

    public void onTripAccepted(OrderModel order) {
        screenStatus = Const.MainUserStatus.ORDER_ACCEPTED;
        acceptedOrder = order;

        googleMap.clear();
        hideRequestTaxiScreenElements();
        showTripLayout();

        checkOrderStatus(order);
    }

    private void checkOrderStatus(final OrderModel orderModel){
        if(screenStatus == Const.MainUserStatus.REQUEST_TAXI_SCREEN){
            return;
        }
        if(stopChecking){
            return;
        }
        PostCheckOrderStatusModel postModel = new PostCheckOrderStatusModel(orderModel._id);
        UserRetroApiInterface retroApiInterface = getRetrofit().create(UserRetroApiInterface.class);
        Call<CheckOrderStatusModel> call = retroApiInterface.checkOrderStatus(postModel, UserSingleton.getInstance().getUser().token_new);
        call.enqueue(new CustomResponse<CheckOrderStatusModel>(getActivity(), false, false) {

            @Override
            public void onCustomSuccess(Call<CheckOrderStatusModel> call, Response<CheckOrderStatusModel> response) {
                super.onCustomSuccess(call, response);

                LogCS.i("LOG", "Order status: " + response.body().data.orderStatus);

                if(screenStatus == Const.MainUserStatus.REQUEST_TAXI_SCREEN){
                    return;
                }

                if(response.body().data.orderStatus == Const.OrderStatusTypes.CANCELED){
                    BasicDialog.startOneButtonDialog(getActivity(), getString(R.string.info), getString(R.string.driver_canceled_order));
                    cancelTripClearMap();
                }else if(response.body().data.orderStatus == Const.OrderStatusTypes.FINISHED_DRIVE){
                    if(acceptedDriver == null){
                        acceptedDriver = response.body().data.driver;
                    }
                    LastTripDialogLikeActivity.startActivity(getActivity(), acceptedOrder, acceptedDriver);
                    cancelTripClearMap();
                }else if(response.body().data.orderStatus == Const.OrderStatusTypes.STARTED_DRIVE){

                    if(acceptedDriver == null){
                        acceptedDriver = response.body().data.driver;
                    }

                    drawRoute(Const.DrawRouteUserTypes.STARTED_DRIVE, orderModel, 100);

                    checkAfterFiveSeconds();

                }else{
                    acceptedDriver = response.body().data.driver;
                    drawRoute(Const.DrawRouteUserTypes.ACCEPTED_DRIVE, orderModel, 100);

                    checkAfterFiveSeconds();
                }

            }

            @Override
            public void onCustomFailed(Call<CheckOrderStatusModel> call, Response<CheckOrderStatusModel> response) {
                super.onCustomFailed(call, response);
                checkOrderStatus(orderModel);
            }
        });
    }

    private void ignoreOrder(final OrderModel order){

        PostCancelTripModel postModelCancel = new PostCancelTripModel(order._id, Const.CancelTypes.USER_CANCELED, "");
        final DriverRetroApiInterface retroApiInterface = getRetrofit().create(DriverRetroApiInterface.class);
        Call<BaseModel> call = retroApiInterface.cancelTrip(postModelCancel, UserSingleton.getInstance().getUser().token_new);
        call.enqueue(new CustomResponse<BaseModel>(getActivity(), true, true) {

            @Override
            public void onCustomSuccess(Call<BaseModel> call, Response<BaseModel> response) {
                super.onCustomSuccess(call, response);

                screenStatus = Const.MainUserStatus.REQUEST_TAXI_SCREEN;
                cancelTripClearMap();

            }

            @Override
            public void onTryAgain(Call<BaseModel> call, Response<BaseModel> response) {
                super.onTryAgain(call, response);
                ignoreOrder(order);
            }

        });
    }

    private void checkAfterFiveSeconds(){
        LogCS.i("LOG", "Start checker screen: " + screenStatus);
        handlerForCheck.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(acceptedOrder != null){
                    checkOrderStatus(acceptedOrder);
                }
            }
        }, 5000);
    }

    private Call<NearestDriverResponse> call = null;
    private void getNearestDriver(final LatLng startLatLng){
        tvTextViewNearestDriverMinutes.setText("-");
        if(call != null){
            call.cancel();
        }
        PostLatLngModel postModel = new PostLatLngModel(myLocation.getLatitude(), myLocation.getLongitude());
        DriverRetroApiInterface api = getRetrofit().create(DriverRetroApiInterface.class);
        call = api.getNearestDriver(postModel, UserSingleton.getInstance().getUser().token_new);
        call.enqueue(new CustomResponse<NearestDriverResponse>(getActivity(), false, false) {
            @Override
            public void onCustomSuccess(Call<NearestDriverResponse> call, Response<NearestDriverResponse> response) {
                super.onCustomSuccess(call, response);

                UserModel nearestDriver = response.body().data.driver;
                if(nearestDriver.currentLocation != null){
                    LatLng driverLatLng = new LatLng(nearestDriver.currentLocation.get(1), nearestDriver.currentLocation.get(0));
                    MapsUtils.getTimeBetween(startLatLng, driverLatLng, new MapsUtils.OnTimeCalculated() {
                        @Override
                        public void onSuccessCalculate(String timeString, long timeSeconds) {
                            tvTextViewNearestDriverMinutes.setText(String.valueOf(timeSeconds / 60));
                        }
                    }, getActivity());
                }

            }
        });
    }

}
