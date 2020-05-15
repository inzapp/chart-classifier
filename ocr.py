#!/usr/bin/env python
# coding: utf-8

import sys
from PyQt5.QtWidgets import *
import cv2
import numpy as np
import glob
from pytesseract import image_to_string

class MyApp(QWidget):
    
    def __init__(self):
        super().__init__()
        self.initUI()
       
 
    def initUI(self):
        self.setGeometry(200, 200, 300, 300)
        self.setWindowTitle("OOMII Coordinates Extractor")    
        
        self.pushButton1 = QPushButton("Open Image File")
        self.pushButton1.clicked.connect(self.pushButtonForOpenImageFileClicked)
        self.label1 = QLabel()

        layout = QVBoxLayout()
        
        layout.addWidget(self.pushButton1)
        layout.addWidget(self.label1)

        self.setLayout(layout)
        
    def pushButtonForOpenImageFileClicked(self):
        global image_to_show, s_x, s_y, e_x, e_y, mouse_pressed
        
        fname = QFileDialog.getOpenFileName(self)
        self.label1.setText(fname[0])
        print(fname)
        
        ori_img = cv2.imread(fname[0])
        image_to_show = np.copy(ori_img)

        mouse_pressed = False
        s_x = s_y = e_x = e_y = -1
        

        # resize image
        def resizing(mod_img):
            return cv2.resize(mod_img, None, fx=2, fy=2, interpolation=cv2.INTER_CUBIC)

        # get grayscale image
        def get_grayscale(mod_img):
            return cv2.cvtColor(mod_img, cv2.COLOR_BGR2GRAY)

        # noise removal
        def remove_noise(mod_img):
            return cv2.medianBlur(mod_img, 5)

        # thresholding
        def thresholding(mod_img):
            return cv2.threshold(mod_img, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)[1]
        
        # dilation
        def dilate(mod_img):
            kernel = np.ones((5,5), np.uint8)
            return cv2.dilate(mod_img, kernel, iterations = 1)

        # erosion
        def erode(mod_img):
            kernel = np.ones((5,5), np.uint8)
            return cv2.erode(mod_img, kernel, iterations = 1)

        # opening - erosion followed by dilation
        def opening(mod_img):
            kernel = np.ones((5,5), np.uint8)
            return cv2.morphologyEx(mod_img, cv2.MORPH_OPEN, kernel)

        # canny edge detection
        def canny (mod_img):
            return cv2.Canny(mod_img, 100, 200)

        # skew correction
        def deskew(mod_img):
            coords = np.column_stack(np.where(mod_img > 0))
            angle = cv2.minAreaRect(coords)[-1]
            if angle < -45:
                angle = -(90 + angle)
            else:
                angle = -angle
            (h, w) = mod_img.shape[:2]
            center = (w // 2, h // 2)
            M = cv2.getRotationMatrix2D(center, angle, 1.0)
            rotated = cv2.warpAffine(mod_img, M, (w, h), flags=cv2.INTER_CUBIC, borderMode=cv2.BORDER_REPLICATE)
            return rotated

        def my_mouse_callback (event, x, y, flags, param):
            global image_to_show, s_x, s_y, e_x, e_y, mouse_pressed

            if event == cv2.EVENT_LBUTTONDOWN:
                mouse_pressed = True
                s_x, s_y = x, y
                image_to_show = np.copy(ori_img)
                print("~~~~~~~~~~~~~~~~~~")
                print("Button Down~!")
                print("s_x: ", x)
                print("s_y: ", y)
                print("~~~~~~~~~~~~~~~~~~")
                
            elif event == cv2.EVENT_MOUSEMOVE:
                if mouse_pressed:
                    image_to_show = np.copy(ori_img)
                    cv2.rectangle(image_to_show, (s_x, s_y), (x, y), (255, 0, 255), 1)
                    
            elif event == cv2.EVENT_LBUTTONUP:
                mouse_pressed = False
                e_x, e_y = x, y
                print("~~~~~~~~~~~~~~~~~~")
                print("Button up~!")
                print("e_x: ", x)
                print("e_y: ", y)
                print("~~~~~~~~~~~~~~~~~~")


        cv2.namedWindow("OOMII_image_window")
        
        
        cv2.setMouseCallback("OOMII_image_window", my_mouse_callback)
       

        while True:
            cv2.imshow("OOMII_image_window", image_to_show)
            k = cv2.waitKey(1)
            
            if k == ord("c"):
                if s_x > e_x:
                    s_x, e_x = e_x, s_x
                    
                if s_y > e_y:
                    s_y, e_y = e_y, s_y
                    
                #if e_x > s_x and e_y > s_y:
                if e_y - s_y > 1 and e_x - s_x > 0:

                    mod_img = ori_img[s_y:e_y, s_x:e_x] 
                    
                    # preproc 1 - resize image
                    resize_img = resizing (mod_img)

                    # preproc2 - grayscale
                    gray_img = get_grayscale(resize_img)

                    # preproc3 - noise removal
                    denoise_img = remove_noise(gray_img)

                    # preproc4 - thresholding
                    thresh_img = thresholding(gray_img)

                    # preproc5 - dilation
                    dilate_img = dilate(gray_img)

                    # preproc6 - erosion
                    erode_img = erode(gray_img)

                    # preproc7 - opening: erosion followed by dilation
                    open_img = opening(gray_img)

                    # preproc8 - canny edge detection
                    canny_img = canny(gray_img)

                    # preproc9 - skew correction
                    deskew_img = deskew(gray_img)

                    new_img = deskew_img
                    image_to_show = np.copy(new_img)
                    text_rec = image_to_string(new_img, config='outputbase digits')
                    
                    print("*~ 텍스트 출력 ~*")
                    print(text_rec)
                
            elif k == 27:
                break
                
        cv2.destroyAllWindows()
        
'''
        ori_image_files_counter = 0
        
        for img in ori_image_files:
            new_image_files = cv2.imread(img)[s_y:e_y, s_x:e_x]
            ori_image_files_counter += 1
            cv2.imwrite(new_path + "/new_{0:d}.jpg".format(ori_image_files_counter), new_image_files)
'''
if __name__ == '__main__':

    app = QApplication(sys.argv)
    ex = MyApp()
    ex.show()
    sys.exit(app.exec_())