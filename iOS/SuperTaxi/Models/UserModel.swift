//
//  UserModel.swift
//  SuperTaxi
//
//  Created by Jurica Mlinarić on 13/07/16.
//  Copyright © 2016 Jensen Pich. All rights reserved.
//

import Foundation
import ObjectMapper

public class UserModel: Mappable {

    var _v: NSInteger!
    var _id: String!
    var created: CLong!
    var email: String!
    var password: String!
    var token: String!
    var token_generated: String!
    var driver: DriverInfoModel!
    var user: UserInfoModel!
    
    required public init?(_ map: Map) {
        
    }
    
    public func mapping(map: Map) {
        _v <- map["_v"]
        _id <- map["_id"]
        created <- map["created"]
        email <- map["email"]
        password <- map["password"]
        token <- map["token"]
        token_generated <- map["token_generated"]
        driver <- map["driver"]
        user <- map["user"]
        
    }

}
