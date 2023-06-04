package com.example.myrunmain

data class RunData(var len:Double, var pace:Double, var dif:Int, var date:String)
//코스 길이, 평균 페이스, 난이도(초급자 1, 중급자 2, 고급자 3, 난이도 없음 0), 날짜("YYYY.MM.DD 0요일")