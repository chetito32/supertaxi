var express = require('express');
var router = express.Router();
var _ = require('lodash');
var async = require('async');

var pathTop = "../../../../";

var Const = require( pathTop + "lib/consts");
var Utils = require( pathTop + 'lib/utils');
var tokenChecker = require( pathTop + 'lib/authAPI');

var BackendBase = require('../BackendBase');

var OrderModel = require(pathTop + 'Models/Order');
var UserModel = require(pathTop + 'Models/User');

var GetOpenOrderController = function(){
}

_.extend(GetOpenOrderController.prototype,BackendBase.prototype);

GetOpenOrderController.prototype.init = function(app){
        
    var self = this;

   /**
     * @api {post} /api/v1/order/getOpenOrder Get Open Order 
     * @apiName Get Open Order
     * @apiGroup WebAPI
     * @apiDescription This API receives JSON request. Get open order for taxi driver
     * 
     * @apiHeader {String} access-token Users unique access-token.
     * 
     * @apiParam {Decimal} lat (Required) Current driver latitude
     * @apiParam {Decimal} lon (Required) Current driver longitude
    
     * @apiError UnknownError 6000000
     * @apiError ParamErrorLatitudeDriver 6000024
     * @apiError ParamErrorLongitudeDriver 6000025

     * 
     * @apiSuccessExample Success-Response:
        { 
            code: 1,
            time: 1468314014075,
            data: { 
                order: { 
                    userId: '5784a21a773cfd5e2d58e770',
                    createOrderTs: 1468310044176,
                    crewNum: 4,
                    _id: 5784a21c773cfd5e2d58e771,
                    __v: 0,
                    to: { 
                        lat: 235.45454545,
                        lon: 100.45454545,
                        address: 'Bučarova 13 Zagreb' 
                    },
                    from: { 
                        lat: 99.45454545, 
                        lon: 70.45445, 
                        address: 'Siget 11 Zagreb' 
                    },
                    user: { 
                        __v: 0,
                        _id: 5784a21a773cfd5e2d58e770,
                        created: 1468310042789,
                        email: 'testn4eH1@test.com',
                        password: '*****',
                        telNum: '+385981234567',
                        token: '*****',
                        token_generated: 1468310042935,
                        avatar: { 
                            fileid: '7osIqcIYQeCcxoC3FQXwreu0Sj07JkvT',
                            thumbfileid: 'tIXtp5R6z0aylAYv97ktDRQVK209pRAs' 
                        },
                        user: { 
                            name: 'test', 
                            age: 0, 
                            note: null 
                        }
                    }
                }
            }
        }

     **/

    router.post('', tokenChecker, (request, response) => {

        var orderModel = OrderModel.get();
        var userModel = UserModel.get();

        async.waterfall([

            (done) => {
                    
                done(null, { fields: request.body });

            },
            (result, done) => {

                done(self.validation(result.fields), result);

            },
            (result, done) => {

                // get open order
                orderModel.findOne({
                    acceptOrderTs: { $exists: false },
                    cancelOrderOrTrip: { $exists: false }
                }, (err, findResult) => {

                    if (findResult) 
                        result.order = findResult.toObject();
                    
                    done(err, result);

                });

            },
            (result, done) => {

                if (!result.order) {
                    done(null, result);
                    return;
                }

                // get user who request order
                userModel.findOne({ _id: result.order.userId }, (err, findResult) => {

                    result.order.user = findResult;
                    done(err, result);
                    
                });

            }
        ],
        (err, result) => {

            if (err) {

                if (err.handledError) {

                    self.successResponse(response, err.handledError);

                } else {

                    console.log(err);

                    self.successResponse(response, Const.responsecodeUnknownError);

                }

            } else {

                delete result.fields;
                self.successResponse(response, Const.responsecodeSucceed, result);

            }

        });

    });

    return router;

}


GetOpenOrderController.prototype.validation = function(fields) {

    if (!_.isNumber(fields.lat)) {
        return { handledError: Const.responsecodeParamErrorLatitudeDriver };
    }
    
    if (!_.isNumber(fields.lon)) {
        return { handledError: Const.responsecodeParamErrorLongitudeDriver };
    }

    return null;
}

module["exports"] = new GetOpenOrderController();
