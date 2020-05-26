import cv2
import pytesseract
from concurrent.futures import ThreadPoolExecutor
pytesseract.pytesseract.tesseract_cmd = 'E:/Tesseract-OCR/tesseract.exe'

template = cv2.imread('methacholine_template.jpg', cv2.IMREAD_COLOR)
tpe = ThreadPoolExecutor(8)

def detect(chart, template, ratio):
    proc = cv2.resize(chart, dsize=(0, 0), fx=ratio, fy=ratio, interpolation=cv2.INTER_CUBIC)
    res = cv2.matchTemplate(proc, template, cv2.TM_CCOEFF)
    (_, max_val, _, max_loc) = cv2.minMaxLoc(res)
    print(max_val)
    print(ratio)
    return [max_val, max_loc, proc]


def get_methacholine_aridol_table(img_file_name):
    chart = cv2.imread(img_file_name, cv2.IMREAD_COLOR)
    ratio = 0.980
    h, w, ch = template.shape
    max_val = -99999999
    max_loc = 0
    max_chart = 0

    fs = []
    ratio = 0.980
    for i in range(1, 8):
        fs.append(tpe.submit(detect, chart, template, ratio))
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

    table_x = max_template_match_loc[0]
    table_y = max_template_match_loc[1] + h
    table_w = w
    table_h = 360
    table = chart[table_y:table_y+table_h, table_x:table_x+table_w]
    cv2.imshow('table', table)
    cv2.waitKey(0)


get_methacholine_aridol_table('0.jpg')
        

