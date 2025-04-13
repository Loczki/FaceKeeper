/*
 * Copyright 2023 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.mediapipe.examples.facedetection

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Friend(
    val id: String,
    val name: String,
    val status: String,
    val avatarResId: Int,
    val lastSeen: String,
    val role: String,
    val accessLevel: Int,
    var isExpanded: Boolean = false
)

class FriendsAdapter(
    private val friends: List<Friend>
) : RecyclerView.Adapter<FriendsAdapter.FriendViewHolder>() {

    class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avatar: ImageView = itemView.findViewById(R.id.friend_avatar)
        val name: TextView = itemView.findViewById(R.id.friend_name)
        val status: TextView = itemView.findViewById(R.id.friend_status)
        val mainInfo: LinearLayout = itemView.findViewById(R.id.main_info_layout)
        val expandedInfo: LinearLayout = itemView.findViewById(R.id.expanded_info_layout)
        val lastSeen: TextView = itemView.findViewById(R.id.friend_last_seen)
        val role: TextView = itemView.findViewById(R.id.friend_role)
        val id: TextView = itemView.findViewById(R.id.friend_id)
        val accessLevel: TextView = itemView.findViewById(R.id.friend_access_level)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.friend_item, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friends[position]

        holder.avatar.setImageResource(friend.avatarResId)
        holder.name.text = friend.name
        holder.status.text = friend.status
        holder.lastSeen.text = "Last seen: ${friend.lastSeen}"
        holder.role.text = "Role: ${friend.role}"
        holder.id.text = "ID: ${friend.id}"
        holder.accessLevel.text = "Access Level: ${friend.accessLevel}"

        holder.expandedInfo.visibility = if (friend.isExpanded) View.VISIBLE else View.GONE

        holder.mainInfo.setOnClickListener {
            friend.isExpanded = !friend.isExpanded
            notifyItemChanged(position)
        }
    }

    override fun getItemCount() = friends.size
}