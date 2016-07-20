package clover_studio.com.supertaxi.api.retrofit;

import clover_studio.com.supertaxi.models.BaseModel;
import clover_studio.com.supertaxi.models.DriverListResponse;
import clover_studio.com.supertaxi.models.post_models.PostCallTaxiModel;
import clover_studio.com.supertaxi.models.post_models.PostLatLngModel;
import clover_studio.com.supertaxi.utils.Const;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Created by ubuntu_ivo on 10.02.16..
 */
public interface DriverRetroApiInterface {

    @POST(Const.Server.GET_DRIVER_LIST)
    Call<DriverListResponse> getDriverList(@Body PostLatLngModel postModel, @Header(Const.HeadersParams.ACCESS_TOKEN) String token);

    @POST(Const.Server.CALL_TAXI)
    Call<BaseModel> callTaxi(@Body PostCallTaxiModel postModel, @Header(Const.HeadersParams.ACCESS_TOKEN) String token);


}
