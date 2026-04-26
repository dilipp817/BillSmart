package com.autobill.billsmart.security

import com.autobill.billsmart.exception.AppException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.authority.SimpleGrantedAuthority

/**
 * TenantUtils — Multi-tenant isolation helpers.
 *
 * Every controller that accepts a {restaurantId} path variable must call
 * [assertTenantAccess] before processing the request.  This ensures that a
 * user whose JWT says restaurantId=1 cannot reach data that belongs to
 * restaurantId=2 simply by guessing a different path variable.
 *
 * Super-admin tokens carry no restaurantId claim (null) and are allowed to
 * access any restaurant — the check is intentionally skipped for them.
 */
object TenantUtils {

    /**
     * Returns true when the current caller has the ROLE_SUPER_ADMIN authority.
     * Used to validate that a null restaurantId JWT claim is intentional, not bad data.
     */
    fun isSuperAdmin(): Boolean {
        val auth = SecurityContextHolder.getContext().authentication
        return auth?.authorities?.contains(SimpleGrantedAuthority("ROLE_SUPER_ADMIN")) == true
    }

    /**
     * Returns the restaurantId embedded in the current request's JWT, or null
     * if the caller is a super_admin (token has no restaurantId claim).
     */
    fun getJwtRestaurantId(): Long? {
        val auth = SecurityContextHolder.getContext().authentication
        return auth?.details as? Long
    }

    /**
     * Throws [AppException.ForbiddenException] (→ HTTP 403) when the JWT
     * restaurantId does not match [pathRestaurantId].
     *
     * No-op when:
     * - the JWT restaurantId is null AND the caller has ROLE_SUPER_ADMIN
     * - the JWT restaurantId equals [pathRestaurantId]  (normal user, own tenant)
     *
     * Throws 403 when:
     * - the JWT restaurantId is null but the caller is NOT ROLE_SUPER_ADMIN
     *   (guards against bad data / migration issues giving unintended cross-tenant access)
     * - the JWT restaurantId does not match [pathRestaurantId]
     */
    fun assertTenantAccess(pathRestaurantId: Long) {
        val jwtRestaurantId = getJwtRestaurantId()
        if (jwtRestaurantId == null) {
            // null restaurantId is only valid for super_admin — verify the role explicitly
            if (!isSuperAdmin()) {
                throw AppException.ForbiddenException(
                    "Access denied: missing restaurant association"
                )
            }
            return  // super_admin — allowed to access any tenant
        }
        if (jwtRestaurantId != pathRestaurantId) {
            throw AppException.ForbiddenException(
                "Access denied: you do not belong to restaurant $pathRestaurantId"
            )
        }
    }

    /**
     * Verifies that a resource (food / category / bill / payment) belongs to
     * the caller's restaurant.  Pass the resource's restaurantId as
     * [resourceRestaurantId].  Throws 403 on mismatch, 404 when the resource
     * restaurantId is null (shouldn't happen but guards against bad data).
     */
    fun assertResourceOwnership(resourceRestaurantId: Long?) {
        if (resourceRestaurantId == null) {
            throw AppException.ResourceNotFoundException("Resource not found")
        }
        assertTenantAccess(resourceRestaurantId)
    }
}

