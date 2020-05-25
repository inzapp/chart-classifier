from concurrent.futures import ThreadPoolExecutor
pool = ThreadPoolExecutor(4)

def task(param1, param2):
    return param2
    

if __name__ == "__main__":
    fs = []
    fs.append(pool.submit(task, '1', '2'))
    fs.append(pool.submit(task, ('2', '3')))
    fs.append(pool.submit(task, ('2', '3')))

    for f in fs:
        print(f.result())
