#!/bin/bash

for i in {1..15}; do
    echo "Loop iteration: $i"
    scala kbgt-assembly-1.0.jar -r $i -s $((i*2)) -d uniform -o "knowledge_bases/Generated/ranks$i" --scadrFile  --defeasibleOnly --conservative
    sed -i "" 's/~>/|~/g' "knowledge_bases/Generated/ranks$i.txt"
done

