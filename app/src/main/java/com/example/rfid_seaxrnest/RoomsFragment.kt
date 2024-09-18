import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.rfid_seaxrnest.EditRoomFragment
import com.example.rfid_seaxrnest.R
import com.example.rfid_seaxrnest.model.Room
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class RoomsFragment : Fragment() {

    private lateinit var roomsList: LinearLayout
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_rooms, container, false)
        roomsList = view.findViewById(R.id.rooms_list)
        fetchRoomsData()
        return view
    }

    private fun fetchRoomsData() {
        CoroutineScope(Dispatchers.IO).launch {
            val roomMap = mutableMapOf<String, Int>() // Use String for nomorRak
            try {
                val dataBarangCollection = db.collection("data_barang").get().await()

                for (document in dataBarangCollection) {
                    val nomorRak = document.getString("nomor_rak")
                    val stokSekarang = document.getLong("stok_sekarang")?.toInt() ?: 0

                    if (nomorRak != null) {
                        roomMap[nomorRak] = (roomMap[nomorRak] ?: 0) + stokSekarang
                    }
                }

                val rooms = roomMap.map { (room, totalStock) -> Room(room, totalStock) }
                withContext(Dispatchers.Main) {
                    displayRooms(rooms)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun displayRooms(rooms: List<Room>) {
        roomsList.removeAllViews()

        for (room in rooms) {
            // Inflate the correct room card layout
            val roomCardView = LayoutInflater.from(context).inflate(R.layout.item_room_card, roomsList, false)

            val roomCardTitle = roomCardView.findViewById<TextView>(R.id.room_card_title)
            val roomCardStock = roomCardView.findViewById<TextView>(R.id.room_card_stock)

            roomCardTitle.text = "Room: ${room.nomorRak}"
            roomCardStock.text = "Total Items: ${room.totalStock}"

            roomCardView.setOnClickListener {
                // Navigate to EditRoom screen with nomorRak
                val editRoomFragment = EditRoomFragment().apply {
                    arguments = Bundle().apply {
                        putString("nomorRak", room.nomorRak)
                    }
                }
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, editRoomFragment)
                    .addToBackStack(null)
                    .commit()
            }

            roomsList.addView(roomCardView)
        }
    }

}
