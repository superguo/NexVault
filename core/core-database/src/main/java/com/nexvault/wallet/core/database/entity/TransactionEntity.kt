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
import androidx.room.Index

/**
 * Room entity for blockchain transactions (both native coin and ERC-20 transfers).
 *
 * Indices are added for frequently queried columns to improve query performance.
 *
 * Ref: doc/05-IMPLEMENTATION-PLAN-PHASE2.md Task 2.2.1
 * Ref: doc/09-PERFORMANCE-OPTIMIZATION.md Section 4.1 - Indices
 *
 * @property txHash Transaction hash (unique per chain)
 * @property chainId The blockchain chain ID
 * @property fromAddress Sender address
 * @property toAddress Recipient address
 * @property value Transaction value as a String (in wei for native, in token units for ERC-20)
 * @property gasUsed Gas consumed by the transaction (null if pending)
 * @property gasPrice Gas price in wei
 * @property tokenSymbol ERC-20 token symbol (null for native coin transfers)
 * @property tokenContractAddress ERC-20 contract address (null for native)
 * @property tokenDecimals ERC-20 token decimals (null for native)
 * @property blockNumber Block number where the tx was included
 * @property timestamp Unix timestamp in seconds
 * @property status 0 = pending, 1 = confirmed, 2 = failed
 * @property type Transaction type: "send", "receive", "swap", "contract", "approve"
 */
@Entity(
    tableName = "transactions",
    primaryKeys = ["txHash", "chainId"],
    indices = [
        Index(value = ["chainId", "fromAddress"]),
        Index(value = ["chainId", "toAddress"]),
        Index(value = ["timestamp"]),
        Index(value = ["chainId", "status"]),
    ],
)
data class TransactionEntity(
    val txHash: String,
    val chainId: Int,
    val fromAddress: String,
    val toAddress: String,
    val value: String,
    val gasUsed: String?,
    val gasPrice: String?,
    val tokenSymbol: String?,
    val tokenContractAddress: String?,
    val tokenDecimals: Int?,
    val blockNumber: Long,
    val timestamp: Long,
    val status: Int,
    val type: String,
)
