import cv2

w = 41
h = 20
dose_h_offset = 23

pos = {}
pos['ref'] = 150
pos['pre'] = 221
pos['lv1'] = 287
pos['lv2'] = 354
pos['lv3'] = 421
pos['lv4'] = 488
pos['lv5'] = 554
pos['lv6'] = 620

pos['fvc_dose'] = 288
pos['fvc_liters'] = 308
pos['fvc_pref'] = 331
pos['fvc_pchg'] = 351

pos['fev1_dose'] = 382
pos['fev1_liters'] = 403
pos['fev1_pref'] = 422
pos['fev1_pchg'] = 444

pos['fef_25_75_dose'] = 473
pos['fef_25_75_per'] = 494
pos['fef_25_75_pref'] = 519
pos['fef_25_75_pchg'] = 539

pos['pef_dose'] = 564
pos['pef_l_sec'] = 585
pos['pef_pref'] = 605
pos['pef_pchg'] = 626

def submat(img, x_pos_title, y_pos_title):
    if y_pos_title.find('dose') > -1:
        return img[pos[y_pos_title]:pos[y_pos_title]+h, pos[x_pos_title]-dose_h_offset:pos[x_pos_title]+w]
    else
        return img[pos[y_pos_title]:pos[y_pos_title]+h, pos[x_pos_title]:pos[x_pos_title]+w]

img = cv2.imread('1.jpg', cv2.IMREAD_COLOR)
for i in range(1, 6 + 1):
    cv2.imshow('img', img[pos['fvc_liters']:pos['fvc_liters'] + h, pos['lv' + str(i)]:pos['lv' + str(i)] + w])
    cv2.waitKey(0)

for i in range(1, 6 + 1):
    cv2.imshow('img', img[pos['fvc_dose']:pos['fvc_dose'] + h, pos['lv' + str(i)] - dose_h_offset :pos['lv' + str(i)] + w])
    cv2.waitKey(0)
    
for i in range(1, 6 + 1):
    cv2.imshow('img', img[pos['fvc_pref']:pos['fvc_pref'] + h, pos['lv' + str(i)]:pos['lv' + str(i)] + w])
    cv2.waitKey(0)
    
for i in range(1, 6 + 1):
    cv2.imshow('img', img[pos['fvc_pchg']:pos['fvc_pchg'] + h, pos['lv' + str(i)]:pos['lv' + str(i)] + w])
    cv2.waitKey(0)
    
cv2.destroyAllWindows()
