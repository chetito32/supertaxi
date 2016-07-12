//
//  TaxiUserLocationViewController.swift
//  SuperTaxi
//
//  Created by Administrator on 6/24/16.
//  Copyright © 2016 Jensen Pich. All rights reserved.
//

import UIKit
import MapKit

class TaxiUserLocationViewController: UIViewController, MKMapViewDelegate {

    @IBOutlet weak var viewAlert: UIView!
    @IBOutlet weak var btnStartTrip: UIButton!
    @IBOutlet weak var mapView: MKMapView!
    
    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
        viewAlert.layer.cornerRadius = 5
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    @IBAction func onStartTripClick(sender: AnyObject) {
        let blurEffect = UIBlurEffect(style: UIBlurEffectStyle.Dark)
        let blurEffectView = UIVisualEffectView(effect: blurEffect)

        blurEffectView.frame = view.bounds
        blurEffectView.autoresizingMask = [.FlexibleWidth, .FlexibleHeight] // for supporting device rotation
        blurEffectView.layer.opacity = 0.8
        view.addSubview(blurEffectView)
        blurEffectView.addSubview(viewAlert)
        viewAlert.hidden = false
    }
}
