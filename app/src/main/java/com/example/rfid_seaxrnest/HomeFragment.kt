package com.example.rfid_seaxrnest

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.rfid_seaxrnest.model.Room
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class HomeFragment : Fragment() {

    private lateinit var linearLayout: LinearLayout
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        linearLayout = view.findViewById(R.id.linear_layout)

        fetchRoomsData()

        return view
    }

    private fun fetchRoomsData() {
        CoroutineScope(Dispatchers.IO).launch {
            val roomMap = mutableMapOf<String, Int>() // Use String for nomorRak
            try {
                Log.d("HomeFragment", "Fetching data from Firestore")

                // Fetch documents from 'data_barang' collection
                val dataBarangCollection = db.collection("data_barang").get().await()
                Log.d("HomeFragment", "Fetched ${dataBarangCollection.size()} documents from 'data_barang'")

                for (document in dataBarangCollection) {
                    // Extract fields from each document
                    val nomorRak = document.getString("nomor_rak")
                    val stokSekarang = document.getLong("stok_sekarang")?.toInt() ?: 0

                    if (nomorRak != null) {
                        // Aggregate the stock for each room
                        roomMap[nomorRak] = (roomMap[nomorRak] ?: 0) + stokSekarang
                    }
                }

                // Convert map to list and update UI
                val rooms = roomMap.map { (room, totalStock) -> Room(room, totalStock) }
                withContext(Dispatchers.Main) {
                    displayRooms(rooms)
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error fetching data", e)
            }
        }
    }


    private fun displayRooms(rooms: List<Room>) {
        linearLayout.removeAllViews() // Clear previous views if any
        for (room in rooms) {
            // Create a new LinearLayout for each room entry
            val roomView = LayoutInflater.from(context).inflate(R.layout.item_room, linearLayout, false)

            // Set the text of the TextViews
            val roomTitle = roomView.findViewById<TextView>(R.id.room_title)
            val roomStock = roomView.findViewById<TextView>(R.id.room_stock)

            roomTitle.text = "Room: ${room.nomorRak}"
            roomStock.text = "Total Items: ${room.totalStock}"

            // Add the roomView to the LinearLayout
            linearLayout.addView(roomView)
        }
    }
}
