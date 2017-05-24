# KernCodeTools
tools about kernel code. 
This project is developed by Android Studio. You could use jar file under out diretory or compile it by yourself.

1.  parsesystemmap.jar
    parse a special system map and generate the built source code. some file may missed.
    a) aarch64-linux-android-nm -A -a -l -n vmlinux | grep -v '\( [aNUw] \)\|\(__crc_\)\|\( \$[adt]\)' > System.map.all
    b) java -jar parsesystemmap.jar KERNEL_PATH System.map.all_PATH
  

2. parseKernBuild.jar
   a) when kernel was built, dot_target cmd file is generated. the name is likely .xx.o.cmd. It contains the source file
   the head file.
   b) parse the cmd files and get the source
   c) java -jar parseKernBuild.jar KERNEL_SOURCE_DIR KERNEL_OBJ_DIR
   
