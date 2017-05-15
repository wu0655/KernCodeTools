# KernCodeTools
tools about kernel code

1.  parsesystemmap.jar
    parse a special system map and generate the built source code. some file may missed.
  a) aarch64-linux-android-nm -A -a -l -n vmlinux | grep -v '\( [aNUw] \)\|\(__crc_\)\|\( \$[adt]\)' > System.map.all
  b) java -jar parsesystemmap.jar KERNEL_PATH System.map.all_PATH
  

