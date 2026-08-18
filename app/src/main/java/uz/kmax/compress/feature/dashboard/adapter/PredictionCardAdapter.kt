package uz.kmax.compress.feature.dashboard.adapter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import uz.kmax.compress.databinding.ItemPredictionCardBinding
class PredictionCardAdapter: ListAdapter<Pair<String,String>, PredictionCardAdapter.Holder>(Diff) { fun submit(items: List<Pair<String,String>>) = submitList(items); override fun onCreateViewHolder(p:ViewGroup,v:Int)=Holder(ItemPredictionCardBinding.inflate(LayoutInflater.from(p.context),p,false)); override fun onBindViewHolder(h:Holder,p:Int)=h.bind(getItem(p)); class Holder(private val b:ItemPredictionCardBinding):RecyclerView.ViewHolder(b.root){fun bind(v:Pair<String,String>){b.title.text=v.first;b.value.text=v.second;b.root.contentDescription="${v.first}: ${v.second}"}}; private object Diff:DiffUtil.ItemCallback<Pair<String,String>>(){override fun areItemsTheSame(a:Pair<String,String>,b:Pair<String,String>)=a.first==b.first;override fun areContentsTheSame(a:Pair<String,String>,b:Pair<String,String>)=a==b}}
