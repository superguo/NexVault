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
import androidx.room.PrimaryKey

/**
 * Room entity for user-saved addresses (address book).
 *
 * Used in the Send flow to quickly select a recipient.
 *
 * Ref: doc/05-IMPLEMENTATION-PLAN-PHASE2.md Task 2.2.1
 *
 * @property id Auto-generated primary key
 * @property name User-assigned label for this address
 * @property address Ethereum-compatible address (0x...)
 * @property chainId Optional chain ID if address is chain-specific (null = all chains)
 * @property createdAt Timestamp when the entry was created (epoch millis)
 */
@Entity(tableName = "address_book")
data class AddressBookEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val address: String,
    val chainId: Int?,
    val createdAt: Long,
)
