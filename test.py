import cv2
import pytesseract
pytesseract.pytesseract.tesseract_cmd = 'E:/Tesseract-OCR/tesseract.exe'

def methacholine(img, x_pos_title, y_pos_title):
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

    pos['fvc_dose'] = 286
    pos['fvc_liters'] = 308
    pos['fvc_pref'] = 329
    pos['fvc_pchg'] = 349

    pos['fev1_dose'] = 380
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

    if y_pos_title.find('dose') > -1:
        img = img[pos[y_pos_title]:pos[y_pos_title]+h, pos[x_pos_title]-dose_h_offset:pos[x_pos_title]+w]
    else:
        img = img[pos[y_pos_title]:pos[y_pos_title]+h, pos[x_pos_title]:pos[x_pos_title]+w]

    img = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    return img

x_pos = [
    'ref',
    'pre', 
    'lv1', 
    'lv2', 
    'lv3', 
    'lv4', 
    'lv5', 
    'lv6'
]

y_pos = [    
    'fvc_dose',
    'fvc_liters',
    'fvc_pref',
    'fvc_pchg',

    'fev1_dose',
    'fev1_liters',
    'fev1_pref',
    'fev1_pchg',

    'fef_25_75_dose',
    'fef_25_75_per',
    'fef_25_75_pref',
    'fef_25_75_pchg',

    'pef_dose',
    'pef_l_sec',
    'pef_pref',
    'pef_pchg',
]

img = cv2.imread('1.jpg', cv2.IMREAD_COLOR)
for i in range(1, 6 + 1):
    sub = methacholine(img, 'lv' + str(i), 'fev1_dose')
    cv2.imshow('img', sub)
    print(pytesseract.image_to_string(sub))
    cv2.waitKey(0)

cv2.destroyAllWindows()
