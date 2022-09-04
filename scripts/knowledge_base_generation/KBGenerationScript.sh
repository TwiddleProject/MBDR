#!/bin/bash

for ((i = 10 ; i <= 100 ; i+=10)); do
    # echo "Loop iteration: $i"
    for ((j = 1 ; j <= 10 ; j++)); do
        scala kbgt-assembly-1.0.jar -r $i -s $(($i*$j)) -d uniform -o "../../data/benchmarking/cumulative/${i}x${j}" --scadrFile  --defeasibleOnly --conservative
        sed -i "" 's/~>/|~/g' "../../data/benchmarking/cumulative/${i}x${j}.txt"
        # echo "${i}x${j}"
    done
done

