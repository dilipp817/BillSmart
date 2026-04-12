package com.autobill.billsmart.model.enums

/**
 * OrderType Enum - Distinguishes how an order is fulfilled
 *
 * | Type     | Table? | Packaging? | Delivery address? |
 * |----------|--------|------------|-------------------|
 * | DINE_IN  | Yes    | No         | No                |
 * | TAKEAWAY | No     | Yes        | No                |
 * | DELIVERY | No     | Yes        | Yes               |
 */
enum class OrderType {
    /** Customer eats at the restaurant — table assignment required */
    DINE_IN,

    /** Customer collects at the counter — packaging required */
    TAKEAWAY,

    /** Restaurant delivers to customer — packaging + delivery address required */
    DELIVERY
}

