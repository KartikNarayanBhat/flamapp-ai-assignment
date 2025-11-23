cmake_minimum_required(VERSION 3.18.1)

project("flamapp_ai_native")

add_library(
        flamapp_ai_native
        SHARED
        native-lib.cpp)

find_library(
        log-lib
        log)

target_link_libraries(
        flamapp_ai_native
        ${log-lib})
