/*
 * Copyright (c) 2025 NexVault
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nexvault.wallet.core.database.entity

import androidx.room.Entity

/**
 * Room entity for NFTs (ERC-721 and ERC-1155) owned by the user.
 *
 * NFT metadata (name, description, image) is cached here to avoid
 * re-fetching from IPFS/HTTP on every screen load.
 *
 * Ref: doc/05-IMPLEMENTATION-PLAN-PHASE2.md Task 2.2.1
 *
 * @property contractAddress NFT contract address
 * @property tokenId NFT token ID within the contract
 * @property chainId The blockchain chain ID
 * @property name NFT name from metadata
 * @property description NFT description from metadata
 * @property imageUrl Resolved image URL (IPFS URLs converted to HTTP gateway)
 * @property animationUrl Animation/video URL if present
 * @property collectionName Name of the NFT collection
 * @property standard Token standard: "ERC-721" or "ERC-1155"
 * @property attributes JSON string of NFT trait attributes
 * @property lastUpdated Timestamp of the last metadata refresh (epoch millis)
 */
@Entity(
    tableName = "nfts",
    primaryKeys = ["contractAddress", "tokenId", "chainId"],
)
data class NftEntity(
    val contractAddress: String,
    val tokenId: String,
    val chainId: Int,
    val name: String?,
    val description: String?,
    val imageUrl: String?,
    val animationUrl: String?,
    val collectionName: String?,
    val standard: String,
    val attributes: String?,
    val lastUpdated: Long,
)
