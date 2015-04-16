//
//  ViewController.h
//  HayaGeekQRAuth
//
//  Created by Ravishanker Kusuma on 24/3/15.
//  Copyright (c) 2015 Helius Technologies Pte Ltd. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>
#import <FacebookSDK/FacebookSDK.h>

@interface ViewController : UIViewController<AVCaptureMetadataOutputObjectsDelegate,FBLoginViewDelegate,NSURLSessionDelegate>
{
    AVCaptureVideoPreviewLayer *previewLayer;
}
@property (strong) AVCaptureSession *captureSession;
@property (strong, nonatomic) id<FBGraphUser> loggedInUser;
@property (strong, nonatomic) IBOutlet FBProfilePictureView *profilePic;

@property (strong, nonatomic) IBOutlet UIButton *scanButton;
@property (strong, nonatomic) IBOutlet UILabel *label1;
@property (strong, nonatomic) IBOutlet UITextView *label2;

@end

