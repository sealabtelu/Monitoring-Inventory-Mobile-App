package com.example.rfid_seaxrnest

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.rfid_seaxrnest.model.Item
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private lateinit var linearLayout: LinearLayout
    private lateinit var searchBar: EditText
    private val db = FirebaseFirestore.getInstance()
    private var allItems: List<Item> = listOf()
    private var roomMap = mutableMapOf<String, Int>() // Store rooms and their total stock

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        linearLayout = view.findViewById(R.id.linear_layout)
        searchBar = view.findViewById(R.id.search_bar)

        fetchItemsData()

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterItems(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        return view
    }

    private fun fetchItemsData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("HomeFragment", "Fetching items data from Firestore")

                val itemsCollection = db.collection("data_barang").get().await()
                val items = mutableListOf<Item>()

                for (document in itemsCollection) {
                    val no = document.getLong("no")?.toInt() ?: 0
                    val namaBarang = document.getString("nama_barang") ?: ""
                    val stokSekarang = document.getLong("stok_sekarang")?.toInt() ?: 0
                    val nomorRak = document.getString("nomor_rak") ?: ""

                    items.add(Item(no, namaBarang, stokSekarang))

                    if (nomorRak.isNotEmpty()) {
                        roomMap[nomorRak] = (roomMap[nomorRak] ?: 0) + stokSekarang
                    }
                }

                allItems = items

                withContext(Dispatchers.Main) {
                    displayRooms()
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error fetching data", e)
            }
        }
    }

    private fun filterItems(query: String) {
        if (query.isEmpty()) {
            displayRooms()
        } else {
            val filteredItems = allItems.filter { it.namaBarang.contains(query, ignoreCase = true) }
            displayItems(filteredItems)
        }
    }

    private fun displayRooms() {
        linearLayout.removeAllViews()
        for ((room, totalStock) in roomMap) {
            val roomView = LayoutInflater.from(context).inflate(R.layout.item_room, linearLayout, false)

            val roomTitle = roomView.findViewById<TextView>(R.id.room_title)
            val roomStock = roomView.findViewById<TextView>(R.id.room_stock)

            roomTitle.text = "Room: $room"
            roomStock.text = "Total Items: $totalStock"

            linearLayout.addView(roomView)
        }
    }

    private fun displayItems(items: List<Item>) {
        linearLayout.removeAllViews()
        for (item in items) {
            val itemView = LayoutInflater.from(context).inflate(R.layout.item_room, linearLayout, false)
            val itemName = itemView.findViewById<TextView>(R.id.room_title)
            val itemStock = itemView.findViewById<TextView>(R.id.room_stock)

            itemName.text = "Item: ${item.namaBarang}"
            itemStock.text = "Stock: ${item.stokSekarang}"

            linearLayout.addView(itemView)
        }
    }
}
