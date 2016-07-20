var should = require('should');
var request = require('supertest');
var app = require('../mainTest');

describe('WEB API', function () {

    var req, res;

    describe('/order/cancel POST', function () {
        
        it('success cancel order', (done) => {

            request(app)
                .post('/api/v1/order/call')
                .set('access-token', global.user1.token)
                .send({
                    latFrom: 89.45454545,
                    lonFrom: 150.45445,
                    addressFrom: 'Test adresa start',
                    latTo: 88.45454545,
                    lonTo: 149.94595,
                    addressTo: 'Proba destinacija',
                    crewNum: 4
                })
                .end(function (err, res) {

                if (err) {
                    throw err;
                }

                res.body.code.should.be.exactly(1);
                res.body.should.have.property('data');
                
                request(app)
                    .post('/api/v1/order/getOpenOrder')
                    .set('access-token', global.user1.token)
                    .send({
                        lat: 99.45454545,
                        lon: 70.45445
                    })
                    .end(function (err, res) {

                    if (err) {
                        throw err;
                    }

                    res.body.code.should.be.exactly(1);
                    res.body.should.have.property('data');
                    res.body.data.should.have.property('order');
                    
                    global.order = res.body.data.order;

                    request(app)
                        .post('/api/v1/order/cancel')
                        .set('access-token', global.user1.token)
                        .send({
                            orderId: global.order._id,
                            type: 1,
                            reason: "Odustao"
                        })
                        .end((err, res) => {

                        if (err) {
                            throw err;
                        }

                        res.body.code.should.be.exactly(1);
                        res.body.should.have.property('data');
                        
                        done();
                    
                    });  
                
                });
            
            });  
            
        });

        it('wrong order id', function (done) {

            request(app)
                .post('/api/v1/order/cancel')
                .set('access-token', global.user1.token)
                .send({
                    orderId: "test"
                })
                .end(function (err, res) {

                if (err) {
                    throw err;
                }

                res.body.code.should.be.exactly(6000026);
                
                done();
            
            });   
            
        });

        it('wrong type', function (done) {

            request(app)
                .post('/api/v1/order/cancel')
                .set('access-token', global.user1.token)
                .send({
                    orderId: global.order._id,
                    type: "test"
                })
                .end(function (err, res) {

                if (err) {
                    throw err;
                }

                res.body.code.should.be.exactly(6000011);
                
                done();
            
            });   
            
        });

        it('driver already started drive or order is canceled', function (done) {

            request(app)
                .post('/api/v1/order/cancel')
                .set('access-token', global.user1.token)
                .send({
                    orderId: global.order._id,
                    type: 1
                })
                .end(function (err, res) {

                if (err) {
                    throw err;
                }

                res.body.code.should.be.exactly(6000029);
                
                done();
            
            });   
            
        });

    });

});