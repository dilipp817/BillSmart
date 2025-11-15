package com.autobill.billsmart.repositories

import com.autobill.billsmart.model.Restaurant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RestaurantRepository : JpaRepository<Restaurant, Long>

