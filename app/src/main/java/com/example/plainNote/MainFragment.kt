package com.example.plainNote

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.plainNote.R
import com.example.plainNote.databinding.FragmentMainBinding
import com.example.plainNote.data.NoteEntity

class MainFragment : Fragment(),
    NoteListAdapter.IListItemListener {

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: FragmentMainBinding
    private lateinit var noteListAdapter: NoteListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)

        setHasOptionsMenu(true)

        requireActivity().title = getString(R.string.app_name)


        binding = FragmentMainBinding.inflate(inflater)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        with(binding.recyclerView) {
            setHasFixedSize(true)
            val divider = DividerItemDecoration(
                context, LinearLayoutManager(context).orientation
            )
            viewModel.notesList?.observe(viewLifecycleOwner, Observer {
                Log.i("noteLogging", it.toString())
                noteListAdapter = NoteListAdapter(it, this@MainFragment)
                binding.recyclerView.adapter = noteListAdapter
                binding.recyclerView.layoutManager = LinearLayoutManager(activity)

                val selectedNotes = savedInstanceState?.getParcelableArrayList<NoteEntity>(
                    SELECTED_NOTES_KEY
                )
                noteListAdapter.selectedNotes.addAll(selectedNotes ?: emptyList())
            })
            addItemDecoration(divider)
        }

        binding.addFab.setOnClickListener {
            onItemClick(NEW_NOTE_ID)
        }
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val menuId = if (this::noteListAdapter.isInitialized &&
            noteListAdapter.selectedNotes.isNotEmpty()
        )
            R.menu.main_menu_selected_items
        else R.menu.main_menu

        inflater.inflate(menuId, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sample_note -> addSampleNotes()
            R.id.action_delete -> deleteSelectedNotes()
            R.id.action_delete_all -> deleteAllNotes()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun deleteAllNotes(): Boolean {
        viewModel.deleteAllNotes()
        Handler(Looper.getMainLooper()).postDelayed(
            {
                noteListAdapter.selectedNotes.clear()
                requireActivity().invalidateOptionsMenu()
            }, 100
        )
        return true
    }

    private fun deleteSelectedNotes(): Boolean {
        viewModel.deleteNotes(noteListAdapter.selectedNotes)
        Handler(Looper.getMainLooper()).postDelayed(
            {
                noteListAdapter.selectedNotes.clear()
                requireActivity().invalidateOptionsMenu()
            }, 100
        )
        return true
    }

    private fun addSampleNotes(): Boolean {
        viewModel.addSampleNotes()
        return true
    }

    override fun onItemClick(noteId: Int) {
        findNavController().navigate(
            MainFragmentDirections.actionEditNote().setNoteId(noteId)
        )

    }

    override fun onItemSelectionChanged() {
        requireActivity().invalidateOptionsMenu()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (this::noteListAdapter.isInitialized)
            outState.putParcelableArrayList(
                SELECTED_NOTES_KEY,
                noteListAdapter.selectedNotes
            )
        super.onSaveInstanceState(outState)
    }
}