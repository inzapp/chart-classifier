import cv2
import pytesseract
from concurrent.futures import ThreadPoolExecutor
pytesseract.pytesseract.tesseract_cmd = 'E:/Tesseract-OCR/tesseract.exe'

methacholine_header = cv2.imread('methacholine_template.jpg', cv2.IMREAD_COLOR)
pool = ThreadPoolExecutor(8)

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
        fs.append(pool.submit(detect, image, template, ratio))
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


def pre_process_table(table):
    table = cv2.cvtColor(table, cv2.COLOR_BGR2GRAY)
    tmp, table = cv2.threshold(table, 190, 255, cv2.THRESH_BINARY)
    table = cv2.blur(table, (2, 2))
    return table


def get_methacholine_aridol_table(image):
    res = get_max_matched_res(image, methacholine_header)
    img = res[0]
    loc = res[1]
    h, w, ch = methacholine_header.shape
    table_x = loc[0]
    table_y = loc[1] + h
    table_w = w
    table_h = 360
    table = img[table_y:table_y+table_h, table_x:table_x+table_w]
    return pre_process_table(table)


def table_to_arr(table_image):
    ocr_res = pytesseract.image_to_string(table, config='-psm 6 digits')
    sp = ocr_res.split('\n')
    arr = []
    for s in sp:
        if s != '':
            ns = s.split(' ')
            tmp_arr = []
            for n in ns:
                tmp_arr.append(n)
            arr.append(tmp_arr)
    return arr


img = cv2.imread('1.jpg', cv2.IMREAD_COLOR)
table = get_methacholine_aridol_table(img)
arr = table_to_arr(table)
print(arr)
cv2.imshow('table', table)
cv2.waitKey(0)