package com.example.easyo_alarm

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.easyo_alarm.databinding.FragmentSettingBinding

class settingFragment : Fragment() {
    lateinit var binder : FragmentSettingBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_setting, null)
        binder = FragmentSettingBinding.bind(view)
        return view
    }

    override fun onResume() {
        super.onResume()

        binder.settingRecycler.layoutManager = LinearLayoutManager(requireContext())
        binder.settingRecycler.adapter = SettingRecyclerAdapter(requireContext())
        Log.d("settingFragment", "onResume()")
    }
}