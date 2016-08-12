package clover_studio.com.supertaxi.dialog;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import clover_studio.com.supertaxi.R;
import clover_studio.com.supertaxi.api.retrofit.CustomResponse;
import clover_studio.com.supertaxi.api.retrofit.DriverRetroApiInterface;
import clover_studio.com.supertaxi.base.BaseActivity;
import clover_studio.com.supertaxi.models.BaseModel;
import clover_studio.com.supertaxi.models.OrderModel;
import clover_studio.com.supertaxi.models.post_models.PostCancelTripModel;
import clover_studio.com.supertaxi.singletons.UserSingleton;
import clover_studio.com.supertaxi.utils.AnimationUtils;
import clover_studio.com.supertaxi.utils.Const;
import clover_studio.com.supertaxi.utils.ImageUtils;
import clover_studio.com.supertaxi.utils.Utils;
import retrofit2.Call;
import retrofit2.Response;

public class DialogUserRequestDetails extends Dialog {

    RelativeLayout parentLayout;
    RelativeLayout mainLayout;

    private ImageView ivAvatar;
    private TextView tvName;
    private TextView tvAge;
    private TextView tvFrom;
    private TextView tvTo;
    private TextView tvNote;
    private LinearLayout llStarLayout;

    private OrderModel order;
    private int mainDriverStatus;

    public static DialogUserRequestDetails startDialog(Context context, OrderModel order, int driverStatus) {
        return new DialogUserRequestDetails(context, order, driverStatus);
    }

    public DialogUserRequestDetails(Context context, OrderModel order, int driverStatus) {
        super(context, R.style.Theme_Dialog_no_dim);

        setOwnerActivity((Activity) context);
        setCancelable(true);
        setCanceledOnTouchOutside(true);

        this.order = order;
        this.mainDriverStatus = driverStatus;

        show();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_user_request_details);

        parentLayout = (RelativeLayout) findViewById(R.id.parentLayout);
        mainLayout = (RelativeLayout) findViewById(R.id.mainLayout);

        ivAvatar = (ImageView) findViewById(R.id.ivAvatarInUserDetails);
        tvName = (TextView) findViewById(R.id.tvName);
        tvAge = (TextView) findViewById(R.id.tvAge);
        tvFrom = (TextView) findViewById(R.id.tvFromValue);
        tvTo = (TextView) findViewById(R.id.tvToValue);
        tvNote = (TextView) findViewById(R.id.tvNoteValue);
        llStarLayout = (LinearLayout) findViewById(R.id.ratingStars);

        int rating = 2;
        if (rating > llStarLayout.getChildCount()) {
            rating = llStarLayout.getChildCount();
        }
        for (int i = 0; i < rating; i++) {
            llStarLayout.getChildAt(i).setSelected(true);
        }

        findViewById(R.id.closeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUserRequestDetails.this.dismiss();
            }
        });

        findViewById(R.id.buttonCancelTrip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelTripApi();
            }
        });

        if(mainDriverStatus == Const.MainDriverStatus.START_TRIP){
            findViewById(R.id.buttonCancelTrip).setVisibility(View.INVISIBLE);
        }

        if(order.user != null){
            ImageUtils.setImageWithPicasso(ivAvatar, Utils.getAvatarUrl(order.user));
            if(order.user.user != null){
                tvName.setText(order.user.user.name);
                tvAge.setText(getOwnerActivity().getResources().getString(R.string.age) + ": " + order.user.user.age);
                tvNote.setText(order.user.user.note);
            }
        }

        tvFrom.setText(order.from.address);
        tvTo.setText(order.from.address);

    }


    @Override
    public void dismiss() {
        AnimationUtils.fade(parentLayout, 1, 0, 300, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                DialogUserRequestDetails.super.dismiss();
            }
        });

    }

    @Override
    public void show() {
        super.show();
        AnimationUtils.fade(parentLayout, 0, 1, 300, null);
    }

    private void cancelTripApi(){
        PostCancelTripModel postModel = new PostCancelTripModel(order._id, Const.UserType.USER_TYPE_USER, "");

        if(getOwnerActivity() instanceof BaseActivity){

            DriverRetroApiInterface retroApiInterface = ((BaseActivity)getOwnerActivity()).getRetrofit().create(DriverRetroApiInterface.class);
            Call<BaseModel> call = retroApiInterface.cancelTrip(postModel, UserSingleton.getInstance().getUser().token_new);
            call.enqueue(new CustomResponse<BaseModel>(getOwnerActivity(), true, true) {

                @Override
                public void onCustomSuccess(Call<BaseModel> call, Response<BaseModel> response) {
                    super.onCustomSuccess(call, response);

                    LocalBroadcastManager.getInstance(getOwnerActivity()).sendBroadcast(new Intent(Const.ReceiverIntents.ON_CANCEL_TRIP).putExtra(Const.Extras.ORDER_MODEL, order));
                    DialogUserRequestDetails.this.dismiss();

                }

                @Override
                public void onTryAgain(Call<BaseModel> call, Response<BaseModel> response) {
                    super.onTryAgain(call, response);
                    cancelTripApi();
                }
            });

        }
    }

}