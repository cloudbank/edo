/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.droidteahouse.edo.ui

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.droidteahouse.edo.GlideRequests
import com.droidteahouse.edo.R
import com.droidteahouse.edo.vo.ArtObject


/**
 */
class ArtObjectViewHolder(view: View, private val glide: GlideRequests)
    : RecyclerView.ViewHolder(view) {
    private val title: TextView = view.findViewById(R.id.title)
    private val name: TextView = view.findViewById(R.id.name)
    //private val date: TextView = view.findViewById(R.id.date)
    internal val thumbnail: ImageView = view.findViewById(R.id.thumbnail)
    private val id: TextView = view.findViewById(R.id.id)
    // private var artObject: ArtObject? = null

    init {
        view.setOnClickListener {
            /*
            artObject?.getImageUrl()?.let { url ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                view.context.startActivity(intent)
            }
            */
        }
    }

    fun bind(art: ArtObject?) {
        //this.artObject = art
        title.text = art?.title?.trim() ?: "---"
        name.text = if (art?.people!!.isNotEmpty()) art.people.get(0).name.trim() else ""
        //medium.text = art?.medium?.trim() ?: "---"
        // date.text = art?.date ?: "[no date listed]"
        id.text = "No." + art.id
        glide.load(art.url)
                .centerCrop()
                .placeholder(R.drawable.ic_insert_photo_black_48dp)
                .into(thumbnail)
    }


    companion object {
        fun create(parent: ViewGroup, glide: GlideRequests): ArtObjectViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.art_object_item, parent, false)
            return ArtObjectViewHolder(view, glide)
        }
    }


}