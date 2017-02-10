# Image To Object Count Estimator

## Description
I develop a smartphone application for the Android platform with access to a cloud-based machine learning (ML) service that can estimate the amount of crop on a bush or tree from an image. 
The development of an image-to-object-count estimation system called Estimage is presented. The Estimage system consists
of an Android client application for user interaction, a PHP server application for
request handling, an Octave program for image normalization, and an open-source
ML software package called ilastik that applies a predictive model to an image. The
functionality of the mobile client application is tested and the system satisfies all of
the functional requirements and most of the non-functional requirements. The system
is tested on the images of coins on a table, logs stacked in a pile, and blueberries on
the bush. Other detail information is in my [thesis](./honors_thesis.pdf).

## Introduction to Use

This Android application has two interfaces to start with. 

The HOME interface allows users to create an image record by taking a picture with camera or picking an image from local directory.

The IMAGES interface shows up a list of image records that users created previously, and each item displays title, creation date and estimation status.

We also can start an estimation on any image via the start button of each image record on the right. Once started, the selected image will be sent to the PHP server and be estimated by a pre-trained machine learning predictive model.

<img src="screenshots/implement_interface_home.png" width="250">
<img src="screenshots/implement_interface_image_repository.png" width="250">

The INFO interface can be accessed by clicking on any image record in the IMAGES interface.

The EDIT interface allows edition on an image record.

<img src="screenshots/implement_interface_record_info.png" width="250">
<img src="screenshots/implement_interface_edit_record.png" width="250">

Once we click on the image on the INFO interface, the following interface shows up, and we can see how good the image is estimated. 

Dark color means irrelated pixels, while white color means target pixels.

<img src="screenshots/implement_interface_image_display_original.png" width="250">
<img src="screenshots/implement_interface_image_display_mixed.png" width="250">
<img src="screenshots/implement_interface_image_display_density.png" width="250">

We also can zoom in or out the selected image to see the details.

<img src="screenshots/implement_interface_image_display_original_zoom.png" width="250">
<img src="screenshots/implement_interface_image_display_mixed_zoom.png" width="250">
<img src="screenshots/implement_interface_image_display_density_zoom.png" width="250">
