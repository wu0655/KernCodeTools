#!/bin/bash

KERNEL_PATH=/home/wupeng/work/8996n/kernel/msm-3.18
VMLINUX_PATH=/home/wupeng/work/cdev/out/target/product/le_x10/obj/KERNEL_OBJ/vmlinux
BUILT_PATH=
#/home/wupeng/work/cdev/out/target/product/le_x10/obj/KERNEL_OBJ/
OUT_PATH=${PWD}"/out/"
MAP_PATH=${PWD}"/system.map.all"

echo "KERNEL_PATH = "${KERNEL_PATH}
echo "VMLINUX_PATH = "${VMLINUX_PATH}
echo "BUILT_PATH = "${BUILT_PATH}
echo "OUT_PATH = "${OUT_PATH}
echo "MAP_PATH = "${MAP_PATH}

#./aarch64-linux-android-nm -A -a -l -n ${VMLINUX_PATH} | grep -v '\( [aNUw] \)\|\(__crc_\)\|\( \$[adt]\)' > ${MAP_PATH}

mkdir ${OUT_PATH}
java -jar parsesystemmap.jar ${KERNEL_PATH} ${MAP_PATH} ${OUT_PATH} ${BUILT_PATH}
