package com.example.myrunmain

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myrunmain.R
import com.example.myrunmain.databinding.TrdFragmentBinding
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class TrdFragment : Fragment() {
    private lateinit var binding: TrdFragmentBinding

    var data:ArrayList<RunData> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = TrdFragmentBinding.inflate(inflater, container, false)
        initData()
        binding.trdRecyclerView.adapter = RunDataAdapter(data)
        return binding.root
    }

    private fun initData(){
        try{
            val scan = Scanner(resources.openRawResource(R.raw.runlist))
            while(scan.hasNextLine()) {
                val runOne = scan.nextLine()
                val runarr = runOne.split(",")
                if(runarr[2].toInt() == 3)
                    data.add(RunData(runarr[0].toDouble(),runarr[1].toDouble(),runarr[2].toInt(),runarr[3]))
            }
        }catch(e: IOException){
            Log.i("error", "initData")
        }
    }

}