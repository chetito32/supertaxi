//
//  ForgotPasswordViewController.swift
//  SuperTaxi
//
//  Created by Administrator on 6/25/16.
//  Copyright © 2016 Jensen Pich. All rights reserved.
//

import UIKit

class ForgotPasswordViewController: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    @IBAction func onCancelClick(sender: AnyObject) {
        self.navigationController?.popViewControllerAnimated(true)
    }


}
