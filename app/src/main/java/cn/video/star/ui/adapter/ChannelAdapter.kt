package cn.video.star.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import android.view.ViewGroup
import cn.video.star.data.remote.model.TypeData
import cn.video.star.ui.fragment.HomeFragmentItemType2
import cn.junechiu.junecore.utils.ALogger

class ChannelAdapter(
    fm: FragmentManager,
    private val data: MutableList<Fragment>, typeData: MutableList<TypeData>?
) : FragmentStatePagerAdapter(fm) {

    private var typeData: MutableList<TypeData>? = null

    init {
        this.typeData = typeData
    }

    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): Fragment {
        val fragment = data[position]
        if (position != 0 && typeData != null) {
            fragment as HomeFragmentItemType2
            fragment.callback = {
                fragment.setCategoryData(
                    typeData!![position - 1].categories,
                    typeData!![position - 1].name,
                    typeData!![position - 1].id
                )
                ALogger.d("ChannelAdapter", "name: " + typeData!![position - 1].name)
            }
        }
        return fragment
    }


    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
//        super.destroyItem(container, position, `object`)
    }
}