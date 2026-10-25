cmake_minimum_required(VERSION 3.22.1)
project("vtools")

add_library(native-lib SHARED native-lib.cpp)

find_library(log-lib log)
find_library(vulkan-lib vulkan)

target_link_libraries(native-lib ${log-lib} ${vulkan-lib})
