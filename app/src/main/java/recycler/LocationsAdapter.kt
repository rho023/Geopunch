//package recycler
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import com.example.geopunch.R
//
//class LocationsAdapter(private val locationsList: List<Locations>) :
//    RecyclerView.Adapter<LocationsAdapter.LocationsViewHolder>() {
//
//    class LocationsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val imageView: ImageView = itemView.findViewById(R.id.location_image)
//        val textName: TextView = itemView.findViewById(R.id.location_name)
//        val textDis: TextView = itemView.findViewById(R.id.location_dis)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationsViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.content, parent, false)
//        return LocationsViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: LocationsViewHolder, position: Int) {
//        val location = locationsList[position]
//        holder.textName.text = location.name
//        holder.textDis.text = location.dis
//
//        // Load image using Glide or Picasso
////        Glide.with(holder.itemView.context)
////            .load(location.imageUrl)
////            .into(holder.imageView)
//    }
//
//    override fun getItemCount(): Int {
//        return locationsList.size
//    }
//}
