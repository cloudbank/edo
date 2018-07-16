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

import android.arch.paging.PagedListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.droidteahouse.edo.GlideRequests
import com.droidteahouse.edo.R
import com.droidteahouse.edo.repository.NetworkState
import com.droidteahouse.edo.vo.ArtObject

/**
 */
class ArtObjectAdapter(
    private val glide: GlideRequests,
    private val retryCallback: () -> Unit)
  : PagedListAdapter<ArtObject, RecyclerView.ViewHolder>(DIFF_CALLBACK) {
  private var networkState: NetworkState? = null
  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (getItemViewType(position)) {
      R.layout.art_object_item -> (holder as ArtObjectViewHolder).bind(getItem(position))
      R.layout.network_state_item -> (holder as NetworkStateItemViewHolder).bindTo(
          networkState)
    }
  }


  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      R.layout.art_object_item -> ArtObjectViewHolder.create(parent, glide)
      R.layout.network_state_item -> NetworkStateItemViewHolder.create(parent, retryCallback)
      else -> throw IllegalArgumentException("unknown view type $viewType")
    }
  }

  private fun hasExtraRow() = networkState != null && networkState != NetworkState.LOADED

  override fun getItemViewType(position: Int): Int {
    return if (hasExtraRow() && position == itemCount - 1) {
      R.layout.network_state_item
    } else {
      R.layout.art_object_item
    }
  }

  override fun getItemCount(): Int {
    return super.getItemCount() + if (hasExtraRow()) 1 else 0
  }

  fun setNetworkState(newNetworkState: NetworkState?) {
    val previousState = this.networkState
    val hadExtraRow = hasExtraRow()
    this.networkState = newNetworkState
    val hasExtraRow = hasExtraRow()
    if (hadExtraRow != hasExtraRow) {
      if (hadExtraRow) {
        notifyItemRemoved(super.getItemCount())
      } else {
        notifyItemInserted(super.getItemCount())
      }
    } else if (hasExtraRow && previousState != newNetworkState) {
      notifyItemChanged(itemCount - 1)
    }
  }


  companion object {
    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ArtObject>() {
      override fun areContentsTheSame(oldItem: ArtObject, newItem: ArtObject): Boolean =
          oldItem == newItem

      override fun areItemsTheSame(oldItem: ArtObject, newItem: ArtObject): Boolean =
          oldItem.id == newItem.id


    }

  }


}