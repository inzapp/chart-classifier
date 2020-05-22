from multiprocessing import Process, Queue, Value

max_process_cnt = 10
cur_process_cnt = 0
global processes

def process_ocr(submat):
    value = Value("d", 0.0, lock=False)
    p = Process(target=ocr, args=(submat, value))
    p.start()
    p.join()
    return value.value


def ocr(q, key, submat):
    q.put({key: submat + 1})
    return
    

if __name__ == "__main__":
    global processes
    q = Queue()
    processes = []
    i = 0
    while True:
        if i >= 100:
            break
        processes.append(Process(target=ocr, args=(q, 'tmp_key' + str(i), i)))
        print('append process')
        i += 1
        
    for p in processes:
        p.start()
        print('start process')
        
    for p in processes:
        p.join()
        print('join process')

    q.put({'END': True})

    while True:
        q_val = q.get()
        try:
            if q_val['END'] == True:
                break
        except:
            pass
        print(q_val)
        
