package com.example.storybookapiintegration.ui.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.storybookapiintegration.R
import com.example.storybookapiintegration.databinding.FragmentBookmarksBinding

class BookmarksFragment : Fragment(R.layout.fragment_bookmarks) {

    private var _binding: FragmentBookmarksBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBookmarksBinding.bind(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}