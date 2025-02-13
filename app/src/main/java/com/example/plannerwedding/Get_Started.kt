package com.example.plannerwedding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController

class Get_Started : Fragment() {
    private lateinit var getStartedButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = inflater.inflate(R.layout.fragment_get__started, container, false)

        // Initialize the button
        getStartedButton = binding.findViewById(R.id.getStartedButton)

        // Set OnClickListener to navigate to the Login fragment using NavController
        getStartedButton.setOnClickListener {
            // Use the NavController to navigate using the action defined in the nav_graph
            findNavController().navigate(R.id.action_getStarted_to_loginFragment)
        }

        return binding
    }
}
