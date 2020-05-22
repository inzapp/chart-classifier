from multiprocessing import Process, Queue, Value

max_process_cnt = 10
cur_process_cnt = 0

def process_ocr(submat):
    value = Value("d", 0.0, lock=False)
    p = Process(target=ocr, args=(submat, value))
    p.start()
    p.join()
    return value.value


def ocr(map, key, submat):
    map[key] = submat + 1
    return
    

if __name__ == "__main__":
    map = {}
    processes = []
    i = 0
    while True:
        if i >= 100:
            break
        processes.append(Process(target=ocr, args=(map, 'tmp_key' + str(i), i)))
        print('append process')
        i += 1
        
    for p in processes:
        p.start()
        print('start process')
        
    for p in processes:
        p.join()
        print('join process')

    print(map)
        
