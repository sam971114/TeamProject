package com.example.myrunmain

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myrunmain.R
import com.example.myrunmain.databinding.SecFragmentBinding
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class SecFragment : Fragment() {
    private lateinit var binding: SecFragmentBinding

    var data:ArrayList<RunData> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SecFragmentBinding.inflate(inflater, container, false)
        initData()
        binding.secRecyclerView.adapter = RunDataAdapter(data)
        return binding.root
    }

    private fun initData(){
        try {
            val file = File("/data/data/com.example.myrunmain/files/runlist.txt")
            val reader = BufferedReader(FileReader(file))

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                println(line)
                val runarr = line!!.split(",")
                if (runarr[2].toInt() == 2)
                    data.add(
                        RunData(
                            runarr[0].toDouble(),
                            runarr[1].toDouble(),
                            runarr[2].toInt(),
                            runarr[3]
                        )
                    )
            }

            reader.close()
        } catch (e: Exception) {
            println("파일 읽기 오류: ${e.message}")
        }
    }
}