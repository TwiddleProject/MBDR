import os
from os import listdir
from os.path import isfile, join
import random

QUERY_DIR = 'data/benchmarking/cumulative/query_sets'
NUM_QUERIES = 10


def main():
    query_set_filenames = [f for f in listdir(
        QUERY_DIR) if isfile(join(QUERY_DIR, f))]

    for filename in query_set_filenames:
        print(filename)
        with open(os.path.join(QUERY_DIR, filename)) as f:
            lines = f.readlines()
            lines = random.sample(lines, NUM_QUERIES)
            lines[-1] = lines[-1].strip()
        with open(os.path.join(QUERY_DIR, filename), mode='w') as f:
            f.writelines(lines)


if __name__ == '__main__':
    main()
