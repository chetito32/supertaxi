//
//  UserRequestReceivedViewController.swift
//  SuperTaxi
//
//  Created by Administrator on 6/24/16.
//  Copyright © 2016 Jensen Pich. All rights reserved.
//

import UIKit
import SwiftyJSON
import MapKit

class UserRequestReceivedViewController: UIViewController {
    
    @IBOutlet var contactView: UIView!
    @IBOutlet var txtName: UILabel!
    @IBOutlet var txtDistance: UILabel!
    @IBOutlet var txtCarType: UILabel!
    @IBOutlet var txtCarRegistraion: UILabel!
    @IBOutlet var txtFeeStart: UILabel!
    @IBOutlet var avatar: UIImageView!
    
    var driver: DriverInfoModel!
    var driverFileId: String!
    var driverLocation: [JSON]!
    var from: CLLocationCoordinate2D!
    var to: CLLocationCoordinate2D!
    var distance: String!
    var driverPhoneNumber: String!
    var driverId: String!
    
    var orderId: String!
    
    let UserInformation = NSUserDefaults.standardUserDefaults()
    var apiManager: ApiManager!

    override func viewDidLoad() {
        super.viewDidLoad()

        contactView.layer.borderWidth = 1
        contactView.layer.borderColor = Colors.darkBlue(1).CGColor
        
        if driverFileId != nil {
            avatar.load(Api.IMAGE_URL + driverFileId, placeholder: UIImage(named: "taxi_driver"))
        }
        
        avatar.layer.cornerRadius = avatar.frame.size.height/2
        avatar.clipsToBounds = true
        
        if driver != nil {
            txtName.text = driver.name
            txtCarType.text = driver.car_type
            txtCarRegistraion.text = driver.car_registration
            txtFeeStart.text = String(driver.fee_start)
        }
        
        apiManager = ApiManager()
        
        txtDistance.text = distance
        
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    @IBAction func showOnMap(sender: AnyObject) {
        let viewController = self.storyboard?.instantiateViewControllerWithIdentifier("UserMapWithTaxiID") as? UserLongPressViewController
        viewController?.driver = driver
        viewController?.driverFileId = driverFileId
        viewController?.driverLocation = driverLocation
        viewController?.from = from
        viewController?.to = to
        viewController?.orderId = orderId
        viewController?.driverId = driverId
        self.navigationController?.pushViewController(viewController!, animated: true)
    }
    
    @IBAction func cancel(sender: AnyObject) {
        apiManager.cancelOrder(UserInformation.stringForKey(UserDetails.TOKEN)!, id: orderId, type: 1, reason: "Neznam jos")
        self.navigationController!.popViewControllerAnimated(true)
    }
    
    @IBAction func btnCallDriver(sender: AnyObject) {
        UIApplication.sharedApplication().openURL(NSURL(string: "telprompt://" + driverPhoneNumber)!)
    }
    
    
    
}
