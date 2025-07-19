package com.example.atelie

import kotlinx.datetime.LocalDate
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

@OptIn(InternalSerializationApi::class)
@Serializable
data class RowCouting(
    val count: Int
)

@OptIn(InternalSerializationApi::class)
@Serializable
data class Client(
    val id: Int,
    val name: String,
    val phone: String,
    @Serializable(with = OffsetDateTimeSerializer::class)
    val created_at: OffsetDateTime
)

@OptIn(InternalSerializationApi::class)
@Serializable
data class ClientToDb(
    val name: String,
    val phone: String
)

@OptIn(InternalSerializationApi::class)
@Serializable
data class ItemClothingToDb(
    val id_order: Int,
    val id_clothing_type: Int,
    val id_client: Int,
    val desc: String,
    val price: Float
)

@OptIn(InternalSerializationApi::class)
@Serializable
data class ItemClothing(
    val id: Int? = null,
    val id_order: Int,
    val id_clothing_type: Int,
    val id_client: Int,
    val desc: String,
    val price: Float,
    @Serializable(with = OffsetDateTimeSerializer::class)
    val created_at: OffsetDateTime? = null
)

@OptIn(InternalSerializationApi::class)
@Serializable
data class ItemClothingService(
    val id_item_clothing: Int,
    val id_service: Int,
)

@OptIn(InternalSerializationApi::class)
@Serializable
data class Order(
    val id: Int,
    val id_client: Int,
    val position: Int,
    val price: Float,
    val status_payment: Boolean,
    val date_exit: LocalDate,
    @Serializable(with = OffsetDateTimeSerializer::class)
    val created_at: OffsetDateTime
)

@OptIn(InternalSerializationApi::class)
@Serializable
data class OrderToDb(
    val id_client: Int,
    val position: Int,
    val price: Float,
    val status_payment: Boolean,
    val date_exit: LocalDate,
)

@OptIn(InternalSerializationApi::class)
@Serializable
data class OrderWithClientAndItemClothingCount(
    val id: Int,
    val id_client: Int,
    val position: Int,
    val price: Float,
    val status_payment: Boolean,
    val date_exit: LocalDate,
    @Serializable(with = OffsetDateTimeSerializer::class)
    val created_at: OffsetDateTime,
    val clients: Client,
    val items_clothing: List<RowCouting>
)