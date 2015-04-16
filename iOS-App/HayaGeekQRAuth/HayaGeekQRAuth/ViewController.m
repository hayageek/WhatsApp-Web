//
//  ViewController.m
//  HayaGeekQRAuth
//
//  Created by Ravishanker Kusuma on 24/3/15.
//  Copyright (c) 2015 Helius Technologies Pte Ltd. All rights reserved.
//

#import "ViewController.h"

@interface ViewController ()

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    [self.scanButton setHidden:TRUE];
    [self.label2  setHidden:TRUE];
    
    FBLoginView *loginview = [[FBLoginView alloc] init];
    
    loginview.frame = CGRectOffset(loginview.frame, 35, 25);

    loginview.delegate = self;
    [self.view addSubview:loginview];
    
    [loginview sizeToFit];
    
    
    
    // Do any additional setup after loading the view, typically from a nib.
}


-(IBAction) startScanning:(id)sender
{
    self.captureSession = [[AVCaptureSession alloc] init];
    
    AVCaptureDevice *videoCaptureDevice = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo];
    NSError *error = nil;
    AVCaptureDeviceInput *videoInput = [AVCaptureDeviceInput deviceInputWithDevice:videoCaptureDevice error:&error];
    if(videoInput)
        [self.captureSession addInput:videoInput];
    else
        NSLog(@"Error: %@", error);
    
    AVCaptureMetadataOutput *metadataOutput = [[AVCaptureMetadataOutput alloc] init];
    [self.captureSession addOutput:metadataOutput];
    [metadataOutput setMetadataObjectsDelegate:self queue:dispatch_get_main_queue()];
    [metadataOutput setMetadataObjectTypes:@[AVMetadataObjectTypeQRCode, AVMetadataObjectTypeEAN13Code]];
    
    previewLayer = [[AVCaptureVideoPreviewLayer alloc] initWithSession:self.captureSession];
    previewLayer.frame = self.view.layer.bounds;
    [self.view.layer addSublayer:previewLayer];
    
    [self.captureSession startRunning];

}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)captureOutput:(AVCaptureOutput *)captureOutput didOutputMetadataObjects:(NSArray *)metadataObjects fromConnection:(AVCaptureConnection *)connection
{
    for(AVMetadataObject *metadataObject in metadataObjects)
    {
        AVMetadataMachineReadableCodeObject *readableObject = (AVMetadataMachineReadableCodeObject *)metadataObject;
        if([metadataObject.type isEqualToString:AVMetadataObjectTypeQRCode])
        {
            NSLog(@"QR Code = %@", readableObject.stringValue);
            
            NSString * qrCode = readableObject.stringValue;
            if(qrCode != nil && qrCode.length > 1)
            {
                
                [self.captureSession stopRunning];
                [previewLayer removeFromSuperlayer];
                
                NSString * accessToken =[[[FBSession activeSession] accessTokenData] accessToken];
                [self sendStatusToServer:qrCode :accessToken];
                
                
            }
        }
        else if ([metadataObject.type isEqualToString:AVMetadataObjectTypeEAN13Code])
        {
            NSLog(@"EAN 13 = %@", readableObject.stringValue);
        }
    }
}


#pragma mark - FBLoginViewDelegate

- (void)loginViewShowingLoggedInUser:(FBLoginView *)loginView {
    
    [self.label1  setHidden:TRUE];
    [self.label2  setHidden:false];
    [self.scanButton setHidden:false];
    NSLog(@"Logged In");
}

- (void)loginViewFetchedUserInfo:(FBLoginView *)loginView
                            user:(id<FBGraphUser>)user {
    self.profilePic.profileID = user.objectID;
    self.loggedInUser = user;
    NSLog(@"User Info :%@",user);
}

- (void)loginViewShowingLoggedOutUser:(FBLoginView *)loginView {
    // test to see if we can use the share dialog built into the Facebook application
    FBLinkShareParams *p = [[FBLinkShareParams alloc] init];
    p.link = [NSURL URLWithString:@"http://developers.facebook.com/ios"];
    BOOL canShareFB = [FBDialogs canPresentShareDialogWithParams:p];
    BOOL canShareiOS6 = [FBDialogs canPresentOSIntegratedShareDialogWithSession:nil];
    BOOL canShareFBPhoto = [FBDialogs canPresentShareDialogWithPhotos];
    self.profilePic.profileID = nil;
    self.loggedInUser = nil;
    
    NSLog(@"Logout is done");
    
    [self.label1  setHidden:false];
    [self.label2  setHidden:TRUE];
    [self.scanButton setHidden:TRUE];

    
    
}

- (void)loginView:(FBLoginView *)loginView handleError:(NSError *)error {
    // see https://developers.facebook.com/docs/reference/api/errors/ for general guidance on error handling for Facebook API
    // our policy here is to let the login view handle errors, but to log the results
    NSLog(@"FBLoginView encountered an error=%@", error);
}

#

-(void) sendStatusToServer:(NSString *)uuid :(NSString*)accessToken
{
    NSString * url =@"http://nodejs-whatsappauth.rhcloud.com/auth";
    
    NSString * formData = [NSString stringWithFormat:@"{\"uuid\":\"%@\",\"access_token\":\"%@\"}",uuid,accessToken];
    
    NSLog(@"Sending Request: %@",formData);
    [self post:url formData:formData completion:^(id jsonObj,NSError *error) {
        
            if(error == nil)
            {
                NSLog(@"Response: %@",jsonObj);
            }
        else
        {
                NSLog(@"ERROR: %@",error);
        }
        
        
    }];
    
}
-(void) post:(NSString*) urlStr formData:(NSString*)paramsStr
  completion:(void(^)(id jsonResp,NSError *err))callback
{
    
    
    NSURLSessionConfiguration *defaultConfigObject = [NSURLSessionConfiguration defaultSessionConfiguration];
    NSURLSession* defaultSession = [NSURLSession
                                    sessionWithConfiguration: defaultConfigObject
                                    delegate:self delegateQueue: [NSOperationQueue mainQueue]];
    
    
    NSURL * url = [NSURL URLWithString:urlStr];
    
    NSMutableURLRequest * urlRequest = [NSMutableURLRequest requestWithURL:url];


    [urlRequest setTimeoutInterval:60];
    [urlRequest setHTTPMethod:@"POST"];
    [urlRequest setHTTPBody:[paramsStr dataUsingEncoding:NSUTF8StringEncoding]];
    
    NSLog(@"Sending Request to %@",url);
    NSLog(@"FormData %@",paramsStr);
    NSURLSessionDataTask * dataTask =[defaultSession
                                      dataTaskWithRequest:urlRequest
                                      completionHandler:^(NSData *data, NSURLResponse *response, NSError *error)
                                      {
                                          NSLog(@"Response:%@ %@\n", response, error);
                                          NSLog(@"Request:%@ %@\n", urlRequest, error);
                                          if(error != nil)
                                          {
                                              
                                              //convert to json.
                                              NSError * jsonErr=nil;
                                              id jsonObj= [NSJSONSerialization JSONObjectWithData:data options:kNilOptions
                                                                                            error:&jsonErr];
                                              if(jsonErr != nil)
                                              {
                                                  callback(jsonObj,error);
                                              }
                                              else
                                                  callback(nil,jsonErr);

                                              
                                          }
                                         else
                                            callback(nil,error);
                                      }];
    [dataTask resume];
    
}



@end
