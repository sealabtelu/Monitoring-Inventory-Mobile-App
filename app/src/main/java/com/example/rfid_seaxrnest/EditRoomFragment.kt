package com.example.rfid_seaxrnest

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.rfid_seaxrnest.R
import com.example.rfid_seaxrnest.model.Item
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class EditRoomFragment : Fragment() {

    private lateinit var roomTitle: TextView
    private lateinit var itemsList: LinearLayout
    private val db = FirebaseFirestore.getInstance()

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_room, container, false)

        roomTitle = view.findViewById(R.id.room_title)
        itemsList = view.findViewById(R.id.items_list)

        val nomorRak = arguments?.getString("nomorRak") ?: ""
        roomTitle.text = "Room: $nomorRak"

        fetchItemsData(nomorRak)

        return view
    }

    private fun fetchItemsData(nomorRak: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dataBarangCollection = db.collection("data_barang")
                    .whereEqualTo("nomor_rak", nomorRak)
                    .get()
                    .await()

                val items = mutableListOf<Item>() // Define Item data class with fields No, nama_barang, stok_sekarang

                for ((index, document) in dataBarangCollection.withIndex()) {
                    val namaBarang = document.getString("nama_barang") ?: "Unknown"
                    val stokSekarang = document.getLong("stok_sekarang")?.toInt() ?: 0

                    items.add(Item(index + 1, namaBarang, stokSekarang))
                }

                withContext(Dispatchers.Main) {
                    displayItems(items)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun displayItems(items: List<Item>) {
        itemsList.removeAllViews()

        for (item in items) {
            val itemView = LayoutInflater.from(context).inflate(R.layout.item_table_row, itemsList, false)

            val itemNo = itemView.findViewById<TextView>(R.id.item_no)
            val itemName = itemView.findViewById<TextView>(R.id.item_name)
            val itemStock = itemView.findViewById<TextView>(R.id.item_stock)

            itemNo.text = item.no.toString()
            itemName.text = item.namaBarang
            itemStock.text = item.stokSekarang.toString()

            itemsList.addView(itemView)
        }
    }
}

