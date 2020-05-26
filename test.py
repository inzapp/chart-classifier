import cv2
import pytesseract
from concurrent.futures import ThreadPoolExecutor
pytesseract.pytesseract.tesseract_cmd = 'E:/Tesseract-OCR/tesseract.exe'

template = cv2.imread('methacholine_template.jpg', cv2.IMREAD_COLOR)
tpe = ThreadPoolExecutor(8)

def detect(image, template, ratio):
    resized = cv2.resize(image, dsize=(0, 0), fx=ratio, fy=ratio, interpolation=cv2.INTER_CUBIC)
    res = cv2.matchTemplate(resized, template, cv2.TM_CCOEFF)
    (_, max_val, _, max_loc) = cv2.minMaxLoc(res)
    # print(max_val)
    # print(ratio)
    return [max_val, max_loc, resized]


def get_max_matched_res(image, template):
    ratio = 0.980
    max_val = -1
    max_loc = -1
    max_chart = -1

    fs = []
    ratio = 0.980
    for i in range(1, 8 + 1):
        fs.append(tpe.submit(detect, image, template, ratio))
        ratio += 0.005

    max_template_match_val  = -1
    max_template_match_loc = -1
    max_template_match_img = -1
    for f in fs:
        res = f.result()
        if res[0] > max_template_match_val:
            max_template_match_val = res[0]
            max_template_match_loc = res[1]
            max_template_match_img = res[2]
    
    return [max_template_match_img, max_template_match_loc]


def get_methacholine_aridol_table(image, template):
    res = get_max_matched_res(image, template)
    img = res[0]
    loc = res[1]
    h, w, ch = template.shape
    table_x = loc[0]
    table_y = loc[1] + h
    table_w = w
    table_h = 360
    table = img[table_y:table_y+table_h, table_x:table_x+table_w]
    return table


img = cv2.imread('1.jpg', cv2.IMREAD_COLOR)
table = get_methacholine_aridol_table(img, template)
ocr_res = pytesseract.image_to_string(table, config='-psm 6 digits')
print(ocr_res)
cv2.imshow('table', table)
cv2.waitKey(0)


